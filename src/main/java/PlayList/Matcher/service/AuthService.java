package PlayList.Matcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
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
    private String spotifyRefreshToken;
    private long spotifyAccessExpires;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public void forceExpireAccessToken(){//Token이 잘 갱신되는지 확인하기 위한 테스트 함수
        this.spotifyAccessExpires=System.currentTimeMillis()-1000;//1초만 빼도 과거가 된다
        log.info("Access Token 강제 만료");
    }
    public String requestAccessToken(String code) {//Authorization Code를 사용해서 Access Token, Refresh Token 함께 받아오기
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        Map response = webClient.post()
                .uri(tokenUri)
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirectUri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        assert response != null;
        this.spotifyAccessToken = (String) response.get("access_token");
        this.spotifyRefreshToken=(String) response.get("refresh_token");
        this.spotifyAccessExpires = System.currentTimeMillis() + ((Integer) response.get("expires_in")) * 1000L;

        log.info("Access token:"+spotifyAccessToken);
        log.info("Refresh token:"+spotifyRefreshToken);
        log.info("Expire time:"+spotifyAccessExpires);

        return spotifyAccessToken;
    }
    public String refreshAccessToken(){//Refresh Token 사용해서 새로운 Access Token 받아오기
        if(spotifyRefreshToken==null){
            throw new RuntimeException("No refresh token available. Please re-authenticate.");
        }
        String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

        Map response = webClient.post()
                .uri(tokenUri)
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=refresh_token&refresh_token=" + spotifyRefreshToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        assert response != null;
        this.spotifyAccessToken = (String) response.get("access_token");
        this.spotifyAccessExpires = System.currentTimeMillis() + ((Integer) response.get("expires_in")) * 1000L;

        log.info("Refreshed access token:"+spotifyAccessToken);
        log.info("New expire time:"+spotifyAccessExpires);

        return spotifyAccessToken;
    }
    public String getAccessToken() {
//        if (spotifyAccessToken == null || System.currentTimeMillis() > spotifyAccessExpires) {
//            throw new RuntimeException("Spotify access token expired. Please re-authenticate.");
//        }
        if(isAccessTokenExpired()){
            log.info("access token 만료. token을 refresh 합니다");
            return refreshAccessToken();
        }
        log.info("current access token:"+spotifyAccessToken);
        return spotifyAccessToken;
    }

    public boolean isAccessTokenExpired() {
        return spotifyAccessToken == null || System.currentTimeMillis() > spotifyAccessExpires;
    }

    public void logout() {
        this.spotifyAccessToken = null;
        this.spotifyRefreshToken = null;
        this.spotifyAccessExpires = 0L;
    }
}
