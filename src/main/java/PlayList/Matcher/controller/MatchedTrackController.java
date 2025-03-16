package PlayList.Matcher.controller;

import PlayList.Matcher.dto.SearchResponseDto;
import PlayList.Matcher.repository.MatchedTrackRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matched")
public class MatchedTrackController {
    private final MatchedTrackRepository matchedTrackRepository;

    public MatchedTrackController(MatchedTrackRepository matchedTrackRepository) {
        this.matchedTrackRepository = matchedTrackRepository;
    }

    @GetMapping("/tracks")
    public ResponseEntity<List<SearchResponseDto>> getMatchedTracks() {
        List<SearchResponseDto> tracks = matchedTrackRepository.getAllTracks();
        return ResponseEntity.ok(tracks);
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearMatchedTracks() {
        matchedTrackRepository.clearTracks();
        return ResponseEntity.ok("Matched track repository cleared");
    }

}
