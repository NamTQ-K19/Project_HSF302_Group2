package hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces;


import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductDetailDTO;

import java.util.Optional;

/**
 * Service contract for UC_03 View Product Details.
 * ISP: returns the full product detail — separate from search / listing.
 */
public interface IProductDetailService {

    /**
     * Retrieve full detail for a single active product.
     *
     * @param productId the product identifier from the URL path variable
     * @return Optional.empty() when the product does not exist or is inactive/unavailable
     */
    Optional<ProductDetailDTO> getProductDetail(Integer productId);
}
