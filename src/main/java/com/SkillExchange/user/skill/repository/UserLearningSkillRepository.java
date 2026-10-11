package com.SkillExchange.user.skill.repository;

import com.SkillExchange.user.model.User;
import com.SkillExchange.user.skill.model.UserLearningSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLearningSkillRepository
        extends JpaRepository<UserLearningSkill, Long> {

    List<UserLearningSkill> findByUser(User user);

    Optional<UserLearningSkill> findByUserIdAndSkillId(
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

    List<UserLearningSkill> findBySkillId(Long skillId);
}