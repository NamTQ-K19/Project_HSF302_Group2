package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CoffeeTableRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ICoffeeTableService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/tables")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerTableController {

    private final ICoffeeTableService tableService;

    public ManagerTableController(ICoffeeTableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        return "manager/table/list";
    }

    @PostMapping("/create")
    public String createTable(@Valid @ModelAttribute CoffeeTableRequest dto, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            tableService.createTable(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm bàn mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm bàn: " + e.getMessage());
        }
        return "redirect:/manager/tables";
    }

    @PostMapping("/update/{id}")
    public String updateTable(@PathVariable Integer id, @Valid @ModelAttribute CoffeeTableRequest dto, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            tableService.updateTable(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin bàn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật bàn: " + e.getMessage());
        }
        return "redirect:/manager/tables";
    }

    @GetMapping("/delete/{id}")
    public String deleteTable(@PathVariable Integer id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            tableService.deleteTable(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bàn nước thành công!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa bàn này vì bàn đang được liên kết với thông tin đặt bàn hoặc hóa đơn hiện tại!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa bàn nước: " + e.getMessage());
        }
        return "redirect:/manager/tables";
    }
}
