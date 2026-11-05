package eventHub.deepak.controller;

import eventHub.deepak.dto.response.AdminStatisticsResponse;
import eventHub.deepak.dto.response.OrganizerStatisticsResponse;
import eventHub.deepak.service.statistics.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatisticsController {
    private final StatisticsService statistics;
    public StatisticsController(StatisticsService statistics) { this.statistics=statistics; }

    @GetMapping("/organizer/statistics")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary="Database-backed statistics for the authenticated organizer's own events",security=@SecurityRequirement(name="bearerAuth"))
    public OrganizerStatisticsResponse organizer(Pageable pageable,Authentication auth) { return statistics.organizer(auth.getName(),pageable); }

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary="Database-backed platform statistics for administrators",security=@SecurityRequirement(name="bearerAuth"))
    public AdminStatisticsResponse admin(Authentication auth) { return statistics.admin(auth.getName()); }
}
