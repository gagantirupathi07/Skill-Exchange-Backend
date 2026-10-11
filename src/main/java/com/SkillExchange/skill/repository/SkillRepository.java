package com.SkillExchange.skill.repository;

import com.SkillExchange.skill.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Skill> findByNameContainingIgnoreCase(String name);

    List<Skill> findByCategoryIgnoreCase(String category);

    List<Skill> findByIsActiveTrue();

    List<Skill> findByCategoryIgnoreCaseAndIsActiveTrue(String category);

    List<Skill> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    long countByIsActive(Boolean isActive);
}