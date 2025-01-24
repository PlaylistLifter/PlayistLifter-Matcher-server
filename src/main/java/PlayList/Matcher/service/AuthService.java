package PlayList.Matcher.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Service
public class AuthService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.token-uri}")
    private String tokenUri;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;

    private String spotifyAccessToken;
    private long spotifyAccessExpires;

    public AuthService(WebClient.Builder webClientBuilder) {
        //this.webClient = webClientBuilder.baseUrl("https://spotify.com/v1/me").build();
        this.webClient = webClientBuilder.build();
    }

    public String requestAccessToken(String code) {
        //Basic Auth 헤더 생성
        String credentials= Base64.getEncoder().encodeToString((clientId+":"+clientSecret).getBytes());

        //Access Token 요청
        Map response=webClient.post()
                .uri(tokenUri)
                .header("Authorization", "Basic "+credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code&code="+code+"&redirect_uri="+redirectUri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        assert response!=null;
        return (String)response.get("access_token");
    }

}
