package eventHub.deepak.service.review;

import eventHub.deepak.dto.request.ReviewRequest;
import eventHub.deepak.dto.response.ReviewEligibilityResponse;
import eventHub.deepak.dto.response.ReviewResponse;
import eventHub.deepak.entity.Event;
import eventHub.deepak.entity.EventReview;
import eventHub.deepak.entity.User;
import eventHub.deepak.enums.BookingStatus;
import eventHub.deepak.enums.Role;
import eventHub.deepak.globalExceptionHandler.EventNotFoundException;
import eventHub.deepak.globalExceptionHandler.ReviewNotFoundException;
import eventHub.deepak.repository.BookingRepository;
import eventHub.deepak.repository.EventRepository;
import eventHub.deepak.repository.EventReviewRepository;
import eventHub.deepak.repository.UserRepository;
import eventHub.deepak.util.PageableSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class EventReviewService {
    private final EventReviewRepository reviews;
    private final EventRepository events;
    private final BookingRepository bookings;
    private final UserRepository users;

    public EventReviewService(EventReviewRepository reviews, EventRepository events, BookingRepository bookings, UserRepository users) {
        this.reviews = reviews; this.events = events; this.bookings = bookings; this.users = users;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> list(Long eventId, Pageable pageable) {
        if (!events.existsById(eventId)) throw new EventNotFoundException(eventId);
        Pageable safe = PageableSupport.validate(pageable, Set.of("createdAt", "rating"), Sort.by(Sort.Order.desc("createdAt")));
        return reviews.findByEventId(eventId, safe).map(this::response);
    }

    @Transactional(readOnly = true)
    public ReviewEligibilityResponse eligibility(Long eventId, String email) {
        User customer = customer(email);
        Event event = event(eventId);
        EventReview existing = reviews.findByCustomerIdAndEventId(customer.getId(), eventId).orElse(null);
        String reason = eligibilityReason(event, customer);
        return new ReviewEligibilityResponse(reason == null, reason, existing == null ? null : response(existing));
    }

    @Transactional
    public ReviewResponse create(Long eventId, ReviewRequest request, String email) {
        User customer = customer(email);
        Event event = event(eventId);
        requireEligible(event, customer);
        if (reviews.existsByCustomerIdAndEventId(customer.getId(), eventId))
            throw new IllegalStateException("You have already reviewed this event");
        EventReview review = EventReview.builder().customer(customer).event(event).rating(request.rating())
                .comment(clean(request.comment())).build();
        return response(reviews.saveAndFlush(review));
    }

    @Transactional
    public ReviewResponse update(Long reviewId, ReviewRequest request, String email) {
        User customer = customer(email);
        EventReview review = reviews.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (!review.getCustomer().getId().equals(customer.getId())) throw new AccessDeniedException("You may edit only your own review");
        requireEligible(review.getEvent(), customer);
        review.setRating(request.rating());
        review.setComment(clean(request.comment()));
        return response(reviews.save(review));
    }

    @Transactional
    public void delete(Long reviewId, String email) {
        User customer = customer(email);
        EventReview review = reviews.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (!review.getCustomer().getId().equals(customer.getId())) throw new AccessDeniedException("You may delete only your own review");
        reviews.delete(review);
    }

    private void requireEligible(Event event, User customer) {
        String reason = eligibilityReason(event, customer);
        if (reason != null) throw new AccessDeniedException(reason);
    }

    private String eligibilityReason(Event event, User customer) {
        if (!bookings.existsByCustomerIdAndEventIdAndStatus(customer.getId(), event.getId(), BookingStatus.CONFIRMED))
            return "A confirmed booking is required to review this event";
        LocalDateTime end = event.getStartTime().plus(event.getDuration() == null ? java.time.Duration.ofHours(24) : event.getDuration());
        if (end.isAfter(LocalDateTime.now())) return "Reviews are available after the event has ended";
        return null;
    }

    private Event event(Long id) { return events.findById(id).orElseThrow(() -> new EventNotFoundException(id)); }
    private User customer(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new AccessDeniedException("Account is unavailable"));
        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getRole() != Role.CUSTOMER)
            throw new AccessDeniedException("Only an active customer may review events");
        return user;
    }
    private String clean(String comment) { return comment == null ? "" : comment.trim(); }
    private ReviewResponse response(EventReview review) {
        return new ReviewResponse(review.getId(), review.getEvent().getId(), review.getCustomer().getName(), review.getRating(), review.getComment(), review.getCreatedAt(), review.getUpdatedAt());
    }
}
