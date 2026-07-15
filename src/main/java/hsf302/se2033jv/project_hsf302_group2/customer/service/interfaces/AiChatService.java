package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.common.ai.ChatTurnDTO;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.ChatRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ChatResponse;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface AiChatService {

    /**
     * Xử lý 1 tin nhắn từ khách hàng:
     * 1. Lấy dữ liệu menu hiện có (products đang bán)
     * 2. Ghép menu context + câu hỏi khách thành prompt
     * 3. Gửi tới Gemini, nhận phản hồi
     */
    ChatResponse ask(ChatRequest request, HttpSession session);

    List<ChatTurnDTO> getHistory(HttpSession session);
}