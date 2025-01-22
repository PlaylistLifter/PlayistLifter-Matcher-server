package PlayList.Matcher.controller;

import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;
import PlayList.Matcher.service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/match")
public class MatchController {
    /**
     * 클라이언트로부터 요청 수신 ( JSON 형식의 데이터 )
     * MatchService 호출
     * 처리 결과를 클라이언트에게 반환
     */
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/{platform")
    public List<Song> matchSongs(@RequestBody List<SongRequestDTO> songs, @PathVariable("platform") String platform) {
        return matchService.matchSongs(songs, platform);
    }
}
