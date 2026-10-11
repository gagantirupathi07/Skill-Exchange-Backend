package com.SkillExchange.dashboard.user.controller;

import com.SkillExchange.dashboard.user.dto.DashboardResponse;
import com.SkillExchange.dashboard.user.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                dashboardService.getDashboard(username)
        );
    }
}
