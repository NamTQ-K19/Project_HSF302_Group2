package hsf302.se2033jv.project_hsf302_group2.catalog.controller;


import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductDetailDTO;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IHomepageService;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.ILoyaltyPolicyService;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IProductDetailService;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * MVC Controller for the customer-facing catalog screens.
 *
 * <ul>
 *   <li>GET /            → UC_01 View Homepage</li>
 *   <li>GET /search      → UC_02 Search Product</li>
 *   <li>GET /products/{id} → UC_03 View Product Details</li>
 *   <li>GET /loyalty-policy → UC_04 View Loyalty Reward Policy</li>
 * </ul>
 *
 * DIP: depends on service interfaces, never on implementation classes.
 * SRP: handles HTTP routing and model population only — no business logic.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final IHomepageService homepageService;
    private final IProductSearchService productSearchService;
    private final IProductDetailService productDetailService;
    private final ILoyaltyPolicyService loyaltyPolicyService;

    // ── UC_01: View Homepage ──────────────────────────────────────────────────

    /**
     * Landing page — accessed when user navigates to root URL (TRG-01).
     */
    @GetMapping("/")
    public String viewHomepage(Model model) {
        try {
            model.addAttribute("bestSellers", homepageService.getBestSellers());
            model.addAttribute("categories", homepageService.getActiveCategories());
            return "catalog/homepage";
        } catch (Exception e) {
            log.error("Homepage data load failed", e);
            return "error/500";
        }
    }

    // ── UC_02: Search Product ─────────────────────────────────────────────────

    /**
     * Search results — triggered by the search bar (TRG-01 UC_02).
     * Also handles category filtering with the optional {@code categoryId} parameter.
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            Model model) {

        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", homepageService.getActiveCategories());
        model.addAttribute("selectedCategoryId", categoryId);

        if (categoryId != null) {
            model.addAttribute("products", productSearchService.filterByCategory(categoryId));
            model.addAttribute("searchMode", "category");
        } else if (!keyword.isBlank()) {
            model.addAttribute("products", productSearchService.searchByKeyword(keyword));
            model.addAttribute("searchMode", "keyword");
        } else {
            model.addAttribute("products", homepageService.getBestSellers());
            model.addAttribute("searchMode", "all");
        }

        return "catalog/search-results";
    }

    // ── UC_03: View Product Details ───────────────────────────────────────────

    /**
     * Product detail page — triggered by clicking a product card (TRG-01 UC_03).
     * Returns 404 view when the product is hidden/deleted (AT1 UC_03).
     */
    @GetMapping("/products/{productId}")
    public String viewProductDetail(@PathVariable Integer productId, Model model) {
        Optional<ProductDetailDTO> detail = productDetailService.getProductDetail(productId);

        if (detail.isEmpty()) {
            // AT1: product hidden or deleted — show 404 page
            return "error/product-not-found";
        }

        model.addAttribute("product", detail.get());
        model.addAttribute("categories", homepageService.getActiveCategories());
        return "catalog/product-detail";
    }

    // ── UC_04: View Loyalty Reward Policy ────────────────────────────────────

    /**
     * Loyalty reward policy page — accessed via footer or nav link (TRG-01 UC_04).
     */
    @GetMapping("/loyalty-policy")
    public String viewLoyaltyPolicy(Model model) {
        model.addAttribute("earnPolicies", loyaltyPolicyService.getEarnPolicies());
        model.addAttribute("redeemPolicies", loyaltyPolicyService.getRedeemPolicies());
        model.addAttribute("categories", homepageService.getActiveCategories());
        return "catalog/loyalty-policy";
    }
}