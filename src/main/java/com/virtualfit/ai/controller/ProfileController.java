package com.virtualfit.ai.controller;

import com.virtualfit.ai.model.TryOnResult;
import com.virtualfit.ai.model.User;
import com.virtualfit.ai.repository.TryOnResultRepository;
import com.virtualfit.ai.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final TryOnResultRepository tryOnResultRepository;

    public ProfileController(UserRepository userRepository, TryOnResultRepository tryOnResultRepository) {
        this.userRepository = userRepository;
        this.tryOnResultRepository = tryOnResultRepository;
    }


    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String email = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) return "redirect:/login";

        User user = userOptional.get();
        model.addAttribute("user", user);

        return "profile";
    }

    @GetMapping("/history")
    public String history(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String email = auth.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) return "redirect:/login";

        User user = userOptional.get();
        List<TryOnResult> results = tryOnResultRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("results", results);

        return "history";
    }
}
