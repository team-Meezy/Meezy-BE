package com.example.meezy.bc.collaboration.meeting_report.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record FeedbackId(
        UUID value
) {

    public static FeedbackId newId(){
        return new FeedbackId(UUID.randomUUID());
    }

    public static FeedbackId of(UUID feedbackId){
        return new FeedbackId(feedbackId);
    }
}
