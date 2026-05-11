package com.hnust.easyweibo.backend.domain.dto.topic;

public record TopicResponse(
    String id,
    String name,
    Integer postCount
) {
}
