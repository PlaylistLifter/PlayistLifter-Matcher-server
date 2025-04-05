package PlayList.Matcher.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class LoginController {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spotify.authorization-uri}")
    private String authorizationUri;

    @Value("${spotify.scope}")
    private String scope;

    @GetMapping("/login")
    public String login() {
        log.info("[LoginController]");
        // URL 생성
        String url = authorizationUri
                + "?client_id="+clientId
                + "&response_type=code"
                + "&redirect_uri=" +redirectUri
                + "&scope=" + scope;
        //+ "&show_dialog=true";

        System.out.println("Generated Spotify Authorization URL: " + url);

        //Sptify 로그인 화면으로 리다이렉트
        return "redirect:" + url;
    }

}
