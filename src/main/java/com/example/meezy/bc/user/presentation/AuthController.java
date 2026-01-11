package com.example.meezy.bc.user.presentation;

import com.example.meezy.bc.user.application.service.LocalLoginService;
import com.example.meezy.bc.user.application.service.LocalSignupService;
import com.example.meezy.bc.user.application.service.ProfileSetupService;
import com.example.meezy.bc.user.application.service.ReissueService;
import com.example.meezy.bc.user.application.service.dto.request.LoginRequest;
import com.example.meezy.bc.user.application.service.dto.request.SignupRequest;
import com.example.meezy.bc.user.application.service.dto.request.TokenReissueRequest;
import com.example.meezy.bc.user.application.service.dto.request.UserProfileSetupRequest;
import com.example.meezy.bc.user.application.service.dto.response.ProfileSetupResponse;
import com.example.meezy.bc.user.application.service.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final LocalSignupService localSignupService;
    private final LocalLoginService localLoginService;
    private final ReissueService reissueService;
    private final ProfileSetupService profileSetupService;


    @PostMapping("/auth/signup")
    public void signup(@Valid @RequestBody SignupRequest request) {
        localSignupService.signup(request);
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return localLoginService.login(request);
    }

    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody TokenReissueRequest request) {
        return reissueService.reissue(request);
    }

    @PostMapping("/profile/setup")
    public ResponseEntity<ProfileSetupResponse> setup(@Valid @RequestBody UserProfileSetupRequest request) {
        return ResponseEntity.ok(profileSetupService.setup(request));
    }
}
