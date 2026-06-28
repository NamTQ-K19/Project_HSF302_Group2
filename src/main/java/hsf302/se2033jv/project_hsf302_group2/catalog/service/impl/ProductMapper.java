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
        String imageUrl = resolveImage(product.getImages());
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
        List<String> allImages = allImageUrls(product.getImages());
        double avgRating = avgRating(product.getReviews());
        long reviewCount = countVisibleReviews(product.getReviews());

        return ProductDetailDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .primaryImageUrl(resolveImage(product.getImages()))
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

    // ── private helpers ──────────────────────────────────────────────────────

    private String resolveImage(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return "/img/products/placeholder.jpg";
        return images.stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
    }

    private List<String> allImageUrls(List<ProductImage> images) {
        if (images == null) return List.of("/img/products/placeholder.jpg");
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
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
                .sorted(Comparator.comparing(v -> v.getSize() != null ? v.getSize().name() : ""))
                .map(v -> VariantDTO.builder()
                        .variantId(v.getVariantId())
                        .variantName(v.getVariantName())
                        .size(v.getSize() != null ? v.getSize().name() : null)
                        .temperature(v.getTemperature() != null ? v.getTemperature().name() : null)
                        .price(v.getPrice())
                        .isAvailable(v.getIsAvailable())
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