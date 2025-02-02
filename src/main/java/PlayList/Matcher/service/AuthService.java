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
        this.webClient = webClientBuilder.build();
    }

    public String requestAccessToken(String code) {
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
        this.spotifyAccessExpires = System.currentTimeMillis() + ((Integer) response.get("expires_in")) * 1000L;
        return spotifyAccessToken;
    }

    public String getAccessToken() {
        if (spotifyAccessToken == null || System.currentTimeMillis() > spotifyAccessExpires) {
            throw new RuntimeException("Spotify access token expired. Please re-authenticate.");
        }
        return spotifyAccessToken;
    }

    public boolean isAccessTokenExpired() {
        return spotifyAccessToken == null || System.currentTimeMillis() > spotifyAccessExpires;
    }
}
