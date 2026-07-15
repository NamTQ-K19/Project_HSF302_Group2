// customer/controller/CustomerReviewController.java
package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PolicyRepository;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CreateReviewRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.ReviewResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/customer/reviews")
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService reviewService;
    private final PolicyRepository policyRepository;

    /**
     * Trang danh sách đánh giá của tôi
     */
    @GetMapping
    public String showMyReviews(Model model) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        List<ReviewResponse> reviews = reviewService.getReviewsByCustomer(userId);
        model.addAttribute("reviews", reviews);
        model.addAttribute("pageTitle", "Đánh giá của tôi");
        return "customer/review/list";
    }


    @GetMapping("/create")
    public String showCreateReview(@RequestParam Integer orderId, Model model) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        log.info("Showing create review page for order: {}", orderId);

        // Lấy danh sách sản phẩm có thể đánh giá
        List<ReviewResponse> reviewableProducts = reviewService.getReviewableProducts(userId, orderId);

        if (reviewableProducts.isEmpty()) {
            // Nếu không có sản phẩm để đánh giá, quay lại detail với thông báo
            return "redirect:/customer/orders/detail/" + orderId + "?noProduct=true";
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("products", reviewableProducts);
        model.addAttribute("pageTitle", "Đánh giá sản phẩm");
        model.addAttribute("reviewEarnPoints", getReviewEarnPoints());

        return "customer/review/create";
    }

    private Integer getReviewEarnPoints() {
        return policyRepository.findByPolicyTypeAndActionType(PolicyType.EARN, PolicyActionType.REVIEW)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(p -> p.getCurrencyValue().intValue())
                .orElse(0);
    }

    /**
     * Tạo đánh giá mới (AJAX)
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody CreateReviewRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Customer {} creating review", userId);

            ReviewResponse review = reviewService.createReview(userId, request);

            response.put("success", true);
            response.put("data", review);
            response.put("message", "Đánh giá thành công! Bạn nhận được " + review.getPointsEarned() + " điểm.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách đánh giá theo đơn hàng (AJAX)
     */
    @GetMapping("/order/{orderId}")
    @ResponseBody
    public ResponseEntity<List<ReviewResponse>> getReviewsByOrder(@PathVariable Integer orderId) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        List<ReviewResponse> reviews = reviewService.getReviewsByOrder(userId, orderId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Kiểm tra đã đánh giá chưa (AJAX)
     */
    @GetMapping("/check")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> hasReviewed(
            @RequestParam Integer orderId,
            @RequestParam Integer productId) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        boolean hasReviewed = reviewService.hasReviewed(userId, orderId, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasReviewed", hasReviewed);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/reviewable")
    @ResponseBody
    public ResponseEntity<List<ReviewResponse>> getReviewableProducts(@RequestParam Integer orderId) {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        List<ReviewResponse> products = reviewService.getReviewableProducts(userId, orderId);
        return ResponseEntity.ok(products);
    }
}