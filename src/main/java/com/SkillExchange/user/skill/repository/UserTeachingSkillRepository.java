package com.SkillExchange.user.skill.repository;

import com.SkillExchange.skill.model.Skill;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.skill.model.UserTeachingSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserTeachingSkillRepository
        extends JpaRepository<UserTeachingSkill, Long> {

    List<UserTeachingSkill> findByUser(User user);

    Optional<UserTeachingSkill> findByUserIdAndSkillId(
            Long userId,
            Long skillId
    );

    boolean existsByUserIdAndSkillId(
            Long userId,
            Long skillId
    );

    void deleteByUserIdAndSkillId(
            Long userId,
            Long skillId
    );

    List<UserTeachingSkill> findBySkillId(Long skillId);

    List<UserTeachingSkill> findBySkillNameIgnoreCase(
            String skillName
    );

    boolean existsByUserAndSkill(User receiver, Skill requestedSkill);
}