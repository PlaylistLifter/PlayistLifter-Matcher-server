package PlayList.Matcher.controller;

import PlayList.Matcher.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestingController {//token이 자동으로 갱신되는지 테스트하는 controller
    private final AuthService authService;

    public TestingController(AuthService authService){
        this.authService=authService;
    }

    @GetMapping("/expire-token")
    public String expireAccessToken(){
        authService.forceExpireAccessToken();
        return("access token 강제 만료");
    }

    @GetMapping("/get-token")
    public String getAccessToken(){
        return("현재 access token: "+authService.getAccessToken());
    }
}
