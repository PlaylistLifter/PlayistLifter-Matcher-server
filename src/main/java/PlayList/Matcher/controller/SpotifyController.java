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

    //http://localhost:8080/spotify/search?keyword=aespa
    @GetMapping("/search")
    public List<SearchResponseDto> searchTracks(@RequestParam String keyword) {
        return spotifyService.searchTracks(keyword);
    }

    //http://localhost:8080/spotify/match?artist=aespa&title=supernova
    @GetMapping("/match")
    public SearchResponseDto searchBestMatchTrack(
            @RequestParam String artist,
            @RequestParam String title) {
        return spotifyService.searchBestMatchTrack(artist, title);
    }
}
