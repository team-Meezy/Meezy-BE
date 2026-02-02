package com.example.meezy.bc.user.user.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record UserId(
        UUID value
) {

    public static UserId newId(){
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID userId){
        return new UserId(userId);
    }
}
