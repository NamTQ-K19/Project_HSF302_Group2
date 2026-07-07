package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Category;
import hsf302.se2033jv.project_hsf302_group2.common.entity.Product;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CategoryRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ProductRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ProductResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.IProductManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductManageServiceImpl implements IProductManageService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Constructor Injection (Thay vì dùng @Autowired trên field)
    public ProductManageServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(category)
                .isActive(true)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        // Chuẩn hệ thống lớn: Xóa mềm (Soft Delete) để giữ lại lịch sử đơn hàng
        // product.setActive(false);
        // productRepository.save(product);

        productRepository.delete(product); // Dùng tạm lệnh này nếu bạn muốn xóa hẳn khỏi DB
    }

    // Hàm ánh xạ riêng biệt giúp code sạch sẽ
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Chưa phân loại")
                .isActive(product.getIsActive() != null ? product.getIsActive() : true)
                .build();
    }
}