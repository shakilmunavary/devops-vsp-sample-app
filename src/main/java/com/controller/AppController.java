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

    @PostMapping("/trigger-error")
    public String triggerError() {
        // Intentionally insert a 651-character string into the name column (VARCHAR 255)
        // This generates the exact SQLDataException 22001 column truncation error for AI SRE triage
        String longPayload = "http://localhost:7000/dashboard/users/registration/callback/verify?token="
                + "A".repeat(500)
                + "&session_id=sre_error_simulation_test_2026";
        User errorUser = new User();
        errorUser.setName(longPayload);
        errorUser.setEmail("sre-test@example.com");
        userRepository.save(errorUser);
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
