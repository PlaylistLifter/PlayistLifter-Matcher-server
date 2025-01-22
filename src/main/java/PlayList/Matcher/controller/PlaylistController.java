package PlayList.Matcher.controller;

import PlayList.Matcher.dto.PlaylistRequestDTO;
import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;
import PlayList.Matcher.service.MatchService;
import PlayList.Matcher.service.PlaylistService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class PlaylistController {
    /**
     * 플레이리스트 생성 요청 수신
     * PlaylistService 호출
     * 생성된 플레이리스트 URL 반환
     */
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/{platform")
    public String matchSongs(@RequestBody PlaylistRequestDTO request, @PathVariable("platform") String platform) {
        return playlistService.createPlaylist(request, platform);
    }
}
