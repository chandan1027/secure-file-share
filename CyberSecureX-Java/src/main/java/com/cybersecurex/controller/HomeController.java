package com.cybersecurex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Secure Login");
        model.addAttribute("message", "Login to access file sharing");
        return "index";
    }

    @GetMapping("/file-share")
    public String fileShare(Model model) {
        model.addAttribute("title", "File Sharing");
        return "file-share";
    }

}
