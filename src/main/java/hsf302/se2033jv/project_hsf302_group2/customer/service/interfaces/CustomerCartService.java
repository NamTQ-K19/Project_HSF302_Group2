// customer/service/interfaces/CustomerCartService.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddToCartRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CartUpdateRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.CartResponse;

public interface CustomerCartService {

    CartResponse getCart(Long userId);

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse updateCartItem(Long userId, CartUpdateRequest request);

    CartResponse removeCartItem(Long userId, Integer cartItemId);

    CartResponse clearCart(Long userId);

    Integer getCartCount(Long userId);
}