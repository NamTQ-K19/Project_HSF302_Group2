package hsf302.se2033jv.project_hsf302_group2.catalog.service.impl;

import hsf302.se2033jv.project_hsf302_group2.catalog.dto.*;
import hsf302.se2033jv.project_hsf302_group2.common.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Converts JPA entities → DTOs.
 * SRP: mapping is its only responsibility; no repository or HTTP logic.
 * OCP: add new mappings here without touching services.
 */
@Component
public class ProductMapper {

    /**
     * Map a Product entity to a lightweight ProductCardDTO.
     */
    public ProductCardDTO toCardDTO(Product product) {
        List<ProductImage> allImages = new java.util.ArrayList<>();
        if (product.getImages() != null) allImages.addAll(product.getImages());
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> {
                if (v.getImages() != null) allImages.addAll(v.getImages());
            });
        }
        
        String imageUrl = resolveImage(allImages);
        BigDecimal minPrice = minPrice(product.getVariants());
        BigDecimal maxPrice = maxPrice(product.getVariants());
        double avgRating = avgRating(product.getReviews());
        long reviewCount = countVisibleReviews(product.getReviews());

        return ProductCardDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .primaryImageUrl(imageUrl)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }

    /**
     * Map a Product entity to the full ProductDetailDTO.
     */
    public ProductDetailDTO toDetailDTO(Product product) {
        List<VariantDTO> variants = toVariantDTOs(product.getVariants());
        List<ReviewDTO> reviews = toReviewDTOs(product.getReviews());
        
        List<ProductImage> allProductImages = new java.util.ArrayList<>();
        if (product.getImages() != null) allProductImages.addAll(product.getImages());
        if (product.getVariants() != null) {
            product.getVariants().forEach(v -> {
                if (v.getImages() != null) allProductImages.addAll(v.getImages());
            });
        }
        
        List<String> allImages = allImageUrls(allProductImages);
        double avgRating = avgRating(product.getReviews());
        long reviewCount = countVisibleReviews(product.getReviews());

        return ProductDetailDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .primaryImageUrl(resolveImage(allProductImages))
                .allImageUrls(allImages)
                .variants(variants)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .reviews(reviews)
                .build();
    }

    public CategoryDTO toCategoryDTO(Category category) {
        return CategoryDTO.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .build();
    }

    public PolicyDTO toPolicyDTO(Policy policy) {
        return PolicyDTO.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyName())
                .policyType(policy.getPolicyType() != null ? policy.getPolicyType().name() : "")
                .actionType(policy.getActionType() != null ? policy.getActionType().name() : "")
                .currencyValue(policy.getCurrencyValue())
                .unit(policy.getUnit())
                .comment(policy.getComment())
                .status(policy.getStatus())
                .build();
    }

    private String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) return "/img/products/placeholder.jpg";
        if (url.startsWith("http") || url.startsWith("/")) return url;
        return "/" + url;
    }

    private String resolveImage(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return "/img/products/placeholder.jpg";
        String url = images.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
        return normalizeImageUrl(url);
    }

    private List<String> allImageUrls(List<ProductImage> images) {
        if (images == null) return List.of("/img/products/placeholder.jpg");
        return images.stream()
                .map(ProductImage::getImageUrl)
                .map(this::normalizeImageUrl)
                .distinct()
                .collect(Collectors.toList());
    }

    // MỚI: đảm bảo mọi URL trả ra đều là đường dẫn tuyệt đối (bắt đầu bằng "/")
    // hoặc URL đầy đủ (http://...), không bao giờ để URL tương đối gây lỗi
    // phân giải sai theo path trang hiện tại (VD: /products/11 vs /search)
    private String normalizeImageUrl(String url) {
        if (url == null || url.isBlank()) return "/img/products/placeholder.jpg";
        if (url.startsWith("/") || url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return "/" + url;
    }

    private BigDecimal minPrice(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return BigDecimal.ZERO;
        return variants.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsAvailable()))
                .map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal maxPrice(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return BigDecimal.ZERO;
        return variants.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsAvailable()))
                .map(ProductVariant::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
    }

    private double avgRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return 0.0;
        OptionalDouble avg = reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVisible()))
                .mapToInt(Review::getRating)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    private long countVisibleReviews(List<Review> reviews) {
        if (reviews == null) return 0;
        return reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVisible()))
                .count();
    }

    private List<VariantDTO> toVariantDTOs(List<ProductVariant> variants) {
        if (variants == null) return List.of();
        return variants.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsAvailable()))
                .sorted(Comparator.comparingInt(v -> {
                    if (v.getSize() == null) return 99;
                    switch (v.getSize().name()) {
                        case "M": return 0;
                        case "L": return 1;
                        case "S": return 2;
                        default: return 99;
                    }
                }))
                .map(v -> VariantDTO.builder()
                        .variantId(v.getVariantId())
                        .variantName(v.getVariantName())
                        .size(v.getSize() != null ? v.getSize().name() : null)
                        .temperature(v.getTemperature() != null ? v.getTemperature().name() : null)
                        .price(v.getPrice())
                        .isAvailable(v.getIsAvailable())
                        .imageUrl(resolveImage(v.getImages()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ReviewDTO> toReviewDTOs(List<Review> reviews) {
        if (reviews == null) return List.of();
        return reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsVisible()))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .map(r -> ReviewDTO.builder()
                        .customerName(r.getCustomer().getFirstName() + " " + r.getCustomer().getLastName())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}