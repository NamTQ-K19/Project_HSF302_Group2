// customer/service/impl/CustomerCartServiceImpl.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Cart;
import hsf302.se2033jv.project_hsf302_group2.common.entity.CartItem;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductImage;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductVariant;
import hsf302.se2033jv.project_hsf302_group2.common.entity.User;
import hsf302.se2033jv.project_hsf302_group2.common.exception.BusinessException;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.common.repository.*;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddToCartRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CartUpdateRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.CartResponse;
import hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces.CustomerCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerCartServiceImpl implements CustomerCartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final LoyaltyPointRepository loyaltyPointRepository;

    private static final String DEFAULT_IMAGE = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60' viewBox='0 0 60 60'%3E%3Crect width='60' height='60' fill='%23f8f9fa'/%3E%3Ctext x='50%25' y='55%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='30' fill='%23dee2e6'%3E☕%3C/text%3E%3C/svg%3E";

    @Override
    public CartResponse getCart(Integer userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        return buildCartResponse(cart, cartItems, userId);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Integer userId, AddToCartRequest request) {
        log.info("Adding to cart: userId={}, request={}", userId, request);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getIsActive()) {
            throw new BusinessException("Product is not available");
        }

        ProductVariant variant;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        } else {
            variant = product.getVariants().stream()
                    .filter(ProductVariant::getIsAvailable)
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("No available variant for this product"));
        }

        if (!variant.getIsAvailable()) {
            throw new BusinessException("Variant is not available");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem existingItem = cartItemRepository.findByCart_CartIdAndVariant_VariantId(
                cart.getCartId(), variant.getVariantId()).orElse(null);

        if (existingItem != null) {
            throw new BusinessException("Sản phẩm đã có trong giỏ hàng");
        }

        CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .variant(variant)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .specialNote(request.getSpecialNote())
                .addedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        cartItemRepository.save(newItem);

        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        return buildCartResponse(cart, cartItems, userId);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Integer userId, CartUpdateRequest request) {
        log.info("Updating cart item: userId={}, cartItemId={}, action={}",
                userId, request.getCartItemId(), request.getAction());

        if (request.getCartItemId() == null) {
            throw new BusinessException("cartItemId is required");
        }

        CartItem item = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + request.getCartItemId()));

        if (!item.getCart().getCustomer().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to update this cart item");
        }

        int newQuantity = item.getQuantity();
        if ("increase".equals(request.getAction())) {
            newQuantity++;
        } else if ("decrease".equals(request.getAction())) {
            if (newQuantity <= 1) {
                throw new BusinessException("Sản phẩm đã đạt số lượng tối thiểu là 1!");
            }
            newQuantity--;
        } else {
            throw new BusinessException("Invalid action: " + request.getAction());
        }

        item.setQuantity(newQuantity);
        item.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(item);
        log.info("Updated cart item: id={}, newQuantity={}", item.getCartItemId(), newQuantity);

        Cart cart = item.getCart();
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        return buildCartResponse(cart, cartItems, userId);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Integer userId, Integer cartItemId) {
        log.info("Removing cart item: userId={}, itemId={}", userId, cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getCustomer().getUserId().equals(userId)) {
            throw new BusinessException("You are not authorized to remove this cart item");
        }

        cartItemRepository.delete(item);
        log.info("Removed cart item: id={}", cartItemId);

        Cart cart = item.getCart();
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        return buildCartResponse(cart, cartItems, userId);
    }

    @Override
    @Transactional
    public CartResponse clearCart(Integer userId) {
        log.info("Clearing cart: userId={}", userId);

        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart_CartId(cart.getCartId());
        log.info("Cleared all cart items for cart: id={}", cart.getCartId());

        return buildCartResponse(cart, List.of(), userId);
    }

    @Override
    public Integer getCartCount(Integer userId) {
        Cart cart = cartRepository.findByCustomer_UserId(userId).orElse(null);
        if (cart == null) {
            return 0;
        }
        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // ==================== PRIVATE METHODS ====================

    private Cart getOrCreateCart(Integer userId) {
        return cartRepository.findByCustomer_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    Cart newCart = Cart.builder()
                            .customer(user)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse buildCartResponse(Cart cart, List<CartItem> cartItems, Integer userId) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());

        List<CartResponse.CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> {
                    CartResponse.CartItemResponse itemResponse = new CartResponse.CartItemResponse();
                    itemResponse.setCartItemId(item.getCartItemId());
                    itemResponse.setProductId(item.getProduct().getProductId());
                    itemResponse.setVariantId(item.getVariant().getVariantId());
                    itemResponse.setProductName(item.getProduct().getName());
                    itemResponse.setVariantName(item.getVariant().getVariantName());
                    itemResponse.setPrice(item.getVariant().getPrice());
                    itemResponse.setQuantity(item.getQuantity());
                    itemResponse.setItemTotal(item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    itemResponse.setSpecialNote(item.getSpecialNote());

                    String imageUrl = DEFAULT_IMAGE;
                    if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                        for (ProductImage img : item.getProduct().getImages()) {
                            if (img.getIsPrimary() != null && img.getIsPrimary()) {
                                imageUrl = img.getImageUrl();
                                break;
                            }
                        }
                        if (imageUrl.equals(DEFAULT_IMAGE) && !item.getProduct().getImages().isEmpty()) {
                            imageUrl = item.getProduct().getImages().get(0).getImageUrl();
                        }
                    }
                    itemResponse.setProductImage(imageUrl);

                    return itemResponse;
                })
                .collect(Collectors.toList());

        response.setItems(itemResponses);
        response.setTotalItems(itemResponses.size());

        BigDecimal subtotal = itemResponses.stream()
                .map(CartResponse.CartItemResponse::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.setSubtotal(subtotal);
        response.setTotal(subtotal);

        Integer loyaltyPoints = loyaltyPointRepository.getTotalPointsByCustomerId(userId);
        response.setLoyaltyPoints(loyaltyPoints != null ? loyaltyPoints : 0);

        return response;
    }
}