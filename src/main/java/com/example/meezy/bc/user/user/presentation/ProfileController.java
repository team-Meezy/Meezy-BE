package com.example.meezy.bc.user.user.presentation;

import com.example.meezy.bc.sharedkernel.validation.NotEmptyMultipartFile;
import com.example.meezy.bc.user.user.application.service.dto.request.ChangePasswordRequest;
import com.example.meezy.bc.user.user.application.service.dto.request.WithdrawRequest;
import com.example.meezy.bc.user.user.application.service.dto.response.MyProfileResponse;
import com.example.meezy.bc.user.user.application.service.profile.ChangePasswordService;
import com.example.meezy.bc.user.user.application.service.profile.QueryMyProfileService;
import com.example.meezy.bc.user.user.application.service.profile.UploadProfileImageService;
import com.example.meezy.bc.user.user.application.service.profile.WithdrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final QueryMyProfileService queryMyProfileService;
    private final ChangePasswordService changePasswordService;
    private final UploadProfileImageService uploadProfileImageService;
    private final WithdrawService withdrawService;

    @GetMapping
    public MyProfileResponse getMyProfile() {
        return queryMyProfileService.query();
    }

    @PatchMapping("/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        changePasswordService.change(request);
    }
    

    @PatchMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadProfileImage(@RequestPart @NotEmptyMultipartFile MultipartFile profileImage) {
        uploadProfileImageService.upload(profileImage);
    }

    @DeleteMapping
    public void withdraw(@Valid @RequestBody WithdrawRequest request) {
        withdrawService.withdraw(request);
    }
}
