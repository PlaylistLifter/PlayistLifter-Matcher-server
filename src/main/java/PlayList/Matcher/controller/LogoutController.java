package PlayList.Matcher.controller;

import PlayList.Matcher.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController // ✅ @RestController로 변경
@RequestMapping("/api") // ✅ 명확한 경로 추가
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
public class LogoutController {
    private final AuthService authService;

    public LogoutController(AuthService authService) {
        this.authService = authService;
    }

//    @GetMapping("/logout")
//    public String logout(@RequestParam(required = false) String redirect) {
//        authService.logout();
//        if ("login".equals(redirect)) {
//            return "redirect:/login";
//        }
//        return "redirect:/index.html";
//    }

//    @PostMapping("/logout")
//    public ResponseEntity<String> logout(){
//        log.info("로그아웃 수신");
//        authService.logout();
//
//        return ResponseEntity.ok("로그아웃 완료");
//        // String redirectUrl=(redirect!=null&&redirect.equals("login"))?"/login":"/";
////        String logoutUrl = "https://www.spotify.com/logout/";
////        return ResponseEntity.status(302)
////                .header(HttpHeaders.LOCATION, logoutUrl)
////                .build();
//    }

//    @PostMapping("/change-account")
//    public ResponseEntity<String> changeAccount(){
//        log.info("계정변경 수신");
//
//        authService.logout();
//
//        return ResponseEntity.ok("/login");
//    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("[LogoutController] 로그아웃 호출됨");
        authService.logout(); // access token + refresh token 모두 삭제
        return ResponseEntity.ok().build();
    }

    @GetMapping("/change-account")
    public ResponseEntity<Void> changeAccount() {
        log.info("[LogoutController] 계정 변경 호출됨");
        authService.logout(); // 토큰 초기화
        return ResponseEntity.ok().build();
    }
}
