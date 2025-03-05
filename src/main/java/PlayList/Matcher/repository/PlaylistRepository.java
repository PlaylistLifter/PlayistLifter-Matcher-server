package PlayList.Matcher.repository;

import PlayList.Matcher.model.YoutubePlaylist;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PlaylistRepository {
    private final List<YoutubePlaylist> youtubePlaylistStorage = new ArrayList<>();

    public void save(YoutubePlaylist youtubePlaylist) {
        youtubePlaylistStorage.add(youtubePlaylist);
    }

    public List<YoutubePlaylist> findAll() {
        return new ArrayList<>(youtubePlaylistStorage);
    }

    public void clear() {
        youtubePlaylistStorage.clear();
    }
}
