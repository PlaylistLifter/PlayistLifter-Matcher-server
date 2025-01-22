package PlayList.Matcher.external;

import PlayList.Matcher.dto.PlaylistRequestDTO;
import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;

import java.util.List;

public interface PlatformClient {

    //노래 검색 메서드
    List<Song> searchSongs(List<SongRequestDTO> songs);

    //플레이리스트 생성 메서드
    String createPlaylist(PlaylistRequestDTO request);
}
