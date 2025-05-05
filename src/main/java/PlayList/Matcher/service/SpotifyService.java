package PlayList.Matcher.service;

import PlayList.Matcher.dto.SearchResponseDto;
import PlayList.Matcher.model.YoutubePlaylist;
import PlayList.Matcher.model.Song;
import PlayList.Matcher.repository.MatchedTrackRepository;
import PlayList.Matcher.repository.PlaylistRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SpotifyService {
    private final MatchedTrackRepository matchedTrackRepository;
    private final AuthService authService;
    private final SpotifyMapper spotifyMapper;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    // 이미 처리된 곡들을 추적하기 위한 Set
    private final Set<String> processedSongs = new HashSet<>();

    public SpotifyService(AuthService authService, SpotifyMapper spotifyMapper, MatchedTrackRepository matchedTrackRepository) {
        this.authService = authService;
        this.spotifyMapper = spotifyMapper;
        this.matchedTrackRepository = matchedTrackRepository;
    }

    public List<SearchResponseDto> searchTracks(String keyword) {
        List<SearchResponseDto> searchResponseDtoList = new ArrayList<>();

        try {
            String accessToken = authService.getAccessToken();
            SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

            SearchTracksRequest searchTrackRequest = spotifyApi.searchTracks(keyword)
                    .limit(10)
                    .build();

            Paging<Track> searchResult = searchTrackRequest.execute();
            Track[] tracks = searchResult.getItems();

            for (Track track : tracks) {
                searchResponseDtoList.add(convertTrackToDto(track));
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error searching tracks: {}", e.getMessage());
        }

        return searchResponseDtoList;
    }

    public SearchResponseDto searchBestMatchTrack(String artistName, String title) {
        // 중복 체크를 위한 키 생성
        String songKey = artistName.trim().toLowerCase() + ":" + title.trim().toLowerCase();

        try {
            String accessToken = authService.getAccessToken();
            SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

            // 정확한 검색을 위해 "artist:아티스트명 track:노래제목" 형식으로 검색
            String query = "artist:" + artistName + " track:" + title;
            SearchTracksRequest searchTrackRequest = spotifyApi.searchTracks(query)
                    .limit(1) // 가장 적합한 한 개의 결과만 반환
                    .build();

            Paging<Track> searchResult = searchTrackRequest.execute();
            Track[] tracks = searchResult.getItems();

            if (tracks.length > 0) {
                SearchResponseDto matchedTrack = convertTrackToDto(tracks[0]);
                // 원본 아티스트와 제목 정보 추가
                matchedTrack.setOriginalArtist(artistName);
                matchedTrack.setOriginalTitle(title);
                matchedTrack.setSpotifyTrackId(tracks[0].getId());

                // 매칭된 아티스트와 제목 정보 추가
                matchedTrack.setMatchedArtist(matchedTrack.getArtistName());
                matchedTrack.setMatchedTitle(matchedTrack.getTitle());

                // 저장소에 저장하기 전에 중복 체크
                if (!isDuplicateInRepository(matchedTrack)) {
                    matchedTrackRepository.saveTrack(matchedTrack);
                    log.info("Saved to repository: {} by {}", matchedTrack.getTitle(), matchedTrack.getArtistName());
                    int trackCnt = matchedTrackRepository.getAllTracks().size();
                    log.debug("trackCnt = {}", trackCnt);
                } else {
                    log.info("Skipped duplicate: {} by {}", matchedTrack.getTitle(), matchedTrack.getArtistName());
                }

                return matchedTrack; // 가장 첫 번째 결과 반환
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error searching for track: {}", e.getMessage());
        }

        SearchResponseDto notFound = getDefaultResponse();
        notFound.setOriginalArtist(artistName);
        notFound.setOriginalTitle(title);
        // 매칭 실패시에도 매칭된 필드 설정 (빈 값으로)
        notFound.setMatchedArtist("");
        notFound.setMatchedTitle("");
        return notFound;
    }

    // 중복 체크 메서드 추가
    private boolean isDuplicateInRepository(SearchResponseDto track) {
        String trackKey = track.getSpotifyTrackId();
        if (trackKey == null || trackKey.isEmpty()) {
            return false;
        }

        List<SearchResponseDto> existingTracks = matchedTrackRepository.getAllTracks();
        for (SearchResponseDto existingTrack : existingTracks) {
            if (trackKey.equals(existingTrack.getSpotifyTrackId())) {
                return true;
            }
        }
        return false;
    }

    public List<SearchResponseDto> searchBestMatchForAllSongs() {
        // 매칭 시작 전 저장소와 중복 추적 세트 초기화
        matchedTrackRepository.clearTracks();
        processedSongs.clear();

        List<SearchResponseDto> matchedTracks = new ArrayList<>();
        List<YoutubePlaylist> youtubePlaylists = playlistRepository.findAll();

        // 마지막 플레이리스트만 처리하도록 변경
        if (!youtubePlaylists.isEmpty()) {
            YoutubePlaylist latestPlaylist = youtubePlaylists.get(youtubePlaylists.size() - 1);
            log.info("Processing playlist: {}", latestPlaylist.getTitle());

            for (Song song : latestPlaylist.getSongs()) {
                String songKey = song.getArtist().trim().toLowerCase() + ":" + song.getTitle().trim().toLowerCase();

                // 이미 처리한 곡인지 확인
                if (!processedSongs.contains(songKey)) {
                    processedSongs.add(songKey);
                    log.info("Song: {} {}", song.getArtist(), song.getTitle());

                    SearchResponseDto result = searchBestMatchTrack(song.getArtist(), song.getTitle());
                    if (result != null) {
                        matchedTracks.add(result);
                    }
                } else {
                    log.info("Skipping already processed song: {} - {}", song.getArtist(), song.getTitle());
                }
            }
        }

        return matchedTracks;
    }

    // 이 메서드는 기존 대로 유지하되, searchBestMatchForAllSongs 메서드만 사용하도록 리다이렉트 처리
    public List<SearchResponseDto> searchBestMatchForLatestPlaylist() {
        return searchBestMatchForAllSongs();
    }

    public String createPlaylist(String userId, String playlistName) {
        try {
            String accessToken = authService.getAccessToken();
            SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

            // 플레이리스트 생성
            CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                    .public_(false)
                    .description("Generated Playlist")
                    .build();

            var playlist = createPlaylistRequest.execute();
            String playlistId = playlist.getId();

            // 인메모리 저장소에서 저장된 곡 가져오기 (중복 제거)
            List<SearchResponseDto> matchedTracks = matchedTrackRepository.getAllTracks();
            List<String> trackUris = new ArrayList<>();
            Set<String> addedTrackIds = new HashSet<>();

            for (SearchResponseDto track : matchedTracks) {
                // 스포티파이 ID가 있고 유효한 경우만 추가
                if (track.getSpotifyTrackId() != null && !track.getSpotifyTrackId().isEmpty()) {
                    // 이미 추가된 트랙인지 확인
                    if (!addedTrackIds.contains(track.getSpotifyTrackId())) {
                        log.info("Adding to playlist: {} by {}", track.getTitle(), track.getArtistName());
                        String uri = "spotify:track:" + track.getSpotifyTrackId();
                        trackUris.add(uri);
                        addedTrackIds.add(track.getSpotifyTrackId());
                    }
                }
            }

            // 플레이리스트에 트랙 추가
            if (!trackUris.isEmpty()) {
                int batchSize = 100; // Spotify API 제한
                for (int i = 0; i < trackUris.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, trackUris.size());
                    List<String> batch = trackUris.subList(i, endIndex);

                    AddItemsToPlaylistRequest addItemsRequest = spotifyApi
                            .addItemsToPlaylist(playlistId, batch.toArray(new String[0]))
                            .build();
                    addItemsRequest.execute();
                }
            }

            return playlist.getExternalUrls().get("spotify"); // 생성된 플레이리스트 URL 반환

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error creating playlist: {}", e.getMessage());
            return "Error creating playlist: " + e.getMessage();
        }
    }

    private String getTrackUri(SpotifyApi spotifyApi, String artist, String title) {
        try {
            String query = "artist:" + artist + " track:" + title;
            SearchTracksRequest searchTrackRequest = spotifyApi.searchTracks(query).limit(1).build();
            Track[] tracks = searchTrackRequest.execute().getItems();
            return (tracks.length > 0) ? tracks[0].getUri() : null;
        } catch (Exception e) {
            log.error("Error fetching track URI: {}", e.getMessage());
            return null;
        }
    }

    private SearchResponseDto convertTrackToDto(Track track) {
        String title = track.getName();
        AlbumSimplified album = track.getAlbum();
        ArtistSimplified[] artists = album.getArtists();
        String artistName = (artists.length > 0) ? artists[0].getName() : "Unknown Artist";
        Image[] images = album.getImages();

        String imageUrl;
        if (images != null && images.length > 0) {
            imageUrl = images[0].getUrl();
            log.info("✅ 이미지 URL: {}", imageUrl);
        } else {
            imageUrl = "NO_IMAGE";
            log.warn("❌ 이미지 없음: {} - {}", title, artistName);
        }

        String albumName = album.getName();

        SearchResponseDto dto = spotifyMapper.toSearchDto(artistName, title, albumName, imageUrl);
        dto.setImageUrl(imageUrl);
        dto.setSpotifyTrackId(track.getId()); // Spotify 트랙 ID 저장
        return dto;
    }

    private SearchResponseDto getDefaultResponse() {
        return SearchResponseDto.builder()
                .artistName("Unknown Artist")
                .title("Unknown Title")
                .albumName("Unknown Album")
                .imageUrl("NO_IMAGE")
                .build();
    }

    public String getCurrentUserId() {
        try {
            String accessToken = authService.getAccessToken();
            SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

            // 사용자 프로필 정보 요청
            GetCurrentUsersProfileRequest userProfileRequest = spotifyApi.getCurrentUsersProfile().build();
            User user = userProfileRequest.execute();

            return user.getId(); // Spotify 사용자 ID 반환
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error("Error fetching user ID: {}", e.getMessage());
            return "Unknown User";
        }
    }

    public User getCurrnetUserProfile() {
        try {
            // 액세스 토큰 획득
            String accessToeken = authService.getAccessToken();

            //spotifyApi 인스턴스 생성
            SpotifyApi spotifyApi = new SpotifyApi.Builder()
                    .setAccessToken(accessToeken).build();

            // 현재 프로필 요청
            GetCurrentUsersProfileRequest request = spotifyApi.getCurrentUsersProfile().build();
            User user = request.execute();

            return user;
        } catch (Exception e) {
            // 토큰 만료, 네트워크 에러 등 처리
            log.error("Error fetching user profile: {}", e.getMessage());
            throw new RuntimeException("Error fetching user profile: " + e.getMessage());
        }
    }
}