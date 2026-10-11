package com.SkillExchange.credits.service;

import com.SkillExchange.credits.dto.CreditTransactionResponse;
import com.SkillExchange.credits.model.CreditTransaction;
import com.SkillExchange.credits.repository.CreditTransactionRepository;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditTransactionService {

    private final CreditTransactionRepository
            creditTransactionRepository;

    private final UserRepository userRepository;


    /*
     * Get logged-in user's credit history.
     */
    @Transactional(readOnly = true)
    public List<CreditTransactionResponse>
    getMyTransactions(String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        return creditTransactionRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*
     * Entity → DTO
     */
    private CreditTransactionResponse mapToResponse(
            CreditTransaction transaction
    ) {

        return CreditTransactionResponse.builder()

                .id(transaction.getId())

                .userId(
                        transaction
                                .getUser()
                                .getId()
                )

                .username(
                        transaction
                                .getUser()
                                .getUsername()
                )

                .exchangeId(
                        transaction
                                .getExchange()
                                .getId()
                )

                .type(
                        transaction.getType()
                )

                .amount(
                        transaction.getAmount()
                )

                .balanceAfter(
                        transaction.getBalanceAfter()
                )

                .description(
                        transaction.getDescription()
                )

                .createdAt(
                        transaction.getCreatedAt()
                )

                .build();
    }
}