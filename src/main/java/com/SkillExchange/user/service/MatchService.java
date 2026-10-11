package com.SkillExchange.user.service;

import com.SkillExchange.user.dto.MatchResponse;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.model.UserStatus;
import com.SkillExchange.user.repository.UserRepository;
import com.SkillExchange.user.skill.model.UserLearningSkill;
import com.SkillExchange.user.skill.model.UserTeachingSkill;
import com.SkillExchange.user.skill.repository.UserLearningSkillRepository;
import com.SkillExchange.user.skill.repository.UserTeachingSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;

    private final UserTeachingSkillRepository teachingSkillRepository;

    private final UserLearningSkillRepository learningSkillRepository;


    @Transactional(readOnly = true)
    public List<MatchResponse> findMatches(String username) {

        User currentUser = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        List<UserTeachingSkill> myTeachingSkills =
                teachingSkillRepository.findByUser(currentUser);


        List<UserLearningSkill> myLearningSkills =
                learningSkillRepository.findByUser(currentUser);


        if (myLearningSkills.isEmpty()) {
            return Collections.emptyList();
        }


        Set<String> myTeachingSkillNames =
                myTeachingSkills.stream()
                        .map(skill ->
                                skill.getSkill()
                                        .getName()
                                        .toLowerCase()
                        )
                        .collect(Collectors.toSet());


        Set<String> myLearningSkillNames =
                myLearningSkills.stream()
                        .map(skill ->
                                skill.getSkill()
                                        .getName()
                                        .toLowerCase()
                        )
                        .collect(Collectors.toSet());


        Set<User> candidateUsers = new HashSet<>();

        for (UserLearningSkill learningSkill : myLearningSkills) {

            List<UserTeachingSkill> teachers =
                    teachingSkillRepository.findBySkillId(
                            learningSkill.getSkill().getId()
                    );


            for (UserTeachingSkill teacher : teachers) {

                User candidate = teacher.getUser();


                if (candidate.getId()
                        .equals(currentUser.getId())) {

                    continue;
                }


                if (candidate.getStatus() != UserStatus.ACTIVE) {

                    continue;
                }


                candidateUsers.add(candidate);
            }
        }


        List<MatchResponse> matches = new ArrayList<>();


        for (User candidate : candidateUsers) {

            List<UserTeachingSkill> candidateTeachingSkills =
                    teachingSkillRepository.findByUser(candidate);


            List<UserLearningSkill> candidateLearningSkills =
                    learningSkillRepository.findByUser(candidate);


            List<String> skillsTheyCanTeach =
                    candidateTeachingSkills.stream()
                            .map(skill ->
                                    skill.getSkill().getName()
                            )
                            .filter(skillName ->
                                    myLearningSkillNames.contains(
                                            skillName.toLowerCase()
                                    )
                            )
                            .distinct()
                            .toList();


            List<String> skillsTheyWantToLearn =
                    candidateLearningSkills.stream()
                            .map(skill ->
                                    skill.getSkill().getName()
                            )
                            .filter(skillName ->
                                    myTeachingSkillNames.contains(
                                            skillName.toLowerCase()
                                    )
                            )
                            .distinct()
                            .toList();


            double myLearningMatch =
                    myLearningSkillNames.isEmpty()
                            ? 0
                            : (double) skillsTheyCanTeach.size()
                              / myLearningSkillNames.size();


            double myTeachingMatch =
                    myTeachingSkillNames.isEmpty()
                            ? 0
                            : (double) skillsTheyWantToLearn.size()
                              / myTeachingSkillNames.size();


            int matchScore = (int) Math.round(
                    (myLearningMatch * 50)
                            +
                            (myTeachingMatch * 50)
            );


            boolean twoWayMatch =
                    !skillsTheyCanTeach.isEmpty()
                            &&
                            !skillsTheyWantToLearn.isEmpty();


            if (matchScore > 0) {

                MatchResponse response =
                        MatchResponse.builder()
                                .userId(candidate.getId())
                                .username(candidate.getUsername())
                                .firstName(candidate.getFirstName())
                                .lastName(candidate.getLastName())
                                .bio(candidate.getBio())
                                .profilePicture(
                                        candidate.getProfilePicture()
                                )
                                .matchScore(matchScore)
                                .twoWayMatch(twoWayMatch)
                                .skillsTheyCanTeach(
                                        skillsTheyCanTeach
                                )
                                .skillsTheyWantToLearn(
                                        skillsTheyWantToLearn
                                )
                                .build();

                matches.add(response);
            }
        }


        matches.sort(
                Comparator.comparingInt(
                        MatchResponse::getMatchScore
                ).reversed()
        );


        return matches;
    }
}