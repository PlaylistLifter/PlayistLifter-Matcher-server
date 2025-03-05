package PlayList.Matcher.controller;

import PlayList.Matcher.model.YoutubePlaylist;
import PlayList.Matcher.repository.PlaylistRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

    private final PlaylistRepository playlistRepository;

    public PlaylistController(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    // Flask에서 받은 YouTube 제목 & 노래 목록 저장 (POST /api/playlist/add)
    @PostMapping("/add")
    public ResponseEntity<List<YoutubePlaylist>> receivePlaylist(@RequestBody YoutubePlaylist youtubePlaylist) {
        System.out.println("=== 🎵 Received Playlist from Flask ===");

        playlistRepository.save(youtubePlaylist);

        System.out.println("🎬 Title: " + youtubePlaylist.getTitle());
        for (var song : youtubePlaylist.getSongs()) {
            System.out.println("🎶 Artist: " + song.getArtist() + ", Title: " + song.getTitle());
        }

        return ResponseEntity.ok(playlistRepository.findAll()); // 저장된 데이터 반환
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearPlaylist() {
        playlistRepository.clear();
        return ResponseEntity.ok("Playlist repository cleared");
    }

    // 저장된 모든 플레이리스트 조회 (GET /api/playlist)
    @GetMapping("")
    public ResponseEntity<List<YoutubePlaylist>> getAllPlaylists() {
        return ResponseEntity.ok(playlistRepository.findAll());
    }
}
