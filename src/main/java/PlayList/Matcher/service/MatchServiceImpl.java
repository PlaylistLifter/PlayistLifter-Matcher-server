package PlayList.Matcher.service;

import PlayList.Matcher.dto.SongRequestDTO;
import PlayList.Matcher.model.Song;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchServiceImpl implements MatchService {
    @Override
    public List<Song> matchSongs(List<SongRequestDTO> songs, String platform) {
        return List.of();
    }
}
