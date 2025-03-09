package PlayList.Matcher.controller;

import PlayList.Matcher.dto.SearchResponseDto;
import PlayList.Matcher.model.YoutubePlaylist;
import PlayList.Matcher.repository.PlaylistRepository;
import PlayList.Matcher.service.SpotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/spotify")
@RestController
public class SpotifyController {

    private final SpotifyService spotifyService;

    @Autowired
    private PlaylistRepository playlistRepository;

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

    //http://localhost:8080/spotify/create-playlist?name=MyPlaylist
    @GetMapping("/create-playlist")
    public String createPlaylist(@RequestParam String name) {
        String userId = spotifyService.getCurrentUserId(); // 현재 로그인한 사용자 ID 가져오기
        return spotifyService.createPlaylist(userId, name);
    }

    // http://localhost:8080/spotify/user-id
    @GetMapping("/user-id")
    public String getUserId() {
        return spotifyService.getCurrentUserId();
    }

    //http://localhost:8080/spotify/match-all
    @GetMapping("/match-all")
    public List<SearchResponseDto> matchAllSongs(){
        return spotifyService.searchBestMatchForAllSongs();
    }

    //http://localhost:8080/spotify/create-playlist2
    @GetMapping("/create-playlist2")
    public String createPlaylist2(){
        List<YoutubePlaylist> youtubePlaylists =playlistRepository.findAll();

        YoutubePlaylist latest= youtubePlaylists.get(youtubePlaylists.size()-1);
        String title= latest.getTitle();

        String userId = spotifyService.getCurrentUserId();
        return spotifyService.createPlaylist(userId, title);
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUserProfile(){
        User user = spotifyService.getCurrnetUserProfile();

        // 원하는 형태로 JSON 응답을 생성
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("displayName", user.getDisplayName());
        response.put("images", user.getImages());
        response.put("email", user.getEmail());
        return response;
    }

}
