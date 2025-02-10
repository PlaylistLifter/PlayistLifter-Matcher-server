package PlayList.Matcher.repository;

import PlayList.Matcher.dto.SearchResponseDto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MatchedTrackRepository {
    private final List<SearchResponseDto> matchedTracks = new ArrayList<>();

    //매칭된 트랙 저장
    public void saveTrack(SearchResponseDto track) {
        matchedTracks.add(track);
    }

    //저장된 모든 트랙 조희
    public List<SearchResponseDto> getAllTracks() {
        return new ArrayList<>(matchedTracks);
    }

    //저장된 트랙 초기화
    public void clearTracks() {
        matchedTracks.clear();
    }
}
