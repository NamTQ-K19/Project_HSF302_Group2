package hsf302.se2033jv.project_hsf302_group2.catalog.service.impl;

import hsf302.se2033jv.project_hsf302_group2.catalog.dto.CategoryDTO;
import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductCardDTO;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CategoryRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IHomepageService;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UC_01 – Homepage service implementation.
 * SRP: loads best-sellers and categories only.
 * DIP: depends on IHomepageService abstraction (injected via constructor).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomepageServiceImpl implements IHomepageService {

    private static final int BEST_SELLER_LIMIT = 8;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductCardDTO> getBestSellers() {
        List<Product> bestSellers = productRepository.findBestSellers();

        // Fallback: if not enough completed orders exist yet, show all active products
        if (bestSellers.isEmpty()) {
            bestSellers = productRepository.findAllActiveAvailable();
        }

        return bestSellers.stream()
                .limit(BEST_SELLER_LIMIT)
                .map(productMapper::toCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findAllActive()
                .stream()
                .map(productMapper::toCategoryDTO)
                .collect(Collectors.toList());
    }
}