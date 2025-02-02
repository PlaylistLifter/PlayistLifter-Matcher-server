package PlayList.Matcher.controller;

import PlayList.Matcher.dto.SearchResponseDto;
import PlayList.Matcher.service.SpotifyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/spotify")
@RestController
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    //http://localhost:8080/spotify/search?keyword=Coldplay
    @GetMapping("/search")
    public List<SearchResponseDto> searchTracks(@RequestParam String keyword) {
        return spotifyService.searchTracks(keyword);
    }
}
