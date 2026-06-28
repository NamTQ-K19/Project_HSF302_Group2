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
     * Returns an empty list (never null) when no matches are found.
     *
     * @param keyword the raw search input from the user (trimmed, max 50 chars)
     * @return list of matching product cards
     */
    List<ProductCardDTO> searchByKeyword(String keyword);

    /**
     * Filter products by category ID.
     */
    List<ProductCardDTO> filterByCategory(Integer categoryId);
}