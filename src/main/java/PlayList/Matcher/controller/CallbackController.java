package PlayList.Matcher.controller;

import PlayList.Matcher.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
public class CallbackController {

    @Autowired
    private AuthService authService;
    private static final Logger log = Logger.getLogger(CallbackController.class.getName());

    //스포티파이 개발자 계정에 등록해놓은 redirect url이 http://localhost:8080/callback
    @GetMapping("/callback")
    public String callback(@RequestParam String code){
        String accessToken = authService.requestAccessToken(code);

        log.info("[CallbackController] Access token stored: " + accessToken);

        return "Received Access Token";

    }

}