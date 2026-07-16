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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductImage;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductImageRepository;
import hsf302.se2033jv.project_hsf302_group2.common.entity.ProductVariant;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductVariantRepository;
import hsf302.se2033jv.project_hsf302_group2.common.enums.VariantSize;
import hsf302.se2033jv.project_hsf302_group2.common.enums.VariantTemperature;
import java.math.BigDecimal;

@Service
public class ProductManageServiceImpl implements IProductManageService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    
    // Thư mục lưu ảnh
    private final String uploadDir = "src/main/resources/static/images/";

    // Constructor Injection (Thay vì dùng @Autowired trên field)
    public ProductManageServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductImageRepository productImageRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
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

        Product savedProduct = productRepository.save(product);

        // Lưu các biến thể động
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductRequest.VariantRequest vReq : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                    .product(savedProduct)
                    .variantName(vReq.getVariantName())
                    .size(vReq.getSize())
                    .temperature(vReq.getTemperature())
                    .price(vReq.getPrice())
                    .isAvailable(true)
                    .build();
                productVariantRepository.save(variant);

                if (vReq.getImage() != null && !vReq.getImage().isEmpty()) {
                    saveProductImage(savedProduct, vReq.getImage(), true, variant);
                }
            }
        }

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest dto) {
        Product savedProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        savedProduct.setName(dto.getName());
        savedProduct.setDescription(dto.getDescription());
        savedProduct.setCategory(category);
        
        savedProduct = productRepository.save(savedProduct);

        // Cập nhật giá và ảnh (Xóa variants và ảnh cũ đi làm lại cho nhanh với bài toán này)
        List<ProductImage> oldImages = productImageRepository.findByProductProductId(id);
        for (ProductImage img : oldImages) {
            img.setIsPrimary(false);
            productImageRepository.save(img); // Xóa logic Primary cũ
        }
        
        List<ProductVariant> currentVariants = productVariantRepository.findByProduct_ProductId(id);
        for(ProductVariant v : currentVariants) {
            v.setIsAvailable(false);
            productVariantRepository.save(v); // Ẩn variants cũ
        }

        // Tạo lại các biến thể mới
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductRequest.VariantRequest vReq : dto.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                    .product(savedProduct)
                    .variantName(vReq.getVariantName())
                    .size(vReq.getSize())
                    .temperature(vReq.getTemperature())
                    .price(vReq.getPrice())
                    .isAvailable(true)
                    .build();
                productVariantRepository.save(variant);

                if (vReq.getImage() != null && !vReq.getImage().isEmpty()) {
                    saveProductImage(savedProduct, vReq.getImage(), true, variant);
                }
            }
        }

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public void toggleProductStatus(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        product.setIsActive(product.getIsActive() == null ? false : !product.getIsActive());
        productRepository.save(product);
    }

    private void saveProductImage(Product product, MultipartFile file, boolean isPrimary, ProductVariant variant) {
        try {
            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            
            // Lưu vào src để code có thể commit và bền vững
            Path uploadPathSrc = Paths.get("src/main/resources/static/images/");
            if (!Files.exists(uploadPathSrc)) {
                Files.createDirectories(uploadPathSrc);
            }
            Path filePathSrc = uploadPathSrc.resolve(fileName);
            Files.copy(file.getInputStream(), filePathSrc, StandardCopyOption.REPLACE_EXISTING);
            
            // Lưu vào target để hiển thị luôn không cần restart
            Path uploadPathTarget = Paths.get("target/classes/static/images/");
            if (!Files.exists(uploadPathTarget)) {
                Files.createDirectories(uploadPathTarget);
            }
            Path filePathTarget = uploadPathTarget.resolve(fileName);
            Files.copy(file.getInputStream(), filePathTarget, StandardCopyOption.REPLACE_EXISTING);
            
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .variant(variant)
                    .imageUrl("/images/" + fileName)
                    .isPrimary(isPrimary)
                    .build();
            productImageRepository.save(productImage);
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
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