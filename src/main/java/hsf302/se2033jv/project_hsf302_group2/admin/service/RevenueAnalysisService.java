package hsf302.se2033jv.project_hsf302_group2.admin.service;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.RevenueDataDTO;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueAnalysisService {

    private final OrderRepository orderRepository;
    private final GeminiService geminiService;

    public RevenueDataDTO getRevenueData() {
        RevenueDataDTO dto = new RevenueDataDTO();
        
        // Tính tổng doanh thu
        // Ở đây lấy tất cả hoặc từ đầu năm (tạm lấy theo tất cả cho đơn giản)
        // Vì chưa có hàm tính tổng toàn thời gian nên ta có thể sum các tháng lại
        
        List<Object[]> monthlyData = orderRepository.getRevenueByMonth();
        Map<Integer, BigDecimal> revenueByMonth = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        for (Object[] row : monthlyData) {
            Integer month = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueByMonth.put(month, revenue);
            totalRevenue = totalRevenue.add(revenue);
        }
        
        dto.setRevenueByMonth(revenueByMonth);
        dto.setTotalRevenue(totalRevenue);
        
        List<Object[]> topProducts = orderRepository.getTopSellingProducts();
        Map<String, Long> productMap = new HashMap<>();
        for (Object[] row : topProducts) {
            String name = (String) row[0];
            Number qty = (Number) row[1];
            productMap.put(name, qty.longValue());
        }
        dto.setTopSellingProducts(productMap);
        
        return dto;
    }

    public String getInitialSystemPrompt() {
        RevenueDataDTO data = getRevenueData();
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một trợ lý AI thông minh chuyên tư vấn chiến lược kinh doanh cho quản lý của một quán cà phê.\n");
        prompt.append("Dưới đây là dữ liệu kinh doanh hiện tại của hệ thống quán:\n");
        prompt.append("- Tổng doanh thu: ").append(data.getTotalRevenue()).append(" VNĐ\n");
        
        prompt.append("- Doanh thu theo tháng:\n");
        data.getRevenueByMonth().forEach((month, rev) -> {
            prompt.append("  + Tháng ").append(month).append(": ").append(rev).append(" VNĐ\n");
        });
        
        prompt.append("- Top 10 sản phẩm bán chạy nhất:\n");
        data.getTopSellingProducts().forEach((name, qty) -> {
            prompt.append("  + ").append(name).append(": ").append(qty).append(" lượt bán\n");
        });
        
        prompt.append("\nHãy dựa vào các dữ liệu này để trả lời các câu hỏi của người dùng và tư vấn chiến lược giúp tăng doanh thu. Hãy trả lời ngắn gọn, súc tích và sử dụng định dạng Markdown để trình bày kết quả.");
        
        return prompt.toString();
    }
}
