
package com.SkillExchange.user.service;

import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.user.dto.UserProfileResponse;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfilePictureService {

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final String CLOUDINARY_FOLDER =
            "skill-exchange/profile-pictures";

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
            );

    @Transactional
    public UserProfileResponse uploadProfilePicture(
            String username,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Please select a profile picture"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(
                    "Profile picture must not exceed 5 MB"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT))) {

            throw new BadRequestException(
                    "Only JPG, PNG, and WEBP images are allowed"
            );
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", CLOUDINARY_FOLDER,
                            "resource_type", "image"
                    )
            );

            String imageUrl =
                    result.get("secure_url").toString();

            String publicId =
                    result.get("public_id").toString();

            String oldPublicId =
                    user.getProfilePicturePublicId();

            user.setProfilePicture(imageUrl);
            user.setProfilePicturePublicId(publicId);

            User savedUser = userRepository.save(user);

            // Delete the old image after the replacement is saved.
            if (oldPublicId != null
                    && !oldPublicId.isBlank()
                    && !oldPublicId.equals(publicId)) {

                try {
                    cloudinary.uploader().destroy(
                            oldPublicId,
                            ObjectUtils.asMap(
                                    "resource_type", "image"
                            )
                    );
                } catch (Exception ignored) {
                    // Cloud cleanup can be retried later.
                }
            }

            return mapToProfileResponse(savedUser);

        } catch (IOException exception) {
            throw new BadRequestException(
                    "Failed to upload profile picture"
            );
        }
    }

    @Transactional
    public UserProfileResponse deleteProfilePicture(
            String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        String publicId = user.getProfilePicturePublicId();

        user.setProfilePicture(null);
        user.setProfilePicturePublicId(null);

        User savedUser = userRepository.save(user);

        if (publicId != null && !publicId.isBlank()) {
            try {
                cloudinary.uploader().destroy(
                        publicId,
                        ObjectUtils.asMap(
                                "resource_type", "image"
                        )
                );
            } catch (Exception ignored) {
                // The database reference is cleared. Cloud cleanup
                // may need to be retried if the request fails.
            }
        }

        return mapToProfileResponse(savedUser);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole())
                .build();
    }
}
