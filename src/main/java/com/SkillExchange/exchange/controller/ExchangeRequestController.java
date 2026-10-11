package com.SkillExchange.exchange.controller;

import com.SkillExchange.exchange.dto.ExchangeRequestRequest;
import com.SkillExchange.exchange.dto.ExchangeRequestResponse;
import com.SkillExchange.exchange.service.ExchangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-requests")
@RequiredArgsConstructor
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;
    @PostMapping
    public ResponseEntity<ExchangeRequestResponse> sendRequest(
            Authentication authentication,
            @Valid @RequestBody ExchangeRequestRequest request
    ) {

        String username =
                authentication.getName();

        ExchangeRequestResponse response =
                exchangeRequestService.sendRequest(
                        username,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/sent")
    public ResponseEntity<List<ExchangeRequestResponse>>
    getSentRequests(
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeRequestService
                        .getSentRequests(username)
        );
    }

    @GetMapping("/received")
    public ResponseEntity<List<ExchangeRequestResponse>>
    getReceivedRequests(
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeRequestService
                        .getReceivedRequests(username)
        );
    }
    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ExchangeRequestResponse>
    acceptRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeRequestService
                        .acceptRequest(
                                username,
                                requestId
                        )
        );
    }
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ExchangeRequestResponse>
    rejectRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeRequestService
                        .rejectRequest(
                                username,
                                requestId
                        )
        );
    }

    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<ExchangeRequestResponse>
    cancelRequest(
            Authentication authentication,
            @PathVariable Long requestId
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeRequestService
                        .cancelRequest(
                                username,
                                requestId
                        )
        );
    }
}