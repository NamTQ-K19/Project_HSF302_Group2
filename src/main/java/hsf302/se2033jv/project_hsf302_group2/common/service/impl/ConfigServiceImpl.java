package hsf302.se2033jv.project_hsf302_group2.common.service.impl;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.GeneralConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.ReservationConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.request.SystemConfigRequest;
import hsf302.se2033jv.project_hsf302_group2.common.entity.SystemConfig;
import hsf302.se2033jv.project_hsf302_group2.common.repository.SystemConfigRepository;
import hsf302.se2033jv.project_hsf302_group2.common.service.interfaces.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigRepository systemConfigRepository;

    // CORE METHODS
    @Override
    @Cacheable(value = "systemConfigs", key = "'all'")
    public Map<String, String> getAllConfigs() {
        log.info("Loading all system configs from database");
        List<SystemConfig> configs = systemConfigRepository.findByIsActiveTrue();
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        return configMap;
    }

    @Override
    public String getConfig(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    public Map<String, List<SystemConfig>> getGroupedConfigs() {
        List<SystemConfig> allConfigs = systemConfigRepository.findByIsActiveTrue();
        Map<String, List<SystemConfig>> groupedConfigs = new LinkedHashMap<>();
        for (SystemConfig config : allConfigs) {
            String group = config.getConfigGroup() != null ? config.getConfigGroup() : "general";
            groupedConfigs.computeIfAbsent(group, k -> new ArrayList<>()).add(config);
        }
        return groupedConfigs;
    }

    @Override
    @Transactional
    public void updateConfig(String configKey, String configValue) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình: " + configKey));
        config.setConfigValue(configValue);
        systemConfigRepository.save(config);
        clearCache();
        log.info("Config updated: {} = {}", configKey, configValue);
    }

    @Override
    @Transactional
    public int updateBatchConfigs(Map<String, String> configs) {
        int updatedCount = 0;
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Bỏ qua các param không phải config
            if (key.startsWith("_") || key.equals("submit")) {
                continue;
            }

            SystemConfig config = systemConfigRepository.findByConfigKey(key).orElse(null);
            if (config != null) {
                config.setConfigValue(value);
                systemConfigRepository.save(config);
                updatedCount++;
            }
        }
        clearCache();
        log.info("Batch updated {} configs", updatedCount);
        return updatedCount;
    }

    @Override
    @Transactional
    public void resetConfig(String configKey) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình: " + configKey));

        String defaultValue = getDefaultValue(configKey);
        config.setConfigValue(defaultValue);
        systemConfigRepository.save(config);
        clearCache();
        log.info("Config reset: {} = {}", configKey, defaultValue);
    }

    @Override
    @CacheEvict(value = "systemConfigs", allEntries = true)
    public void clearCache() {
        log.info("System config cache cleared");
    }

    // GENERAL CONFIG
    @Override
    public GeneralConfigRequest getGeneralConfig() {
        GeneralConfigRequest request = new GeneralConfigRequest();
        request.setSiteName(getSiteName());
        request.setSitePhone(getSitePhone());
        request.setSiteEmail(getSiteEmail());
        request.setSiteAddress(getSiteAddress());
        request.setSiteHours(getSiteHours());
        request.setSiteLogo(getSiteLogo());
        request.setSiteFavicon(getSiteFavicon());
        request.setSiteDescription(getSiteDescription());
        return request;
    }

    @Override
    @Transactional
    public void updateGeneralConfig(GeneralConfigRequest request) {
        Map<String, String> configs = new LinkedHashMap<>();
        configs.put("site_name", request.getSiteName());
        configs.put("site_phone", request.getSitePhone());
        configs.put("site_email", request.getSiteEmail());
        configs.put("site_address", request.getSiteAddress());
        configs.put("site_hours", request.getSiteHours());
        configs.put("site_logo", request.getSiteLogo());
        configs.put("site_favicon", request.getSiteFavicon());
        configs.put("site_description", request.getSiteDescription());

        updateBatchConfigs(configs);
        log.info("General config updated");
    }

    @Override
    public Map<String, String> getGeneralConfigMap() {
        Map<String, String> configMap = new LinkedHashMap<>();
        configMap.put("site_name", getSiteName());
        configMap.put("site_phone", getSitePhone());
        configMap.put("site_email", getSiteEmail());
        configMap.put("site_address", getSiteAddress());
        configMap.put("site_hours", getSiteHours());
        configMap.put("site_logo", getSiteLogo());
        configMap.put("site_favicon", getSiteFavicon());
        configMap.put("site_description", getSiteDescription());
        return configMap;
    }

    // SYSTEM CONFIG
    @Override
    public SystemConfigRequest getSystemConfig() {
        SystemConfigRequest request = new SystemConfigRequest();
        request.setMaintenanceMode(isMaintenanceMode());
        request.setMaintenanceMessage(getMaintenanceMessage());
        request.setLogRetentionDays(getLogRetentionDays());
        request.setDefaultLanguage(getDefaultLanguage());
        request.setItemsPerPage(getItemsPerPage());
        return request;
    }

    @Override
    @Transactional
    public void updateSystemConfig(SystemConfigRequest request) {
        Map<String, String> configs = new LinkedHashMap<>();
        configs.put("maintenance_mode", String.valueOf(request.getMaintenanceMode()));
        configs.put("maintenance_message", request.getMaintenanceMessage());
        configs.put("log_retention_days", String.valueOf(request.getLogRetentionDays()));
        configs.put("default_language", request.getDefaultLanguage());
        configs.put("items_per_page", String.valueOf(request.getItemsPerPage()));

        updateBatchConfigs(configs);
        log.info("System config updated");
    }

    @Override
    public Map<String, String> getSystemConfigMap() {
        Map<String, String> configMap = new LinkedHashMap<>();
        configMap.put("maintenance_mode", String.valueOf(isMaintenanceMode()));
        configMap.put("maintenance_message", getMaintenanceMessage());
        configMap.put("log_retention_days", String.valueOf(getLogRetentionDays()));
        configMap.put("default_language", getDefaultLanguage());
        configMap.put("items_per_page", String.valueOf(getItemsPerPage()));
        return configMap;
    }

    // =============================================================
    // RESERVATION CONFIG
    // =============================================================

    @Override
    public ReservationConfigRequest getReservationConfig() {
        ReservationConfigRequest request = new ReservationConfigRequest();
        request.setDepositAmount(getReservationDepositAmount());
        request.setHoldMinutes(getReservationHoldMinutes());
        request.setMaxPerDay(getReservationMaxPerDay());
        request.setMaxAdvanceDays(getReservationMaxAdvanceDays());
        request.setMinAdvanceHours(getReservationMinAdvanceHours());
        request.setMaxPartySize(getReservationMaxPartySize());
        request.setCancelBeforeMinutes(getReservationCancelBeforeMinutes());
        return request;
    }

    @Override
    @Transactional
    public void updateReservationConfig(ReservationConfigRequest request) {
        Map<String, String> configs = new LinkedHashMap<>();
        configs.put("reservation_deposit_amount", String.valueOf(request.getDepositAmount()));
        configs.put("reservation_hold_minutes", String.valueOf(request.getHoldMinutes()));
        configs.put("reservation_max_per_day", String.valueOf(request.getMaxPerDay()));
        configs.put("reservation_max_advance_days", String.valueOf(request.getMaxAdvanceDays()));
        configs.put("reservation_min_advance_hours", String.valueOf(request.getMinAdvanceHours()));
        configs.put("reservation_max_party_size", String.valueOf(request.getMaxPartySize()));
        configs.put("reservation_cancel_before_minutes", String.valueOf(request.getCancelBeforeMinutes()));

        updateBatchConfigs(configs);
        log.info("Reservation config updated");
    }

    @Override
    public Map<String, String> getReservationConfigMap() {
        Map<String, String> configMap = new LinkedHashMap<>();
        configMap.put("reservation_deposit_amount", String.valueOf(getReservationDepositAmount()));
        configMap.put("reservation_hold_minutes", String.valueOf(getReservationHoldMinutes()));
        configMap.put("reservation_max_per_day", String.valueOf(getReservationMaxPerDay()));
        configMap.put("reservation_max_advance_days", String.valueOf(getReservationMaxAdvanceDays()));
        configMap.put("reservation_min_advance_hours", String.valueOf(getReservationMinAdvanceHours()));
        configMap.put("reservation_max_party_size", String.valueOf(getReservationMaxPartySize()));
        configMap.put("reservation_cancel_before_minutes", String.valueOf(getReservationCancelBeforeMinutes()));
        return configMap;
    }

    // General
    @Override
    public String getSiteName() {
        return getConfig("site_name", "BrewMaster Coffee Shop");
    }

    @Override
    public String getSitePhone() {
        return getConfig("site_phone", "1900 1234");
    }

    @Override
    public String getSiteEmail() {
        return getConfig("site_email", "support@brewmaster.com");
    }

    @Override
    public String getSiteAddress() {
        return getConfig("site_address", "123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM");
    }

    @Override
    public String getSiteHours() {
        return getConfig("site_hours", "07:00 - 22:00");
    }

    @Override
    public String getSiteLogo() {
        return getConfig("site_logo", "/images/logo.png");
    }

    @Override
    public String getSiteFavicon() {
        return getConfig("site_favicon", "/images/favicon.ico");
    }

    @Override
    public String getSiteDescription() {
        return getConfig("site_description", "Hệ thống quản lý quán cà phê BrewMaster");
    }

    // System
    @Override
    public boolean isMaintenanceMode() {
        return "true".equals(getConfig("maintenance_mode", "false"));
    }

    @Override
    public String getMaintenanceMessage() {
        return getConfig("maintenance_message", "Hệ thống đang bảo trì. Vui lòng quay lại sau!");
    }

    @Override
    public int getLogRetentionDays() {
        try {
            return Integer.parseInt(getConfig("log_retention_days", "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    @Override
    public String getDefaultLanguage() {
        return getConfig("default_language", "vi");
    }

    @Override
    public int getItemsPerPage() {
        try {
            return Integer.parseInt(getConfig("items_per_page", "12"));
        } catch (NumberFormatException e) {
            return 12;
        }
    }

    // Reservation
    @Override
    public long getReservationDepositAmount() {
        try {
            return Long.parseLong(getConfig("reservation_deposit_amount", "50000"));
        } catch (NumberFormatException e) {
            return 50000;
        }
    }

    @Override
    public int getReservationHoldMinutes() {
        try {
            return Integer.parseInt(getConfig("reservation_hold_minutes", "15"));
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    @Override
    public int getReservationMaxPerDay() {
        try {
            return Integer.parseInt(getConfig("reservation_max_per_day", "3"));
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    @Override
    public int getReservationMaxAdvanceDays() {
        try {
            return Integer.parseInt(getConfig("reservation_max_advance_days", "30"));
        } catch (NumberFormatException e) {
            return 30;
        }
    }

    @Override
    public int getReservationMinAdvanceHours() {
        try {
            return Integer.parseInt(getConfig("reservation_min_advance_hours", "2"));
        } catch (NumberFormatException e) {
            return 2;
        }
    }

    @Override
    public int getReservationMaxPartySize() {
        try {
            return Integer.parseInt(getConfig("reservation_max_party_size", "10"));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    @Override
    public int getReservationCancelBeforeMinutes() {
        try {
            return Integer.parseInt(getConfig("reservation_cancel_before_minutes", "60"));
        } catch (NumberFormatException e) {
            return 60;
        }
    }

    private String getDefaultValue(String configKey) {
        Map<String, String> defaults = new LinkedHashMap<>();
        // General
        defaults.put("site_name", "BrewMaster Coffee Shop");
        defaults.put("site_phone", "1900 1234");
        defaults.put("site_email", "support@brewmaster.com");
        defaults.put("site_address", "123 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM");
        defaults.put("site_hours", "07:00 - 22:00");
        defaults.put("site_logo", "/images/logo.png");
        defaults.put("site_favicon", "/images/favicon.ico");
        defaults.put("site_description", "Hệ thống quản lý quán cà phê BrewMaster");
        // System
        defaults.put("maintenance_mode", "false");
        defaults.put("maintenance_message", "Hệ thống đang bảo trì. Vui lòng quay lại sau!");
        defaults.put("log_retention_days", "30");
        defaults.put("default_language", "vi");
        defaults.put("items_per_page", "12");
        // Reservation
        defaults.put("reservation_deposit_amount", "50000");
        defaults.put("reservation_hold_minutes", "15");
        defaults.put("reservation_max_per_day", "3");
        defaults.put("reservation_max_advance_days", "30");
        defaults.put("reservation_min_advance_hours", "2");
        defaults.put("reservation_max_party_size", "10");
        defaults.put("reservation_cancel_before_minutes", "60");

        return defaults.getOrDefault(configKey, "");
    }
}