package hsf302.se2033jv.project_hsf302_group2.admin.controller;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.ChatMessageDTO;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.GeminiRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.service.GeminiService;
import hsf302.se2033jv.project_hsf302_group2.admin.service.RevenueAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/revenue-analysis")
@RequiredArgsConstructor
public class RevenueAnalysisController {

    private final RevenueAnalysisService revenueAnalysisService;
    private final GeminiService geminiService;

    @GetMapping
    public String showRevenueAnalysisPage() {
        return "admin/revenue-analysis";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<?> chatWithAI(@RequestBody List<ChatMessageDTO> history) {
        try {
            // Chuẩn bị danh sách contents cho Gemini
            List<GeminiRequest.Content> contents = new ArrayList<>();
            
            // System prompt (Tin nhắn ảo từ user cung cấp bối cảnh)
            String systemPrompt = revenueAnalysisService.getInitialSystemPrompt();
            contents.add(GeminiRequest.Content.builder()
                    .role("user")
                    .parts(Collections.singletonList(GeminiRequest.Part.builder().text(systemPrompt).build()))
                    .build());
                    
            // Acknowledge của AI (để đảm bảo luồng user -> model -> user -> model hợp lệ trong gemini)
            contents.add(GeminiRequest.Content.builder()
                    .role("model")
                    .parts(Collections.singletonList(GeminiRequest.Part.builder().text("Đã hiểu dữ liệu. Tôi có thể giúp gì cho bạn?").build()))
                    .build());

            // Thêm lịch sử chat từ frontend
            for (ChatMessageDTO msg : history) {
                contents.add(GeminiRequest.Content.builder()
                        .role(msg.getRole())
                        .parts(Collections.singletonList(GeminiRequest.Part.builder().text(msg.getText()).build()))
                        .build());
            }

            String aiResponse = geminiService.chatRevenue(contents);
            return ResponseEntity.ok(Collections.singletonMap("reply", aiResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }
}
