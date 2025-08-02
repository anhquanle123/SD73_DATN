package com.project.DuAnTotNghiep.controller.user;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class BuyGuildController {
    @GetMapping("buyguide")
    public String getBuyGuild(Model model) {
        return "user/buy-guide";
    }
}
