package com.SkillExchange.dashboard.admin.controller;

import com.SkillExchange.exchange.dto.ExchangeResponse;
import com.SkillExchange.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class AdminExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExchangeResponse>> getAllExchanges() {

        return ResponseEntity.ok(
                exchangeService.getAllExchangesForAdmin()
        );
    }
}