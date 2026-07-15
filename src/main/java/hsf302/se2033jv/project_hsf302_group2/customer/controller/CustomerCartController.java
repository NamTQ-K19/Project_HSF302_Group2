// customer/controller/CustomerCartController.java
package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CartItem;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Policy;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyActionType;
import hsf302.se2033jv.project_hsf302_group2.common.enums.PolicyType;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.PolicyRepository;
import hsf302.se2033jv.project_hsf302_group2.common.util.SecurityUtils;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddToCartRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CartUpdateRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.CartResponse;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CartItemRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerCartController {

    private final CustomerCartService cartService;
    private final CartItemRepository cartItemRepository;
    private final PolicyRepository policyRepository;

    @GetMapping
    public String showCart(Model model) {
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Showing cart for user: {}", userId);
            CartResponse cart = cartService.getCart(userId);
            model.addAttribute("cart", cart);
            model.addAttribute("pageTitle", "Giỏ hàng");
            model.addAttribute("earnPercent", getOrderEarnPercent());   // MỚI
        } catch (Exception e) {
            log.error("Error loading cart: {}", e.getMessage(), e);
            model.addAttribute("cart", null);
            model.addAttribute("pageTitle", "Giỏ hàng");
            model.addAttribute("earnPercent", BigDecimal.ZERO);   // MỚI — fallback khi lỗi
        }
        return "customer/cart/index";
    }

    private BigDecimal getOrderEarnPercent() {
        return policyRepository.findByPolicyTypeAndActionType(PolicyType.EARN, PolicyActionType.ORDER)
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .map(Policy::getCurrencyValue)
                .orElse(BigDecimal.ZERO);
    }

    @GetMapping("/json")
    @ResponseBody
    public ResponseEntity<CartResponse> getCartJson() {
        Integer userId = SecurityUtils.getCurrentUserId().intValue();
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody AddToCartRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Add to cart: userId={}, productId={}, variantId={}, quantity={}",
                    userId, request.getProductId(), request.getVariantId(), request.getQuantity());

            CartResponse cart = cartService.addToCart(userId, request);

            response.put("success", true);
            response.put("data", cart);
            response.put("message", "Thêm sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody CartUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Update cart: userId={}, cartItemId={}, action={}",
                    userId, request.getCartItemId(), request.getAction());

            CartResponse cart = cartService.updateCartItem(userId, request);

            response.put("success", true);
            response.put("data", cart);
            response.put("message", "Cập nhật giỏ hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating cart: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/remove/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeCartItem(@PathVariable Integer itemId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Remove cart item: userId={}, itemId={}", userId, itemId);

            CartResponse cart = cartService.removeCartItem(userId, itemId);

            response.put("success", true);
            response.put("data", cart);
            response.put("message", "Xóa sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error removing cart item: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart() {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Clear cart: userId={}", userId);

            CartResponse cart = cartService.clearCart(userId);

            response.put("success", true);
            response.put("data", cart);
            response.put("message", "Xóa giỏ hàng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartCount() {
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            Integer count = cartService.getCartCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting cart count: {}", e.getMessage(), e);
            return ResponseEntity.ok(0);
        }
    }

    @PostMapping("/selected-items")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSelectedItems(@RequestBody List<Integer> itemIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = SecurityUtils.getCurrentUserId().intValue();
            log.info("Getting selected items: userId={}, itemIds={}", userId, itemIds);

            if (itemIds == null || itemIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không có sản phẩm nào được chọn");
                return ResponseEntity.badRequest().body(response);
            }

            List<CartItem> selectedItems = cartItemRepository.findAllById(itemIds);

            for (CartItem item : selectedItems) {
                if (!item.getCart().getCustomer().getUserId().equals(userId)) {
                    throw new BusinessException("You are not authorized to access this item");
                }
            }

            if (selectedItems.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy sản phẩm nào");
                return ResponseEntity.badRequest().body(response);
            }

            // Tính tổng tiền
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem item : selectedItems) {
                BigDecimal itemTotal = item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }

            // Build response items
            List<Map<String, Object>> items = selectedItems.stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("cartItemId", item.getCartItemId());
                itemMap.put("productId", item.getProduct().getProductId());
                itemMap.put("variantId", item.getVariant().getVariantId());
                itemMap.put("productName", item.getProduct().getName());
                itemMap.put("variantName", item.getVariant().getVariantName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getVariant().getPrice());
                itemMap.put("itemTotal", item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                itemMap.put("specialNote", item.getSpecialNote());

                String imageUrl = "/images/default-product.png";
                if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                    if (item.getProduct().getImages().get(0) != null) {
                        imageUrl = item.getProduct().getImages().get(0).getImageUrl();
                    }
                }
                itemMap.put("productImage", imageUrl);

                return itemMap;
            }).collect(java.util.stream.Collectors.toList());

            response.put("success", true);
            response.put("items", items);
            response.put("total", total);
            response.put("itemIds", itemIds);
            response.put("count", items.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting selected items: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}