package eventHub.deepak.service.favourite;

import eventHub.deepak.dto.response.EventResponse;
import eventHub.deepak.dto.response.FavouriteResponse;
import eventHub.deepak.entity.Favourite;
import eventHub.deepak.entity.User;
import eventHub.deepak.enums.Role;
import eventHub.deepak.globalExceptionHandler.EventNotFoundException;
import eventHub.deepak.mapper.EventMapper;
import eventHub.deepak.repository.EventRepository;
import eventHub.deepak.repository.FavouriteRepository;
import eventHub.deepak.repository.UserRepository;
import eventHub.deepak.util.PageableSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
public class FavouriteService {
    private final FavouriteRepository favourites;
    private final EventRepository events;
    private final UserRepository users;
    private final EventMapper mapper;

    public FavouriteService(FavouriteRepository favourites, EventRepository events, UserRepository users, EventMapper mapper) {
        this.favourites=favourites; this.events=events; this.users=users; this.mapper=mapper;
    }

    @Transactional(readOnly = true)
    public Page<FavouriteResponse> list(String email, Pageable pageable) {
        User customer=customer(email);
        Pageable safe=PageableSupport.validate(pageable, Set.of("createdAt"), Sort.by(Sort.Order.desc("createdAt")));
        return favourites.findByCustomerId(customer.getId(),safe).map(this::response);
    }

    @Transactional
    public FavouriteResponse add(Long eventId,String email) {
        User customer=customer(email);
        if(!events.existsById(eventId))throw new EventNotFoundException(eventId);
        Favourite favourite=favourites.findByCustomerIdAndEventId(customer.getId(),eventId).orElseGet(()->{
            var event=events.findById(eventId).orElseThrow(()->new EventNotFoundException(eventId));
            return favourites.saveAndFlush(Favourite.builder().customer(customer).event(event).build());
        });
        return response(favourite);
    }

    @Transactional
    public void remove(Long eventId,String email) {
        User customer=customer(email);
        favourites.deleteByCustomerIdAndEventId(customer.getId(),eventId);
    }

    private User customer(String email) {
        User user=users.findByEmail(email).orElseThrow(()->new AccessDeniedException("Account is unavailable"));
        if(!Boolean.TRUE.equals(user.getIsActive())||user.getRole()!=Role.CUSTOMER)
            throw new AccessDeniedException("Only an active customer may manage favourites");
        return user;
    }
    private FavouriteResponse response(Favourite favourite) {
        EventResponse event=mapper.toResponse(favourite.getEvent());
        return new FavouriteResponse(favourite.getId(),event.getId(),event,favourite.getCreatedAt());
    }
}
