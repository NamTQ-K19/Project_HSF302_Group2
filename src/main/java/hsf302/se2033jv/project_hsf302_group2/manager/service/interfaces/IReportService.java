package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import java.util.Map;

public interface IReportService {
    
    /**
     * Get aggregated report data including Sales, Order, and Product statistics.
     * 
     * @param period       "today", "week", "month", or "custom"
     * @param startDateStr ISO date string (YYYY-MM-DD) for custom start range
     * @param endDateStr   ISO date string (YYYY-MM-DD) for custom end range
     * @return Map containing report categories and their metrics
     */
    Map<String, Object> getReportData(String period, String startDateStr, String endDateStr);
    
    /**
     * Get raw CSV formatted report content for export.
     * 
     * @param period       "today", "week", "month", or "custom"
     * @param startDateStr ISO date string (YYYY-MM-DD)
     * @param endDateStr   ISO date string (YYYY-MM-DD)
     * @param type         "sales", "orders", or "products"
     * @return CSV string content
     */
    byte[] generateExcelReport(String period, String startDateStr, String endDateStr, String type);
}
