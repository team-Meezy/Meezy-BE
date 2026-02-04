package com.example.meezy.bc.user.user.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ProfileImageUploadFailedException extends CustomException {

    public ProfileImageUploadFailedException(){
        super(ErrorCode.PROFILE_IMAGE_UPLOAD_FAILED);
    }
}
