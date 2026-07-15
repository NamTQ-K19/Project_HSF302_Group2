package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.ai.ChatTurnDTO;
import hsf302.se2033jv.project_hsf302_group2.common.ai.GeminiClient;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.ChatRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ChatResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.AiChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String SESSION_KEY = "AI_CHAT_HISTORY";
    private static final int MAX_TURNS_KEPT = 20; // tránh session phình to vô hạn

    private final ProductRepository productRepository;
    private final GeminiClient geminiClient;

    @Override
    public ChatResponse ask(ChatRequest request, HttpSession session) {
        ChatResponse response = new ChatResponse();

        if (request.getMessage() == null || request.getMessage().isBlank()) {
            response.setSuccess(false);
            response.setErrorMessage("Vui lòng nhập tin nhắn");
            return response;
        }

        List<ChatTurnDTO> history = getHistory(session);

        try {
            List<Product> products = productRepository.findAllActiveAvailable();

            String menuContext = buildMenuContext(products);
            String prompt = buildPrompt(menuContext, request.getMessage());
            String rawReply = geminiClient.generateReply(prompt);

            // ── Format đẹp (markdown-lite → HTML) + gắn link sản phẩm ──
            String htmlReply = formatToHtml(rawReply);
            htmlReply = linkifyProducts(htmlReply, products);

            // ── Lưu vào session (user message lưu plain text) ──────────
            history.add(new ChatTurnDTO("user", HtmlUtils.htmlEscape(request.getMessage())));
            history.add(new ChatTurnDTO("ai", htmlReply));
            trimHistory(history);
            session.setAttribute(SESSION_KEY, history);

            response.setSuccess(true);
            response.setReply(htmlReply);

        } catch (Exception e) {
            log.error("Lỗi AI Chat: {}", e.getMessage());
            response.setSuccess(false);
            response.setErrorMessage("Trợ lý AI đang gặp sự cố, vui lòng thử lại sau.");
        }

        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatTurnDTO> getHistory(HttpSession session) {
        Object stored = session.getAttribute(SESSION_KEY);
        if (stored instanceof List) {
            return (List<ChatTurnDTO>) stored;
        }
        return new ArrayList<>();
    }

    private void trimHistory(List<ChatTurnDTO> history) {
        while (history.size() > MAX_TURNS_KEPT) {
            history.removeFirst();
        }
    }

    // ── Xây dữ liệu menu dạng text để đưa vào prompt ────────────────────
    private String buildMenuContext(List<Product> products) {
        return products.stream()
                .map(p -> {
                    String variants = p.getVariants() == null ? "" :
                            p.getVariants().stream()
                            .filter(v -> Boolean.TRUE.equals(v.getIsAvailable()))
                            .map(v -> String.format("%s (%s, %s): %,.0fđ",
                                    v.getVariantName(),
                                    v.getSize() != null ? v.getSize() : "-",
                                    v.getTemperature() != null ? v.getTemperature() : "-",
                                    v.getPrice()))
                            .collect(Collectors.joining(", "));

                    return String.format("- %s [%s]: %s. Biến thể: %s",
                            p.getName(),
                            p.getCategory() != null ? p.getCategory().getName() : "Khác",
                            p.getDescription() != null ? p.getDescription() : "",
                            variants);
                })
                .collect(Collectors.joining("\n"));
    }

    private String buildPrompt(String menuContext, String userMessage) {
        return """
            Bạn là trợ lý tư vấn menu của quán cà phê "BrewMaster". \
            Chỉ trả lời dựa trên danh sách món dưới đây, không bịa ra món không có. \
            Khi nhắc tên món, hãy dùng CHÍNH XÁC tên món như trong danh sách (không viết tắt, không đổi cách viết). \
            Trả lời ngắn gọn, thân thiện, bằng tiếng Việt. Có thể dùng markdown **in đậm** và gạch đầu dòng "- " nếu cần liệt kê. \
            Nếu khách hỏi ngoài phạm vi menu/quán, lịch sự từ chối và mời hỏi về menu.

            DANH SÁCH MÓN HIỆN CÓ:
            %s

            CÂU HỎI CỦA KHÁCH:
            %s
            """.formatted(menuContext, userMessage);
    }

    // ── VẤN ĐỀ 1: Format markdown-lite → HTML an toàn ───────────────────
    private String formatToHtml(String rawText) {
        // 1. Escape HTML trước để chống XSS (nếu AI lỡ trả về ký tự < > &)
        String escaped = HtmlUtils.htmlEscape(rawText);

        // 2. **bold** → <strong>bold</strong>
        escaped = escaped.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");

        // 3. Xử lý từng dòng: gộp các dòng bắt đầu bằng "- " hoặc "* " thành <ul><li>
        String[] lines = escaped.split("\n");
        StringBuilder html = new StringBuilder();
        boolean inList = false;

        for (String line : lines) {
            String trimmed = line.trim();
            boolean isBullet = trimmed.startsWith("- ") || trimmed.startsWith("* ");

            if (isBullet) {
                if (!inList) {
                    html.append("<ul class=\"ai-chat-list\">");
                    inList = true;
                }
                html.append("<li>").append(trimmed.substring(2).trim()).append("</li>");
            } else {
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
                if (!trimmed.isEmpty()) {
                    html.append("<p>").append(trimmed).append("</p>");
                }
            }
        }
        if (inList) html.append("</ul>");

        return html.toString();
    }

    // ── VẤN ĐỀ 3: Gắn link tên món → trang chi tiết sản phẩm ────────────
    private String linkifyProducts(String html, List<Product> products) {

        // Danh sách "tên có thể link" gồm CẢ tên sản phẩm cha lẫn tên từng biến thể,
        // vì AI thường trả lời bằng tên biến thể cụ thể (VD: "Đen Đá Vừa", "Đen Nóng")
        // thay vì tên sản phẩm gốc (VD: "Cà Phê Đen") — cả 2 đều phải trỏ về
        // cùng 1 trang chi tiết sản phẩm /products/{productId}.
        record LinkCandidate(String name, Integer productId) {}

        List<LinkCandidate> candidates = new ArrayList<>();
        for (Product p : products) {
            if (p.getName() != null) {
                candidates.add(new LinkCandidate(p.getName(), p.getProductId()));
            }
            if (p.getVariants() != null) {
                for (var v : p.getVariants()) {
                    if (Boolean.TRUE.equals(v.getIsAvailable()) && v.getVariantName() != null) {
                        candidates.add(new LinkCandidate(v.getVariantName(), p.getProductId()));
                    }
                }
            }
        }

        // Sắp xếp tên dài trước để tránh match nhầm tên ngắn nằm trong tên dài
        // (VD: "Đen Đá" không được match trước "Đen Đá Vừa")
        candidates.sort(Comparator.comparingInt((LinkCandidate c) -> c.name().length()).reversed());

        List<String[]> placeholders = new ArrayList<>();
        int idx = 0;

        // Bước 1: thay tên món/biến thể bằng placeholder duy nhất (KHÔNG dùng regex/lookbehind)
        for (LinkCandidate c : candidates) {
            String escapedName = HtmlUtils.htmlEscape(c.name());
            if (!html.contains(escapedName)) continue;

            String placeholder = "\u0000PRODLINK" + (idx++) + "\u0000";
            String replacementHtml = "<a href=\"/products/" + c.productId()
                    + "\" class=\"ai-chat-product-link\">" + escapedName + "</a>";

            // Chỉ thay LẦN XUẤT HIỆN ĐẦU TIÊN của tên này (String.replaceFirst ở đây
            // dùng Pattern.quote nên là plain-text match, không phải regex thật)
            html = html.replaceFirst(java.util.regex.Pattern.quote(escapedName),
                    java.util.regex.Matcher.quoteReplacement(placeholder));

            placeholders.add(new String[]{placeholder, replacementHtml});
        }

        // Bước 2: thay toàn bộ placeholder bằng thẻ <a> thật
        for (String[] pair : placeholders) {
            html = html.replace(pair[0], pair[1]);
        }

        return html;
    }
}