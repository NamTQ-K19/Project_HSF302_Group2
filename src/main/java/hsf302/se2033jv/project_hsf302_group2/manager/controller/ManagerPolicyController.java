package hsf302.se2033jv.project_hsf302_group2.manager.controller;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.PolicyRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.PolicyManageService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/policies")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerPolicyController {

    private final PolicyManageService policyService;

    public ManagerPolicyController(PolicyManageService policyService) {
        this.policyService = policyService;
    }

    // ── Bước 1-5: Truy cập trang + Filter ───────────────────
    @GetMapping
    public String listPolicies(@RequestParam(required = false) String policyType,
                               @RequestParam(required = false) String actionType,
                               @RequestParam(required = false) String status,
                               Model model) {
        model.addAttribute("policies", policyService.getPolicies(policyType, actionType, status));
        model.addAttribute("selectedPolicyType", policyType);
        model.addAttribute("selectedActionType", actionType);
        model.addAttribute("selectedStatus", status);
        return "manager/policy/list";
    }

    // ── Sub-flow B: Edit ──────────────────────────────────────
    @PostMapping("/update/{id}")
    public String updatePolicy(@PathVariable Integer id,
                               @Valid @ModelAttribute PolicyRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", firstErrorMessage(bindingResult));
            return "redirect:/manager/policies";
        }
        try {
            policyService.updatePolicy(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật chính sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật: " + e.getMessage());
        }
        return "redirect:/manager/policies";
    }

    // ── Sub-flow C: Toggle Status ────────────────────────────
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            policyService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái chính sách!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/manager/policies";
    }

    private String firstErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Dữ liệu không hợp lệ");
    }
}