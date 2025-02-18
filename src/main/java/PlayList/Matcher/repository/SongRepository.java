package PlayList.Matcher.repository;

import PlayList.Matcher.model.Song;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository  //
public class SongRepository {

    private final List<Song> songs = new ArrayList<>();

    // 노래 저장 (1개)
    public void save(Song song) {
        songs.add(song);
    }

    // 여러 개의 노래 저장
    public void saveAll(List<Song> newSongs) {
        songs.addAll(newSongs);
    }

    // 저장된 모든 노래 조회
    public List<Song> findAll() {
        return new ArrayList<>(songs); // 리스트 복사하여 반환 (원본 보호)
    }

    // 특정 아티스트의 노래 조회
    public List<Song> findByArtist(String artist) {
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (song.getArtist().equalsIgnoreCase(artist)) {
                result.add(song);
            }
        }
        return result;
    }
}
