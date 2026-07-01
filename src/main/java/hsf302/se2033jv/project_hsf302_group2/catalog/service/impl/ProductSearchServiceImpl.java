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
    public List<ProductCardDTO> searchByKeyword(String keyword) {
        // Validate: empty keyword → empty result (not an error)
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        // Sanitize: trim and limit to max 50 characters per screen spec
        String sanitized = keyword.trim();
        if (sanitized.length() > KEYWORD_MAX_LENGTH) {
            sanitized = sanitized.substring(0, KEYWORD_MAX_LENGTH);
        }

        return productRepository.searchByKeyword(sanitized)
                .stream()
                .map(productMapper::toCardDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCardDTO> filterByCategory(Integer categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(productMapper::toCardDTO)
                .collect(Collectors.toList());
    }
}
