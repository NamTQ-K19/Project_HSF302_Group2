package hsf302.se2033jv.project_hsf302_group2.admin.service;

import hsf302.se2033jv.project_hsf302_group2.admin.dto.GeminiRequest;
import hsf302.se2033jv.project_hsf302_group2.admin.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
    }

    public String chatRevenue(List<GeminiRequest.Content> contents) {
        GeminiRequest request = GeminiRequest.builder()
                .contents(contents)
                .build();

        java.net.URI uri = java.net.URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey);

        GeminiResponse response = webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();

        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            GeminiResponse.Candidate candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        return "Không thể nhận kết quả từ Gemini API.";
    }
}
