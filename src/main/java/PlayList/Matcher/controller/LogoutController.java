package PlayList.Matcher.controller;

import PlayList.Matcher.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LogoutController {
    private final AuthService authService;

    public LogoutController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/logout")
    public String logout(@RequestParam(required = false) String redirect) {
        authService.logout();
        if ("login".equals(redirect)) {
            return "redirect:/login";
        }
        return "redirect:/index.html";
    }
}
