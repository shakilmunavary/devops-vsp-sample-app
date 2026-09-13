package com.controller;

import com.model.User;
import com.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"/", "/dashboard", "/users"})
    public String dashboard(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("user", new User());
        return "dashboard";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteUserPost(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return "redirect:/dashboard";
    }

    @ResponseBody
    @GetMapping("/api/users")
    public List<User> getUsersApi() {
        return userRepository.findAll();
    }

    @ResponseBody
    @GetMapping("/ping")
    public String ping() {
        return "App is healthy and running on H2 In-Memory DB";
    }
}
