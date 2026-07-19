package hsf302.se2033jv.project_hsf302_group2.common.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.GeneralConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.ReservationConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.SystemConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemConfig;

import java.util.List;
import java.util.Map;

public interface ConfigService {

    // Core methods
    Map<String, String> getAllConfigs();
    String getConfig(String key, String defaultValue);
    Map<String, List<SystemConfig>> getGroupedConfigs();

    void updateConfig(String configKey, String configValue, Integer updatedByUserId);
    int updateBatchConfigs(Map<String, String> configs, Integer updatedByUserId);
    void resetConfig(String configKey, Integer updatedByUserId);

    void clearCache();

    // ===== GENERAL CONFIG =====
    GeneralConfigRequest getGeneralConfig();
    void updateGeneralConfig(GeneralConfigRequest request, Integer updatedByUserId);
    Map<String, String> getGeneralConfigMap();

    // ===== SYSTEM CONFIG =====
    SystemConfigRequest getSystemConfig();
    void updateSystemConfig(SystemConfigRequest request, Integer updatedByUserId);
    Map<String, String> getSystemConfigMap();

    // ===== RESERVATION CONFIG =====
    ReservationConfigRequest getReservationConfig();
    void updateReservationConfig(ReservationConfigRequest request, Integer updatedByUserId);
    Map<String, String> getReservationConfigMap();

    // ===== GETTER METHODS (for other services) =====
    // General
    String getSiteName();
    String getSitePhone();
    String getSiteEmail();
    String getSiteAddress();
    String getSiteHours();
    String getSiteLogo();
    String getSiteFavicon();
    String getSiteDescription();

    // System
    boolean isMaintenanceMode();
    String getMaintenanceMessage();
    int getLogRetentionDays();
    String getDefaultLanguage();
    int getItemsPerPage();

    // Reservation
    long getReservationDepositAmount();
    int getReservationHoldMinutes();
    int getReservationMaxPerDay();
    int getReservationMaxAdvanceDays();
    int getReservationMinAdvanceHours();
    int getReservationMaxPartySize();
    int getReservationCancelBeforeMinutes();
}