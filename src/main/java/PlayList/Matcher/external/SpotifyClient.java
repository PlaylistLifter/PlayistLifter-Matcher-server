package PlayList.Matcher.external;

import PlayList.Matcher.dto.PlaylistRequestDTO;
import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;

import java.util.List;

public class SpotifyClient implements PlatformClient{


    @Override
    public List<Song> searchSongs(List<SongRequestDTO> songs) {
        // Spotify API 검색 구현
        return List.of();
    }

    @Override
    public String createPlaylist(PlaylistRequestDTO request) {
        // Spotify API 플레이리스트 생성 구현
        return "";
    }
}
