package PlayList.Matcher.controller;

import PlayList.Matcher.model.Song;
import PlayList.Matcher.repository.SongRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongRepository songRepository; // InMemory Repository 주입

    public SongController(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    // Flask에서 받은 노래 저장 (POST /api/songs/add)
    @PostMapping("/add")
    public ResponseEntity<List<Song>> receiveSongs(@RequestBody List<Song> songs) {
        System.out.println("=== 🎵 Received Songs from Flask ===");
        songRepository.saveAll(songs); // InMemorySongRepository에 저장

        for (Song song : songs) {
            System.out.println("🎶 Artist: " + song.getArtist() + ", Title: " + song.getTitle());
        }

        return ResponseEntity.ok(songRepository.findAll()); // 저장된 데이터 반환
    }

    // 모든 노래 조회 (GET /api/songs)
    @GetMapping("")
    public ResponseEntity<List<Song>> getAllSongs() {
        return ResponseEntity.ok(songRepository.findAll());
    }

    // 특정 아티스트의 노래 조회 (GET /api/songs/artist/{artist})
    @GetMapping("/artist/{artist}")
    public ResponseEntity<List<Song>> getSongsByArtist(@PathVariable String artist) {
        return ResponseEntity.ok(songRepository.findByArtist(artist));
    }
}
