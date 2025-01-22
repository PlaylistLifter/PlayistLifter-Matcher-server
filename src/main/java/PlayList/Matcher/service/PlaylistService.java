package PlayList.Matcher.service;

import PlayList.Matcher.dto.PlaylistRequestDTO;
import PlayList.Matcher.model.Playlist;

public interface PlaylistService {
    /**
     * APPLEMUSIC API를 통해서 플레이리스트 생성
     * JWT 생성 및 인증 처리
     */
    String createPlaylist(PlaylistRequestDTO request, String platform);
}
