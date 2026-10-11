package com.SkillExchange.user.controller;

import com.SkillExchange.user.dto.MatchResponse;
import com.SkillExchange.user.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchResponse>> findMatches(
            Authentication authentication
    ) {
        String username = authentication.getName();

        List<MatchResponse> matches =
                matchService.findMatches(username);

        return ResponseEntity.ok(matches);
    }
}