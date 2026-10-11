package com.SkillExchange.user.skill.service;

import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.skill.model.Skill;
import com.SkillExchange.skill.repository.SkillRepository;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import com.SkillExchange.user.skill.dto.UserSkillResponse;
import com.SkillExchange.user.skill.model.UserLearningSkill;
import com.SkillExchange.user.skill.model.UserTeachingSkill;
import com.SkillExchange.user.skill.repository.UserLearningSkillRepository;
import com.SkillExchange.user.skill.repository.UserTeachingSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSkillService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    private final UserTeachingSkillRepository teachingSkillRepository;
    private final UserLearningSkillRepository learningSkillRepository;


    // =========================
    // TEACHING SKILLS
    // =========================

    @Transactional
    public UserSkillResponse addTeachingSkill(
            String username,
            Long skillId
    ) {

        User user = getUser(username);
        Skill skill = getSkill(skillId);

        if (teachingSkillRepository.existsByUserIdAndSkillId(
                user.getId(),
                skillId
        )) {

            throw new BadRequestException(
                    "Skill is already in teaching skills"
            );
        }

        UserTeachingSkill userTeachingSkill =
                UserTeachingSkill.builder()
                        .user(user)
                        .skill(skill)
                        .build();

        UserTeachingSkill saved =
                teachingSkillRepository.save(userTeachingSkill);

        return mapTeachingSkill(saved);
    }


    @Transactional(readOnly = true)
    public List<UserSkillResponse> getTeachingSkills(
            String username
    ) {

        User user = getUser(username);

        return teachingSkillRepository
                .findByUser(user)
                .stream()
                .map(this::mapTeachingSkill)
                .toList();
    }


    @Transactional
    public void removeTeachingSkill(
            String username,
            Long skillId
    ) {

        User user = getUser(username);

        if (!teachingSkillRepository.existsByUserIdAndSkillId(
                user.getId(),
                skillId
        )) {

            throw new BadRequestException(
                    "Teaching skill not found"
            );
        }

        teachingSkillRepository.deleteByUserIdAndSkillId(
                user.getId(),
                skillId
        );
    }


    // =========================
    // LEARNING SKILLS
    // =========================

    @Transactional
    public UserSkillResponse addLearningSkill(
            String username,
            Long skillId
    ) {

        User user = getUser(username);
        Skill skill = getSkill(skillId);

        if (learningSkillRepository.existsByUserIdAndSkillId(
                user.getId(),
                skillId
        )) {

            throw new BadRequestException(
                    "Skill is already in learning skills"
            );
        }

        UserLearningSkill userLearningSkill =
                UserLearningSkill.builder()
                        .user(user)
                        .skill(skill)
                        .build();

        UserLearningSkill saved =
                learningSkillRepository.save(userLearningSkill);

        return mapLearningSkill(saved);
    }


    @Transactional(readOnly = true)
    public List<UserSkillResponse> getLearningSkills(
            String username
    ) {

        User user = getUser(username);

        return learningSkillRepository
                .findByUser(user)
                .stream()
                .map(this::mapLearningSkill)
                .toList();
    }


    @Transactional
    public void removeLearningSkill(
            String username,
            Long skillId
    ) {

        User user = getUser(username);

        if (!learningSkillRepository.existsByUserIdAndSkillId(
                user.getId(),
                skillId
        )) {

            throw new BadRequestException(
                    "Learning skill not found"
            );
        }

        learningSkillRepository.deleteByUserIdAndSkillId(
                user.getId(),
                skillId
        );
    }


    // =========================
    // HELPER METHODS
    // =========================

    private User getUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException(
                                "User not found"
                        )
                );
    }


    private Skill getSkill(Long skillId) {

        return skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Skill not found"
                        )
                );
    }


    private UserSkillResponse mapTeachingSkill(
            UserTeachingSkill userTeachingSkill
    ) {

        Skill skill = userTeachingSkill.getSkill();

        return UserSkillResponse.builder()
                .id(userTeachingSkill.getId())
                .skillId(skill.getId())
                .skillName(skill.getName())
                .category(skill.getCategory())
                .build();
    }


    private UserSkillResponse mapLearningSkill(
            UserLearningSkill userLearningSkill
    ) {

        Skill skill = userLearningSkill.getSkill();

        return UserSkillResponse.builder()
                .id(userLearningSkill.getId())
                .skillId(skill.getId())
                .skillName(skill.getName())
                .category(skill.getCategory())
                .build();
    }
}