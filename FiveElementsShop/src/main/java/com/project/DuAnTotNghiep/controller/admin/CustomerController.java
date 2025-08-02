package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

@Controller
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/admin-only/customer-management")
    public String showCustomerManagementPage(Model model, @PageableDefault(size = 1000) Pageable pageable) {
        Page<CustomerDto> customers = customerService.getAllCustomers(pageable);
        model.addAttribute("customers", customers);
        return "admin/customer-management";
    }

    @PostMapping("/admin-only/customer-management/save")
    public String saveCustomer(@ModelAttribute CustomerDto customerDto) {
        if (customerDto.getId() != null) {
            customerService.updateCustomer(customerDto.getId(), customerDto);
        } else {
            customerService.createCustomerAdmin(customerDto);
        }
        return "redirect:/admin-only/customer-management";
    }

    @GetMapping("/admin-only/customer-management/delete/{id}")
    public String deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "redirect:/admin-only/customer-management";
    }

    @GetMapping("/admin-only/customer-management/add")
    public String showAddForm(Model model) {
        CustomerDto customerDto = new CustomerDto();
        Customer lastCustomer = customerService.getLastCustomer();
        String autoCode = (lastCustomer == null) ? "KH0001" : "KH" + String.format("%04d", lastCustomer.getId() + 1);
        customerDto.setCode(autoCode);
        model.addAttribute("customer", customerDto);
        return "admin/customer-form";
    }

    @PostMapping("/admin-only/customer-management/add")
    public String addCustomer(@ModelAttribute("customer") CustomerDto customerDto, Model model) {
        try {
            System.out.println("Đang thêm khách hàng với email: " + customerDto.getEmail());
            CustomerDto savedCustomer = customerService.createCustomerAdmin(customerDto);
            System.out.println("✔ Thêm khách hàng thành công: ID = " + savedCustomer.getId());
            return "redirect:/admin-only/customer-management";
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm khách hàng: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi thêm khách hàng: " + e.getMessage());
            return "admin/customer-form";
        }

    }

    @GetMapping("/admin-only/customer-management/detail/{id}")
    public String showDetailForm(@PathVariable Long id, Model model) {
        CustomerDto customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("title", "Chi tiết khách hàng");
        model.addAttribute("action", "#");
        model.addAttribute("readonly", true);
        return "admin/customer-edit";
    }

    @GetMapping("/admin-only/customer-management/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CustomerDto customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        model.addAttribute("title", "Chỉnh sửa khách hàng");
        model.addAttribute("action", "/admin-only/customer-management/save");
        model.addAttribute("readonly", false);
        return "admin/customer-edit";
    }

}