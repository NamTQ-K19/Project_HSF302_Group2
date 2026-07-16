package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.ProductRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ICategoryManageService;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.IProductManageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/products")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerProductController {

    private final IProductManageService productService;
    private final ICategoryManageService categoryService; // Dùng để lấy data cho Dropdown chọn Category

    public ManagerProductController(IProductManageService productService, ICategoryManageService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // Hiển thị danh sách sản phẩm + truyền danh mục sang View để làm Form Add/Edit
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "manager/product/list"; // Đường dẫn tới file HTML Thymeleaf
    }

    // Xử lý tạo sản phẩm mới
    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductRequest dto, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
        return "redirect:/manager/products";
    }

    // Xử lý cập nhật sản phẩm
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Integer id, @Valid @ModelAttribute ProductRequest dto, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
        return "redirect:/manager/products";
    }

    // Xử lý khóa / mở khóa sản phẩm
    @GetMapping("/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.toggleProductStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật trạng thái sản phẩm: " + e.getMessage());
        }
        return "redirect:/manager/products";
    }
}