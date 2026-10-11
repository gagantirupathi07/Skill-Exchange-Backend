package com.SkillExchange.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    private Long userId;

    private String username;

    private String firstName;

    private String lastName;

    private String bio;

    private String profilePicture;

    private int matchScore;

    private boolean twoWayMatch;

    private List<String> skillsTheyCanTeach;

    private List<String> skillsTheyWantToLearn;
}