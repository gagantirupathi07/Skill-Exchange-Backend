package com.SkillExchange.credits.service;

import com.SkillExchange.credits.model.CreditTransaction;
import com.SkillExchange.credits.model.CreditTransactionType;
import com.SkillExchange.credits.model.CreditWallet;
import com.SkillExchange.credits.repository.CreditTransactionRepository;
import com.SkillExchange.credits.repository.CreditWalletRepository;
import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CreditService {

    private static final BigDecimal CREDITS_PER_HOUR =
            new BigDecimal("5.00");

    private static final BigDecimal INITIAL_CREDITS =
            new BigDecimal("20.00");

    private final CreditWalletRepository creditWalletRepository;

    private final CreditTransactionRepository creditTransactionRepository;

    private final UserRepository userRepository;


    /*
     * Get existing wallet.
     *
     * If wallet does not exist,
     * create one with 0 credits.
     */
    @Transactional
    public CreditWallet getOrCreateWallet(User user) {

        return creditWalletRepository
                .findByUser(user)
                .orElseGet(() -> {

                    CreditWallet wallet =
                            CreditWallet.builder()
                                    .user(user)
                                    .balance(BigDecimal.ZERO)
                                    .build();

                    return creditWalletRepository.save(wallet);
                });
    }


    /*
     * Get wallet using username.
     */
    @Transactional
    public CreditWallet getOrCreateWallet(String username) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                )
                        );

        return getOrCreateWallet(user);
    }


    /*
     * Give initial credits to a newly
     * activated user.
     */
    @Transactional
    public void initializeCredits(User user) {

        if (creditWalletRepository.existsByUser(user)) {
            return;
        }

        CreditWallet wallet =
                CreditWallet.builder()
                        .user(user)
                        .balance(INITIAL_CREDITS)
                        .build();

        creditWalletRepository.save(wallet);
    }


    /*
     * Calculate credits from session duration.
     *
     * 60 minutes = 5.00 credits
     * 30 minutes = 2.50 credits
     * 90 minutes = 7.50 credits
     */
    public BigDecimal calculateCredits(
            Integer durationMinutes
    ) {

        if (durationMinutes == null ||
                durationMinutes <= 0) {

            throw new BadRequestException(
                    "Session duration must be greater than zero"
            );
        }

        BigDecimal minutes =
                BigDecimal.valueOf(durationMinutes);

        return minutes
                .multiply(CREDITS_PER_HOUR)
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    /*
     * Transfer credits:
     *
     * learner → teacher
     *
     * This method is transactional.
     */
    @Transactional
    public void transferCredits(
            Exchange exchange,
            BigDecimal amount
    ) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Credit amount must be greater than zero"
            );
        }

        User learner =
                exchange.getLearner();

        User teacher =
                exchange.getTeacher();


        /*
         * Prevent duplicate transfer.
         */
        boolean alreadyTransferred =
                creditTransactionRepository
                        .existsByExchangeAndType(
                                exchange,
                                CreditTransactionType.SPEND
                        );

        if (alreadyTransferred) {

            throw new BadRequestException(
                    "Credits have already been transferred for this exchange"
            );
        }


        /*
         * Get wallets.
         */
        CreditWallet learnerWallet =
                getOrCreateWallet(learner);

        CreditWallet teacherWallet =
                getOrCreateWallet(teacher);


        /*
         * Check learner balance.
         */
        if (learnerWallet
                .getBalance()
                .compareTo(amount) < 0) {

            throw new BadRequestException(
                    "Learner does not have enough credits"
            );
        }


        /*
         * Deduct credits from learner.
         */
        BigDecimal learnerNewBalance =
                learnerWallet
                        .getBalance()
                        .subtract(amount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        learnerWallet.setBalance(
                learnerNewBalance
        );


        /*
         * Add credits to teacher.
         */
        BigDecimal teacherNewBalance =
                teacherWallet
                        .getBalance()
                        .add(amount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        teacherWallet.setBalance(
                teacherNewBalance
        );


        /*
         * Save wallets.
         */
        creditWalletRepository.save(
                learnerWallet
        );

        creditWalletRepository.save(
                teacherWallet
        );


        /*
         * Learner transaction.
         */
        CreditTransaction learnerTransaction =
                CreditTransaction.builder()
                        .user(learner)
                        .exchange(exchange)
                        .type(
                                CreditTransactionType.SPEND
                        )
                        .amount(amount)
                        .balanceAfter(
                                learnerNewBalance
                        )
                        .description(
                                "Credits spent for skill exchange #" +
                                        exchange.getId()
                        )
                        .build();


        /*
         * Teacher transaction.
         */
        CreditTransaction teacherTransaction =
                CreditTransaction.builder()
                        .user(teacher)
                        .exchange(exchange)
                        .type(
                                CreditTransactionType.EARN
                        )
                        .amount(amount)
                        .balanceAfter(
                                teacherNewBalance
                        )
                        .description(
                                "Credits earned from skill exchange #" +
                                        exchange.getId()
                        )
                        .build();


        /*
         * Save transactions.
         */
        creditTransactionRepository.save(
                learnerTransaction
        );

        creditTransactionRepository.save(
                teacherTransaction
        );
    }
}