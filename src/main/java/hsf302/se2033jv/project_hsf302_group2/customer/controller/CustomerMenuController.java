// customer/controller/CustomerMenuController.java
package hsf302.se2033jv.project_hsf302_group2.customer.controller;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductImage;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductVariant;
import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.customer.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CustomerMenuController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    // Ảnh mặc định dạng Data URI (không cần tải từ server)
    private static final String DEFAULT_IMAGE = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200' viewBox='0 0 200 200'%3E%3Crect width='200' height='200' fill='%23f8f9fa'/%3E%3Ctext x='50%25' y='55%25' dominant-baseline='middle' text-anchor='middle' font-family='Arial' font-size='60' fill='%23dee2e6'%3E☕%3C/text%3E%3C/svg%3E";

    @GetMapping({"/", "/menu"})
    public String showMenu(Model model) {
        List<Product> products = productRepository.findByIsActiveTrue();

        products.forEach(product -> {
            // Lấy variants
            List<ProductVariant> variants = productVariantRepository.findByProduct_ProductId(product.getProductId());
            product.setVariants(variants);

            // Đảm bảo mỗi sản phẩm luôn có ít nhất 1 ảnh (fallback)
            if (product.getImages() == null || product.getImages().isEmpty()) {
                ProductImage defaultImage = ProductImage.builder()
                        .imageUrl(DEFAULT_IMAGE)
                        .isPrimary(true)
                        .build();
                product.setImages(List.of(defaultImage));
            }
        });

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Menu");
        return "menu";
    }
}