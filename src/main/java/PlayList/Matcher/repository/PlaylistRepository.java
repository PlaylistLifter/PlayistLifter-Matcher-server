package PlayList.Matcher.repository;

import PlayList.Matcher.model.Playlist;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PlaylistRepository {
    private final List<Playlist> playlistStorage = new ArrayList<>();

    public void save(Playlist playlist) {
        playlistStorage.add(playlist);
    }

    public List<Playlist> findAll() {
        return new ArrayList<>(playlistStorage);
    }
}
