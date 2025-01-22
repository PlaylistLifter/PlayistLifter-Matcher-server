package PlayList.Matcher.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/spotify")
    public String authenticateSpotify(@RequestBody String credentials) {
        // Spotify 인증 처리
        return "Spotify Auth Token";
    }

    @PostMapping("/youtubeMusic")
    public String authenticateYTM(@RequestBody String credentials) {
        // YouTube Music 인증 처리
        return "YouTube Music Auth Token";
    }

}
