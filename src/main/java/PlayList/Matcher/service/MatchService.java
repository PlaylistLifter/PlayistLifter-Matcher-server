package PlayList.Matcher.service;

import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;

import java.util.List;

public interface MatchService {
    /**
     * 노래 매칭 로직 담당
     * Apple Music API를 통해 곡 검색 및 매칭 수행
     */
    List<Song> matchSongs(List<SongRequestDTO> songs, String platform);
}
