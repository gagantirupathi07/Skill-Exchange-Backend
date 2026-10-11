package com.SkillExchange.credits.controller;

import com.SkillExchange.credits.dto.CreditBalanceResponse;
import com.SkillExchange.credits.dto.CreditTransactionResponse;
import com.SkillExchange.credits.service.CreditService;
import com.SkillExchange.credits.service.CreditTransactionService;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;
    private final CreditTransactionService creditTransactionService;
    private final UserRepository userRepository;


    /*
     * Get logged-in user's credit balance.
     *
     * GET /api/credits/balance
     */
    @GetMapping("/balance")
    public ResponseEntity<CreditBalanceResponse> getBalance(
            Authentication authentication
    ) {

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        var wallet =
                creditService.getOrCreateWallet(user);

        CreditBalanceResponse response =
                CreditBalanceResponse.builder()
                        .walletId(wallet.getId())
                        .userId(user.getId())
                        .username(user.getUsername())
                        .balance(wallet.getBalance())
                        .build();

        return ResponseEntity.ok(response);
    }


    /*
     * Get logged-in user's credit transactions.
     *
     * GET /api/credits/transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<CreditTransactionResponse>>
    getTransactions(
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                creditTransactionService
                        .getMyTransactions(username)
        );
    }
}