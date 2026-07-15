package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ReviewFilterRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ReviewResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ReviewManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/reviews")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class ManagerReviewController {

    private static final int PAGE_SIZE = 10;

    private final ReviewManageService reviewService;
    private final ProductRepository productRepository;   // ← THÊM

    @GetMapping
    public String listReviews(
            @ModelAttribute("filter") ReviewFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<ReviewResponse> reviewPage = reviewService.getReviews(filter, page, PAGE_SIZE);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", page);
        model.addAttribute("products", productRepository.findActiveProducts());   // ← THÊM: cho dropdown

        return "manager/review/list";
    }

    @PostMapping("/hide/{id}")
    public String hideReview(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.hideReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi ẩn đánh giá: " + e.getMessage());
        }
        return "redirect:/manager/reviews";
    }

    @PostMapping("/show/{id}")
    public String showReview(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.showReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hiện đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hiện đánh giá: " + e.getMessage());
        }
        return "redirect:/manager/reviews";
    }
}