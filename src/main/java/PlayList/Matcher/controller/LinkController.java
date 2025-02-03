package PlayList.Matcher.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/link")
public class LinkController {

    private final WebClient webClient;

    public LinkController(WebClient.Builder webClientBuilder, @Value("${python.server.url}") String pythonServerUrl) {
        this.webClient = webClientBuilder.baseUrl(pythonServerUrl).build();
    }

    @PostMapping("/send-link")
    public Mono<Map> sendYouTubeLink(@RequestBody Map<String, String> request) {

        if (!request.containsKey("youtubeUrl")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing 'youtubeUrl' in request.");
        }

        String youtubeUrl = request.get("youtubeUrl");
        System.out.println("Received YouTube URL: " + youtubeUrl);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("youtubeUrl", youtubeUrl);

        return webClient.post()
                .uri("/process-link")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class);
    }
}
