package com.project.DuAnTotNghiep.controller.user;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChinhSachDoiHangController {
    @GetMapping("doihang")
    public String getBuyGuild(Model model) {
        return "user/doi-hang";
    }
}
