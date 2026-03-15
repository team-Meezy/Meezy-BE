package com.example.meezy.bc.collaboration.team.domain.event;

import java.util.UUID;

public record TeamMemberLeftEvent(UUID teamId, UUID userId) {
}
