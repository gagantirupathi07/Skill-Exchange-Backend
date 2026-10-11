package com.SkillExchange.skill.controller;

import com.SkillExchange.skill.dto.SkillRequest;
import com.SkillExchange.skill.dto.SkillResponse;
import com.SkillExchange.skill.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;


    // =========================================================
    // CREATE
    // ADMIN ONLY
    // =========================================================

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> createSkill(
            @Valid @RequestBody SkillRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        skillService.createSkill(request)
                );
    }


    // =========================================================
    // GET ALL ACTIVE SKILLS
    // =========================================================

    @GetMapping("/get")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {

        return ResponseEntity.ok(
                skillService.getAllSkills()
        );
    }


    // =========================================================
    // GET ALL SKILLS
    // INCLUDING INACTIVE
    // ADMIN ONLY
    // =========================================================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SkillResponse>> getAllSkillsForAdmin() {

        return ResponseEntity.ok(
                skillService.getAllSkillsForAdmin()
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @GetMapping("/byId/{id}")
    public ResponseEntity<SkillResponse> getSkillById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                skillService.getSkillById(id)
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> searchSkills(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                skillService.searchSkills(name)
        );
    }


    // =========================================================
    // GET BY CATEGORY
    // =========================================================

    @GetMapping("/category")
    public ResponseEntity<List<SkillResponse>> getSkillsByCategory(
            @RequestParam String name
    ) {

        return ResponseEntity.ok(
                skillService.getSkillsByCategory(name)
        );
    }


    // =========================================================
    // UPDATE
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request
    ) {

        return ResponseEntity.ok(
                skillService.updateSkill(id, request)
        );
    }


    // =========================================================
    // DEACTIVATE
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/deactivate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateSkill(
            @PathVariable Long id
    ) {

        skillService.deactivateSkill(id);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // ACTIVATE
    // ADMIN ONLY
    // =========================================================

    @PutMapping("/activate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateSkill(
            @PathVariable Long id
    ) {

        skillService.activateSkill(id);

        return ResponseEntity.noContent().build();
    }
}