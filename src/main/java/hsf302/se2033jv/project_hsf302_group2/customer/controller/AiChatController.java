package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.ai.ChatTurnDTO;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.ChatRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ChatResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.AiChatService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * Nhận tin nhắn từ widget chat (AJAX), trả JSON.
     * Cho phép cả Anonymous Visitor lẫn Customer (theo đúng Swimlane).
     */
    @PostMapping("/send")
    public ChatResponse sendMessage(@Valid @RequestBody ChatRequest request,
                                    BindingResult bindingResult,
                                    HttpSession session) {
        if (bindingResult.hasErrors()) {
            ChatResponse res = new ChatResponse();
            res.setSuccess(false);
            res.setErrorMessage("Vui lòng nhập tin nhắn");
            return res;
        }
        return aiChatService.ask(request, session);
    }

    @GetMapping("/history")
    public List<ChatTurnDTO> getHistory(HttpSession session) {
        return aiChatService.getHistory(session);
    }
}
