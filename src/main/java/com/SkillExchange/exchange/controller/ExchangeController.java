package com.SkillExchange.exchange.controller;

import com.SkillExchange.exchange.dto.ExchangeCompletionResponse;
import com.SkillExchange.exchange.dto.ExchangeResponse;
import com.SkillExchange.exchange.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;


    /*
     * Get one exchange.
     *
     * GET /api/exchanges/{exchangeId}
     */
    @GetMapping("/{exchangeId}")
    public ResponseEntity<ExchangeResponse> getExchange(
            Authentication authentication,
            @PathVariable Long exchangeId
    ) {

        return ResponseEntity.ok(
                exchangeService.getExchangeById(
                        authentication.getName(),
                        exchangeId
                )
        );
    }


    /*
     * Get all exchanges for logged-in user.
     *
     * GET /api/exchanges/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<ExchangeResponse>>
    getMyExchanges(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                exchangeService.getMyExchanges(
                        authentication.getName()
                )
        );
    }


    /*
     * Get exchanges where logged-in user is teacher.
     *
     * GET /api/exchanges/teaching
     */
    @GetMapping("/teaching")
    public ResponseEntity<List<ExchangeResponse>>
    getTeachingExchanges(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                exchangeService.getTeachingExchanges(
                        authentication.getName()
                )
        );
    }


    /*
     * Get exchanges where logged-in user is learner.
     *
     * GET /api/exchanges/learning
     */
    @GetMapping("/learning")
    public ResponseEntity<List<ExchangeResponse>>
    getLearningExchanges(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                exchangeService.getLearningExchanges(
                        authentication.getName()
                )
        );
    }


    /*
     * Complete an exchange.
     *
     * ONLY THE TEACHER CAN DO THIS.
     *
     * PUT /api/exchanges/{exchangeId}/complete
     */
    @PutMapping("/{exchangeId}/complete")
    public ResponseEntity<ExchangeCompletionResponse>
    completeExchange(
            Authentication authentication,
            @PathVariable Long exchangeId
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                exchangeService.completeExchange(
                        username,
                        exchangeId
                )
        );
    }
}