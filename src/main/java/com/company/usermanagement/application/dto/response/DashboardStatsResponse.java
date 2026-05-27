package com.company.usermanagement.application.dto.response;

import java.util.List;
import java.util.Map;

public record DashboardStatsResponse(
    long               totalUsers,
    long               activeToday,
    long               failedLoginsToday,
    long               activeTokens,
    List<DayCount>     loginsByDay,
    Map<String, Long>  usersByRole
) {
    public record DayCount(String day, long value) {}
}
