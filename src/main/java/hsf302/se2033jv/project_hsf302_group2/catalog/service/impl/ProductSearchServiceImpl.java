package hsf302.se2033jv.project_hsf302_group2.catalog.service.impl;

import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductCardDTO;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UC_02 – Search Product service implementation.
 * SRP: keyword search and category filter only.
 * OCP: new search strategies can be added without changing callers.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchServiceImpl implements IProductSearchService {

    private static final int KEYWORD_MAX_LENGTH = 50;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public org.springframework.data.domain.Page<ProductCardDTO> searchByKeyword(String keyword, org.springframework.data.domain.Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return org.springframework.data.domain.Page.empty(pageable);
        }

        String sanitized = keyword.trim();
        if (sanitized.length() > KEYWORD_MAX_LENGTH) {
            sanitized = sanitized.substring(0, KEYWORD_MAX_LENGTH);
        }

        return productRepository.searchByKeyword(sanitized, pageable)
                .map(productMapper::toCardDTO);
    }

    @Override
    public org.springframework.data.domain.Page<ProductCardDTO> filterByCategory(Integer categoryId, org.springframework.data.domain.Pageable pageable) {
        if (categoryId == null) {
            return org.springframework.data.domain.Page.empty(pageable);
        }
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(productMapper::toCardDTO);
    }

    @Override
    public org.springframework.data.domain.Page<ProductCardDTO> getAllProducts(org.springframework.data.domain.Pageable pageable) {
        return productRepository.findAllActiveAvailablePage(pageable)
                .map(productMapper::toCardDTO);
    }
}
