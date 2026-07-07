package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CategoryRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CategoryResponse;
import java.util.List;

public interface ICategoryManageService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Integer id);
    CategoryResponse createCategory(CategoryRequest dto);
    CategoryResponse updateCategory(Integer id, CategoryRequest dto);
    void deleteCategory(Integer id); // Soft delete
}