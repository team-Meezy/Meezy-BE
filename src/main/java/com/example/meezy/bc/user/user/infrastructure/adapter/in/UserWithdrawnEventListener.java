package com.example.meezy.bc.user.user.infrastructure.adapter.in;

import com.example.meezy.bc.sharedkernel.file.FileStoragePort;
import com.example.meezy.bc.user.user.domain.event.UserWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawnEventListener {

    private final FileStoragePort fileStoragePort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserWithdrawn(UserWithdrawnEvent event) {
        if (event.profileImageUrl() == null || event.profileImageUrl().isBlank()) {
            return;
        }

        try {
            fileStoragePort.deleteByKey(event.profileImageUrl());
            log.info("프로필 이미지 삭제 완료: {}", event.profileImageUrl());
        } catch (Exception e) {
            log.warn("프로필 이미지 삭제 실패: {}", event.profileImageUrl(), e);
        }
    }
}
