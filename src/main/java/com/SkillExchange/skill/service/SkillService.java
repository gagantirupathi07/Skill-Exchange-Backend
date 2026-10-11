package com.SkillExchange.skill.service;

import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.skill.dto.SkillRequest;
import com.SkillExchange.skill.dto.SkillResponse;
import com.SkillExchange.skill.model.Skill;
import com.SkillExchange.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;


    // =========================================================
    // CREATE SKILL
    // =========================================================

    @Transactional
    public SkillResponse createSkill(SkillRequest request) {

        String skillName = request.getName().trim();

        if (skillRepository.existsByNameIgnoreCase(skillName)) {
            throw new BadRequestException(
                    "Skill already exists"
            );
        }

        Skill skill = Skill.builder()
                .name(skillName)
                .category(
                        request.getCategory() != null
                                ? request.getCategory().trim()
                                : null
                )
                .description(
                        request.getDescription() != null
                                ? request.getDescription().trim()
                                : null
                )
                .isActive(true)
                .build();

        Skill savedSkill = skillRepository.save(skill);

        return mapToResponse(savedSkill);
    }


    // =========================================================
    // GET ALL ACTIVE SKILLS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkills() {

        return skillRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET SKILL BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(Long id) {

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Skill not found"
                        )
                );

        return mapToResponse(skill);
    }


    // =========================================================
    // SEARCH ACTIVE SKILLS
    // =========================================================

    @Transactional(readOnly = true)
    public List<SkillResponse> searchSkills(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException(
                    "Search name is required"
            );
        }

        return skillRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrue(
                        name.trim()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET SKILLS BY CATEGORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<SkillResponse> getSkillsByCategory(
            String category
    ) {

        if (category == null || category.trim().isEmpty()) {
            throw new BadRequestException(
                    "Category is required"
            );
        }

        return skillRepository
                .findByCategoryIgnoreCaseAndIsActiveTrue(
                        category.trim()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // UPDATE SKILL
    // =========================================================

    @Transactional
    public SkillResponse updateSkill(
            Long id,
            SkillRequest request
    ) {

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Skill not found"
                        )
                );

        String skillName = request.getName().trim();

        if (!skill.getName().equalsIgnoreCase(skillName)
                && skillRepository.existsByNameIgnoreCase(skillName)) {

            throw new BadRequestException(
                    "Skill already exists"
            );
        }

        skill.setName(skillName);

        skill.setCategory(
                request.getCategory() != null
                        ? request.getCategory().trim()
                        : null
        );

        skill.setDescription(
                request.getDescription() != null
                        ? request.getDescription().trim()
                        : null
        );

        Skill updatedSkill = skillRepository.save(skill);

        return mapToResponse(updatedSkill);
    }


    // =========================================================
    // DEACTIVATE SKILL
    // =========================================================

    @Transactional
    public void deactivateSkill(Long id) {

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Skill not found"
                        )
                );

        if (!Boolean.TRUE.equals(skill.getIsActive())) {
            throw new BadRequestException(
                    "Skill is already inactive"
            );
        }

        skill.setIsActive(false);

        skillRepository.save(skill);
    }


    // =========================================================
    // ACTIVATE SKILL
    // =========================================================

    @Transactional
    public void activateSkill(Long id) {

        Skill skill = skillRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Skill not found"
                        )
                );

        if (Boolean.TRUE.equals(skill.getIsActive())) {
            throw new BadRequestException(
                    "Skill is already active"
            );
        }

        skill.setIsActive(true);

        skillRepository.save(skill);
    }


    // =========================================================
    // GET ALL SKILLS INCLUDING INACTIVE
    // ADMIN ONLY
    // =========================================================

    @Transactional(readOnly = true)
    public List<SkillResponse> getAllSkillsForAdmin() {

        return skillRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // MAPPER
    // =========================================================

    private SkillResponse mapToResponse(Skill skill) {

        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .isActive(skill.getIsActive())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}