package hsf302.se2033jv.project_hsf302_group2.catalog.service.impl;

import hsf302.se2033jv.project_hsf302_group2.catalog.dto.ProductDetailDTO;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.catalog.service.interfaces.IProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UC_03 – View Product Details service implementation.
 * SRP: retrieves and maps one product by ID.
 * LSP: fulfils IProductDetailService contract without side effects.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailServiceImpl implements IProductDetailService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Optional<ProductDetailDTO> getProductDetail(Integer productId) {
        if (productId == null || productId <= 0) {
            return Optional.empty();
        }
        return productRepository.findActiveById(productId)
                .map(productMapper::toDetailDTO);
    }
}
