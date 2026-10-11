package com.SkillExchange.exchange.controller;

import com.SkillExchange.exchange.dto.ExchangeSessionRequest;
import com.SkillExchange.exchange.dto.ExchangeSessionResponse;
import com.SkillExchange.exchange.service.ExchangeSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ExchangeSessionController {

    private final ExchangeSessionService exchangeSessionService;


    @PostMapping("/{exchangeId}/sessions")
    public ResponseEntity<ExchangeSessionResponse> createSession(
            Authentication authentication,
            @PathVariable Long exchangeId,
            @Valid @RequestBody ExchangeSessionRequest request
    ) {

        String username =
                authentication.getName();

        ExchangeSessionResponse response =
                exchangeSessionService.createSession(
                        username,
                        exchangeId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/{exchangeId}/sessions")
    public ResponseEntity<List<ExchangeSessionResponse>> getSessions(
            Authentication authentication,
            @PathVariable Long exchangeId
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeSessionService.getSessions(
                        username,
                        exchangeId
                )
        );
    }


    /*
     * Teacher OR learner confirms
     * that the session was completed.
     */
    @PutMapping("/{exchangeId}/sessions/{sessionId}/complete")
    public ResponseEntity<ExchangeSessionResponse> completeSession(
            Authentication authentication,
            @PathVariable Long exchangeId,
            @PathVariable Long sessionId
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeSessionService.completeSession(
                        username,
                        exchangeId,
                        sessionId
                )
        );
    }
}