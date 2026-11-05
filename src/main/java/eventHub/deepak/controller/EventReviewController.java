package eventHub.deepak.controller;

import eventHub.deepak.dto.request.ReviewRequest;
import eventHub.deepak.dto.response.ReviewEligibilityResponse;
import eventHub.deepak.dto.response.ReviewResponse;
import eventHub.deepak.service.review.EventReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class EventReviewController {
    private final EventReviewService reviews;
    public EventReviewController(EventReviewService reviews) { this.reviews=reviews; }

    @GetMapping("/events/{eventId}/reviews")
    @Operation(summary="List public event reviews")
    public Page<ReviewResponse> list(@PathVariable Long eventId, Pageable pageable) { return reviews.list(eventId,pageable); }

    @GetMapping("/events/{eventId}/reviews/eligibility")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary="Check review eligibility and retrieve the customer's existing review",security=@SecurityRequirement(name="bearerAuth"))
    public ReviewEligibilityResponse eligibility(@PathVariable Long eventId,Authentication auth) { return reviews.eligibility(eventId,auth.getName()); }

    @PostMapping("/events/{eventId}/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary="Create a single review after a confirmed booking and completed event",security=@SecurityRequirement(name="bearerAuth"))
    public ResponseEntity<ReviewResponse> create(@PathVariable Long eventId,@Valid @RequestBody ReviewRequest request,Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviews.create(eventId,request,auth.getName()));
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary="Update the authenticated customer's own review",security=@SecurityRequirement(name="bearerAuth"))
    public ReviewResponse update(@PathVariable Long reviewId,@Valid @RequestBody ReviewRequest request,Authentication auth) { return reviews.update(reviewId,request,auth.getName()); }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary="Delete the authenticated customer's own review",security=@SecurityRequirement(name="bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable Long reviewId,Authentication auth) { reviews.delete(reviewId,auth.getName()); return ResponseEntity.noContent().build(); }
}
