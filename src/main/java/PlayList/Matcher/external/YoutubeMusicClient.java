package PlayList.Matcher.external;

import PlayList.Matcher.dto.PlaylistRequestDTO;
import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;

import java.util.List;

public class YoutubeMusicClient implements PlatformClient{
    @Override
    public List<Song> searchSongs(List<SongRequestDTO> songs) {
        // YouTube Music API 검색 구현
        return List.of();
    }

    @Override
    public String createPlaylist(PlaylistRequestDTO request) {
        // YouTube Music API 플레이리스트 생성 구현
        return "";
    }
}
