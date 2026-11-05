package eventHub.deepak.controller;

import eventHub.deepak.dto.request.ScreenRequest;
import eventHub.deepak.dto.response.ScreenResponse;
import eventHub.deepak.service.impl.CatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/screens")
@SecurityRequirement(name = "bearerAuth")
public class ScreenController {
    private final CatalogService service;

    public ScreenController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    public List<ScreenResponse> all() {
        return service.screens();
    }

    @PostMapping
    public ScreenResponse create(@Valid @RequestBody ScreenRequest request) {
        return service.createScreen(request);
    }

    @PutMapping("/{id}")
    public ScreenResponse update(@PathVariable Long id, @Valid @RequestBody ScreenRequest request) {
        return service.updateScreen(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteScreen(id);
    }
}
