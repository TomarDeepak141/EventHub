package eventHub.deepak.controller;

import eventHub.deepak.dto.request.SeatCategoryRequest;
import eventHub.deepak.dto.response.SeatCategoryResponse;
import eventHub.deepak.service.impl.CatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seat-categories")
@SecurityRequirement(name = "bearerAuth")
public class SeatCategoryController {
    private final CatalogService service;

    public SeatCategoryController(CatalogService s) {
        service = s;
    }

    @GetMapping
    public List<SeatCategoryResponse> all() {
        return service.categories();
    }

    @PostMapping
    public SeatCategoryResponse create(@Valid @RequestBody SeatCategoryRequest r) {
        return service.createCategory(r);
    }

    @PutMapping("/{id}")
    public SeatCategoryResponse update(@PathVariable Long id, @Valid @RequestBody SeatCategoryRequest r) {
        return service.updateCategory(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteCategory(id);
    }
}
