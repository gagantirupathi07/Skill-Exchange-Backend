package com.SkillExchange.user.controller;

import com.SkillExchange.user.dto.PublicUserResponse;
import com.SkillExchange.user.dto.UpdateProfileRequest;
import com.SkillExchange.user.dto.UserProfileResponse;
import com.SkillExchange.user.dto.UserSearchResponse;
import com.SkillExchange.user.service.ProfilePictureService;
import com.SkillExchange.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ProfilePictureService profilePictureService;


    // ==========================================
    // MY PROFILE
    // ==========================================

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                userService.getMyProfile(username)
        );
    }


    // ==========================================
    // UPDATE MY PROFILE
    // ==========================================

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                userService.updateMyProfile(
                        username,
                        request
                )
        );
    }


    // ==========================================
    // UPLOAD PROFILE PICTURE
    // ==========================================

    @PostMapping(
            value = "/profile/picture",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                profilePictureService.uploadProfilePicture(
                        username,
                        file
                )
        );
    }


    // ==========================================
    // DELETE PROFILE PICTURE
    // ==========================================

    @DeleteMapping("/profile/picture")
    public ResponseEntity<UserProfileResponse> deleteProfilePicture(
            Authentication authentication
    ) {

        String username =
                authentication.getName();

        return ResponseEntity.ok(
                profilePictureService.deleteProfilePicture(
                        username
                )
        );
    }


    // ==========================================
    // SEARCH USERS BY TEACHING SKILL
    // ==========================================

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam String skill,
            Authentication authentication
    ) {

        String currentUsername =
                authentication.getName();

        return ResponseEntity.ok(
                userService.searchUsersByTeachingSkill(
                        skill,
                        currentUsername
                )
        );
    }


    // ==========================================
    // PUBLIC USER PROFILE
    // ==========================================

    @GetMapping("/{id}/public-profile")
    public ResponseEntity<PublicUserResponse> getPublicProfile(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                userService.getPublicProfile(id)
        );
    }
}