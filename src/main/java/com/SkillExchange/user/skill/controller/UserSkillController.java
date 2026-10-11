package com.SkillExchange.user.skill.controller;

import com.SkillExchange.user.skill.dto.UserSkillRequest;
import com.SkillExchange.user.skill.dto.UserSkillResponse;
import com.SkillExchange.user.skill.service.UserSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserSkillController {

    private final UserSkillService userSkillService;
    @PostMapping("/add-teaching-skills")
    public ResponseEntity<UserSkillResponse> addTeachingSkill(
            Authentication authentication,
            @Valid @RequestBody UserSkillRequest request
    ) {

        String username = authentication.getName();

        UserSkillResponse response =
                userSkillService.addTeachingSkill(
                        username,
                        request.getSkillId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/get-teaching-skills")
    public ResponseEntity<List<UserSkillResponse>> getTeachingSkills(
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                userSkillService.getTeachingSkills(username)
        );
    }


    @DeleteMapping("/remove-teaching-skills/{skillId}")
    public ResponseEntity<Void> removeTeachingSkill(
            Authentication authentication,
            @PathVariable Long skillId
    ) {

        String username = authentication.getName();

        userSkillService.removeTeachingSkill(
                username,
                skillId
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add-learning-skills")
    public ResponseEntity<UserSkillResponse> addLearningSkill(
            Authentication authentication,
            @Valid @RequestBody UserSkillRequest request
    ) {

        String username = authentication.getName();

        UserSkillResponse response =
                userSkillService.addLearningSkill(
                        username,
                        request.getSkillId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/get-learning-skills")
    public ResponseEntity<List<UserSkillResponse>> getLearningSkills(
            Authentication authentication
    ) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                userSkillService.getLearningSkills(username)
        );
    }


    @DeleteMapping("/remove-learning-skills/{skillId}")
    public ResponseEntity<Void> removeLearningSkill(
            Authentication authentication,
            @PathVariable Long skillId
    ) {

        String username = authentication.getName();

        userSkillService.removeLearningSkill(
                username,
                skillId
        );

        return ResponseEntity.noContent().build();
    }
}