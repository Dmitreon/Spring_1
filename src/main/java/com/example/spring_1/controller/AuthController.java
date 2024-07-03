package com.example.spring_1.controller;

import com.example.spring_1.dto.PasswordChangeDto;
import com.example.spring_1.dto.UserDto;
import com.example.spring_1.entity.User;
import com.example.spring_1.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setUserService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null, "There is already an account registered with the same email");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "register";
        }

        userService.saveUser(userDto);
        return "redirect:/register?success";
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication, @RequestParam(value = "passwordChanged", required = false) String passwordChanged) {
        String email = authentication.getName();
        User user = userService.findUserByEmail(email);
        UserDto userDto = new UserDto(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        model.addAttribute("user", userDto);
        if (passwordChanged != null) {
            model.addAttribute("passwordChangedMessage", "Password changed successfully.");
        }
        return "profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordChange", new PasswordChangeDto());
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChange") PasswordChangeDto passwordChangeDto,
                                 BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return "change-password";
        }

        String email = authentication.getName();
        User user = userService.findUserByEmail(email);

        if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPassword())) {
            result.rejectValue("oldPassword", null, "Old password is incorrect");
            return "change-password";
        }

        if (!passwordChangeDto.getNewPassword().equals(passwordChangeDto.getConfirmNewPassword())) {
            result.rejectValue("confirmNewPassword", null, "New password and Confirm password do not match");
            return "change-password";
        }

        userService.updatePassword(user, passwordChangeDto.getNewPassword());
        return "redirect:/profile?passwordChanged=true";
    }
}
