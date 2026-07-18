package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Category;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CategoryRepository;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CategoryRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CategoryResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ICategoryManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryManageServiceImpl implements ICategoryManageService {

    private final CategoryRepository categoryRepository;

    public CategoryManageServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với ID: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .isActive(true)
                .build();
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với ID: " + id));

        category.setName(dto.getName());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void toggleCategoryStatus(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category với ID: " + id));
        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getCategoryId())
                .name(category.getName())
                .isActive(category.getIsActive() != null ? category.getIsActive() : true)
                .build();
    }
}