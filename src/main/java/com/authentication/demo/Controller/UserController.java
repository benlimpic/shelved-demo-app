package com.authentication.demo.Controller;

import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.authentication.demo.Service.CollectionService;
import com.authentication.demo.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {

  private final AuthenticationManager authenticationManager;
  private final UserService userService;

  public UserController(@Lazy UserService userService, AuthenticationManager authenticationManager,
      CollectionService collectionService) {
    this.userService = userService;
    this.authenticationManager = authenticationManager;
  }

  // UPDATE USER USERNAME
  @PostMapping("/update_username")
  public String updateUsername(@RequestParam String username, RedirectAttributes redirectAttributes) {
    String result = userService.updateUsername(username);
    if ("Username updated successfully".equals(result)) {
      redirectAttributes.addFlashAttribute("message", result);
      return "redirect:/profile";
    } else {
      redirectAttributes.addFlashAttribute("error", result);
      return "redirect:/update-user-details";
    }
  }

  // UPDATE USER FULL NAME
  @PostMapping("/update_name")
  public String updateName(@RequestParam String firstName, @RequestParam String lastName,
      RedirectAttributes redirectAttributes) {
    Map<String, String> nameDetails = Map.of("firstName", firstName, "lastName", lastName);
    String result = userService.updateName(nameDetails);
    if ("Name updated successfully".equals(result)) {
      redirectAttributes.addFlashAttribute("message", result);
      return "redirect:/profile";
    } else {
      redirectAttributes.addFlashAttribute("error", result);
      return "redirect:/update-user-details";
    }
  }

  // UPDATE USER EMAIL
  @PostMapping("/update_email")
  public String updateEmail(@RequestParam String newEmail, @RequestParam String confirmNewEmail,
      RedirectAttributes redirectAttributes) {
    String result = userService.updateEmail(newEmail, confirmNewEmail);
    if ("Email updated successfully".equals(result)) {
      redirectAttributes.addFlashAttribute("message", result);
      return "redirect:/profile";
    } else {
      redirectAttributes.addFlashAttribute("error", result);
      return "redirect:/update-user-details";
    }
  }

  // UPDATE USER PASSWORD
  @PostMapping("/update_password")
  public String updatePassword(@RequestParam String currentPassword, @RequestParam String newPassword,
      @RequestParam String confirmNewPassword, RedirectAttributes redirectAttributes) {
    String result = userService.updatePassword(currentPassword, newPassword, confirmNewPassword);
    if ("Password updated successfully".equals(result)) {
      redirectAttributes.addFlashAttribute("message", result);
      return "redirect:/profile";
    } else {
      redirectAttributes.addFlashAttribute("error", result);
      return "redirect:/update-user-details";
    }
  }

  // UPDATE USER PROFILE DETAILS
  @PostMapping("/update_profile")
  public String updateProfile(
      @RequestParam Map<String, String> userDetails,
      @RequestParam MultipartFile profilePicture,
      RedirectAttributes redirectAttributes) {
    try {
      // Delegate the logic to the UserService
      userService.updateProfile(userDetails, profilePicture, redirectAttributes);

      // Assuming the method sets flash attributes directly, redirect to the profile page
      return "redirect:/profile";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while updating the profile.");
      return "redirect:/update-profile";
    }
  }

  // CREATE USER
  @PostMapping("/signup")
  public String createUser(@RequestParam Map<String, String> userDetails,
      RedirectAttributes redirectAttributes) {

    Map<String, String> result = userService.postUser(userDetails);

    if ("error".equals(result.get("status"))) {
      redirectAttributes.addFlashAttribute("error", result.get("message"));
      return "redirect:/signup";
    }

    redirectAttributes.addFlashAttribute("message", result.get("message"));
    return "redirect:/login";
  }

  // LOGIN USER
@PostMapping("/login")
public String login(@RequestParam String username, @RequestParam String password,
    RedirectAttributes redirectAttributes) {
  try {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    return "redirect:/index";
  } catch (AuthenticationException e) {
    redirectAttributes.addFlashAttribute("error", "Invalid username or password");
    return "redirect:/login";
  } catch (Exception e) {
    redirectAttributes.addFlashAttribute("error", "An unexpected error occurred");
    return "redirect:/login";
  }
}

  // LOGOUT USER
@PostMapping("/logout")
public String logout(HttpServletRequest request) {
    request.getSession().setAttribute("demo_logged_out", true);
    SecurityContextHolder.clearContext();
    // Invalidate the session
    request.getSession().invalidate();

    // Redirect to login with the `logout` parameter
    return "redirect:/login?logout";
  }

  // DELETE USER
  @PostMapping("/delete_user")
  public String deleteUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
    try {
      String result = userService.deleteUser(username);
      if ("User deleted successfully".equals(result)) {
        redirectAttributes.addFlashAttribute("message", result);
        System.out.println("Delete result: " + result);
        return "redirect:/login";

      } else {
        redirectAttributes.addFlashAttribute("error", result);
        System.out.println("Delete error: " + result);
        return "redirect:/update-profile";
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while deleting the user.");
      System.out.println("Delete exception: " + e.getMessage());
      return "redirect:/update-profile";
    }
  }

}