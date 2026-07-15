package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.IReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
@RequestMapping("/manager/reports")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerReportController {

    private final IReportService reportService;

    public ManagerReportController(IReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String viewReports(
            @RequestParam(value = "period", defaultValue = "month") String period,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        Map<String, Object> reportData = reportService.getReportData(period, startDate, endDate);
        model.addAttribute("reportData", reportData);
        model.addAttribute("period", period);
        model.addAttribute("startDate", reportData.get("startDate"));
        model.addAttribute("endDate", reportData.get("endDate"));
        model.addAttribute("activePage", "reports");

        return "manager/report/index";
    }

    @GetMapping("/export/excel")
    public void exportExcel(
            @RequestParam(value = "period", defaultValue = "month") String period,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "type", defaultValue = "sales") String type,
            HttpServletResponse response) throws IOException {

        byte[] excelContent = reportService.generateExcelReport(period, startDate, endDate, type);
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        
        String filename = "bao_cao_" + type + "_" + period + "_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        response.getOutputStream().write(excelContent);
        response.getOutputStream().flush();
    }
}
