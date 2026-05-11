package com.hnust.easyweibo.backend.domain.dto.admin;

public record AdminOverviewResponse(
    Integer usersCount,
    Integer postsCount,
    Integer commentsCount,
    Integer openReportsCount,
    Long viewsCount
) {
}
