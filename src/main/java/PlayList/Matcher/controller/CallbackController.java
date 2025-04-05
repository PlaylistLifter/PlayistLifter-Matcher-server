package PlayList.Matcher.controller;

import PlayList.Matcher.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.logging.Logger;

@Controller
public class CallbackController {

    @Autowired
    private AuthService authService;
    private static final Logger log = Logger.getLogger(CallbackController.class.getName());

//    //스포티파이 개발자 계정에 등록해놓은 redirect url이 http://localhost:8080/callback이다
//    @GetMapping("/callback")
//    public String callback(@RequestParam String code){
//        String accessToken = authService.requestAccessToken(code);
//
//        log.info("[CallbackController] Access token stored: " + accessToken);
//
//        return "redirect:/http://localhost:3000";
//    }
@GetMapping("/callback")
public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
    String accessToken = authService.requestAccessToken(code);

    log.info("[CallbackController] Access token stored: " + accessToken);

    // React 앱으로 리디렉션!
    response.sendRedirect("http://localhost:3000");
}


}