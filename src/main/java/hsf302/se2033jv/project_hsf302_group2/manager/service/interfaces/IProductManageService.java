package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ProductRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.ProductResponse;
import java.util.List;

public interface IProductManageService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Integer id);
    ProductResponse createProduct(ProductRequest dto);
    ProductResponse updateProduct(Integer id, ProductRequest dto);
    void deleteProduct(Integer id); // Xóa mềm (Soft delete)
}