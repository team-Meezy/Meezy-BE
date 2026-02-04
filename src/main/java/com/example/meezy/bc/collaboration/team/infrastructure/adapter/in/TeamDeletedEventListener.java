package com.example.meezy.bc.collaboration.team.infrastructure.adapter.in;

import com.example.meezy.bc.collaboration.team.domain.event.TeamDeletedEvent;
import com.example.meezy.bc.sharedkernel.file.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamDeletedEventListener {

    private final FileStoragePort fileStoragePort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTeamDeleted(TeamDeletedEvent event) {
        if (event.serverImageUrl() == null || event.serverImageUrl().isBlank()) {
            return;
        }

        try {
            fileStoragePort.deleteByKey(event.serverImageUrl());
            log.info("팀 이미지 삭제 완료: {}", event.serverImageUrl());
        } catch (Exception e) {
            log.warn("팀 이미지 삭제 실패: {}", event.serverImageUrl(), e);
        }
    }
}
