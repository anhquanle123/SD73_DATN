package com.project.DuAnTotNghiep.controller.admin;



import com.project.DuAnTotNghiep.dto.Employee.EmployeeDto;
import com.project.DuAnTotNghiep.entity.Employee;
import com.project.DuAnTotNghiep.service.EmployeeService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmployeeController {
    private final EmployeeService employeeService;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService=employeeService;
    }

    @GetMapping("/admin-only/employee-management")
    public String showEmployeeManagementPage(Model model, @PageableDefault(size = 1000) Pageable pageable) {
        Page<EmployeeDto> employees = employeeService.getAllEmployees(pageable);
        model.addAttribute("employees", employees);
        return "admin/employee-management";
    }

    @PostMapping("/admin-only/employee-management/save")
    public String saveEmployee(@ModelAttribute EmployeeDto employeeDto) {
        if (employeeDto.getId() != null) {
            employeeService.updateEmployee(employeeDto.getId(), employeeDto);
        } else {
            employeeService.createEmployeeAdmin(employeeDto);
        }
        return "redirect:/admin-only/employee-management";
    }

    @GetMapping("/admin-only/employee-management/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return "redirect:/admin-only/employee-management";
    }

    @GetMapping("/admin-only/employee-management/add")
    public String showAddForm(Model model) {
        EmployeeDto employeeDto = new EmployeeDto();
        Employee lastEmployee = employeeService.getLastEmployee();
        String autoCode = (lastEmployee == null) ? "NV0001" : "NV" + String.format("%04d", lastEmployee.getId() + 1);
        employeeDto.setCode(autoCode);
        employeeDto.setGender(true); // hoặc null nếu muốn mặc định là rỗng
        model.addAttribute("employee", employeeDto);
        return "admin/employee-form";
    }


    @PostMapping("/admin-only/employee-management/add")
    public String addEmployee(@ModelAttribute("employee") EmployeeDto employeeDto, Model model) {
        try {
            System.out.println("Đang thêm nv với email: " + employeeDto.getEmail());
            EmployeeDto savedEmployee = employeeService.createEmployeeAdmin(employeeDto);
            System.out.println("✔ Thêm nv thành công: ID = " + savedEmployee.getId());
            return "redirect:/admin-only/employee-management";
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi thêm nhân viên: " + e.getMessage());
            return "admin/employee-form";
        }

    }

    @GetMapping("/admin-only/employee-management/detail/{id}")
    public String showDetailForm(@PathVariable Long id, Model model) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("title", "Chi tiết nhân viên");
        model.addAttribute("action", "#");
        model.addAttribute("readonly", true);
        return "admin/employee-edit";
    }

    @GetMapping("/admin-only/employee-management/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("title", "Chỉnh sửa nhân viên");
        model.addAttribute("action", "/admin-only/employee-management/save");
        model.addAttribute("readonly", false);
        return "admin/employee-edit";
    }
}