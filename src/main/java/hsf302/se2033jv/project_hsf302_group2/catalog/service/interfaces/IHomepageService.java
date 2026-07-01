package hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces;


import hsf302.se2033jv.project_hsf302_group2.catalog.dto.CategoryDTO;
import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductCardDTO;

import java.util.List;

/**
 * Service contract for UC_01 View Homepage.
 * ISP: dedicated interface — not mixed with search or detail concerns.
 */
public interface IHomepageService {

    /**
     * Returns top best-selling products to display on the homepage grid.
     */
    List<ProductCardDTO> getBestSellers();

    /**
     * Returns all active categories for the navigation filter bar.
     */
    List<CategoryDTO> getActiveCategories();
}
