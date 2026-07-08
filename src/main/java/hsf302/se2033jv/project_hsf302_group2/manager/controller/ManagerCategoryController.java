package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CategoryRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ICategoryManageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/categories")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerCategoryController {

    private final ICategoryManageService categoryService;

    public ManagerCategoryController(ICategoryManageService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "manager/category/list";
    }

    @PostMapping("/create")
    public String createCategory(@Valid @ModelAttribute CategoryRequest dto, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm danh mục: " + e.getMessage());
        }
        return "redirect:/manager/categories";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Integer id, @Valid @ModelAttribute CategoryRequest dto, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật danh mục: " + e.getMessage());
        }
        return "redirect:/manager/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục này vì danh mục đang được liên kết với sản phẩm hoặc dữ liệu khác trong hệ thống!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa danh mục: " + e.getMessage());
        }
        return "redirect:/manager/categories";
    }
}