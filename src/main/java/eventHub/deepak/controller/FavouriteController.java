package eventHub.deepak.controller;

import eventHub.deepak.dto.response.FavouriteResponse;
import eventHub.deepak.service.favourite.FavouriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
@PreAuthorize("hasRole('CUSTOMER')")
@SecurityRequirement(name="bearerAuth")
public class FavouriteController {
    private final FavouriteService favourites;
    public FavouriteController(FavouriteService favourites) { this.favourites=favourites; }

    @GetMapping
    @Operation(summary="List the authenticated customer's favourite events")
    public Page<FavouriteResponse> list(Pageable pageable,Authentication auth) { return favourites.list(auth.getName(),pageable); }

    @PutMapping("/{eventId}")
    @Operation(summary="Add an event to the customer's wishlist; repeated adds are idempotent")
    public ResponseEntity<FavouriteResponse> add(@PathVariable Long eventId,Authentication auth) {
        return ResponseEntity.ok(favourites.add(eventId,auth.getName()));
    }

    @DeleteMapping("/{eventId}")
    @Operation(summary="Remove an event from the customer's wishlist")
    public ResponseEntity<Void> remove(@PathVariable Long eventId,Authentication auth) { favourites.remove(eventId,auth.getName()); return ResponseEntity.noContent().build(); }
}
