package PlayList.Matcher.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthService {
    private final WebClient webClient;

    private String spotifyAccessToken;
    private long spotifyAccessExpires;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://spotify.com/v1/me").build();
    }

}
