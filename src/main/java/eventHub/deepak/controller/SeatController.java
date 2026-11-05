package eventHub.deepak.controller;

import eventHub.deepak.dto.request.SeatRequest;
import eventHub.deepak.dto.response.SeatResponse;
import eventHub.deepak.service.impl.CatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@SecurityRequirement(name = "bearerAuth")
public class SeatController {
    private final CatalogService service;

    public SeatController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    public List<SeatResponse> all(@RequestParam Long screenId) {
        return service.seats(screenId);
    }

    @PostMapping
    public SeatResponse create(@Valid @RequestBody SeatRequest request) {
        return service.createSeat(request);
    }

    @PutMapping("/{id}")
    public SeatResponse update(@PathVariable Long id, @Valid @RequestBody SeatRequest request) {
        return service.updateSeat(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteSeat(id);
    }
}
