package hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductCardDTO;

import java.util.List;

/**
 * Service contract for UC_02 Search Product.
 * ISP: single responsibility — keyword search only.
 */
public interface IProductSearchService {

    /**
     * Search products whose name or description contains the given keyword.
     * Returns an empty page (never null) when no matches are found.
     *
     * @param keyword the raw search input from the user (trimmed, max 50 chars)
     * @param pageable pagination parameters
     * @return page of matching product cards
     */
    org.springframework.data.domain.Page<ProductCardDTO> searchByKeyword(String keyword, org.springframework.data.domain.Pageable pageable);

    /**
     * Filter products by category ID with pagination.
     */
    org.springframework.data.domain.Page<ProductCardDTO> filterByCategory(Integer categoryId, org.springframework.data.domain.Pageable pageable);

    /**
     * Get all active and available products with pagination.
     */
    org.springframework.data.domain.Page<ProductCardDTO> getAllProducts(org.springframework.data.domain.Pageable pageable);
}