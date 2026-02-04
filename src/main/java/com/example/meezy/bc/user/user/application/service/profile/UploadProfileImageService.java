package com.example.meezy.bc.user.user.application.service.profile;

import com.example.meezy.bc.sharedkernel.file.FileStoragePort;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.application.service.exception.ProfileImageUploadFailedException;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadProfileImageService {

    private final UserRepository userRepository;
    private final CurrentUserQuery currentUserQuery;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public void upload(MultipartFile profileImage) {
        User user = userRepository.findByUserId(currentUserQuery.currentUser().userId())
                .orElseThrow(UserNotFoundException::new);

        String oldImageUrl = user.getProfileImageUrl();
        String newImageUrl = fileStoragePort.upload(profileImage);

        try {
            user.uploadProfileImage(newImageUrl);
            userRepository.save(user);
        } catch (Exception e) {
            deleteImage(newImageUrl);
            throw new ProfileImageUploadFailedException();
        }

        deleteImage(oldImageUrl);
    }

    private void deleteImage(String imageUrl) {
        if (imageUrl == null) return;
        try {
            fileStoragePort.deleteByKey(imageUrl);
        } catch (Exception ignored) {
        }
    }
}
