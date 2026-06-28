package hsf302.se2033jv.project_hsf302_group2.admin.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.ChartDataResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.DashboardStatsResponse;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.RoleStatsDTO;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.response.SystemLogDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();

    ChartDataResponse getChartData();

    long getTotalUsers();

    long getNewUsersToday();

    long getActiveUsers();

    long getLockedUsers();

    List<RoleStatsDTO> getRoleStats();

    List<SystemLogDTO> getRecentLogs();
}
