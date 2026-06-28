// customer/service/interfaces/CustomerCartService.java
package hsf302.se2033jv.project_hsf302_group2.customer.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.AddToCartRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.request.CartUpdateRequest;
import hsf302.se2033jv.project_hsf302_group2.customer.dto.response.CartResponse;

public interface CustomerCartService {

    CartResponse getCart(Integer userId);

    CartResponse addToCart(Integer userId, AddToCartRequest request);

    CartResponse updateCartItem(Integer userId, CartUpdateRequest request);

    CartResponse removeCartItem(Integer userId, Integer cartItemId);

    CartResponse clearCart(Integer userId);

    Integer getCartCount(Integer userId);
}