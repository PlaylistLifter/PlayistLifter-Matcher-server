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
import java.util.List;

@Slf4j
@Service
public class SpotifyService {
    private final MatchedTrackRepository matchedTrackRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final AuthService authService;
    private final SpotifyMapper spotifyMapper;

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
            System.out.println("Error: " + e.getMessage());
        }

        return searchResponseDtoList;
    }
    public SearchResponseDto searchBestMatchTrack(String artistName, String title) {
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

                //검색된 곡을 인메모리 저장소에 저장
                matchedTrackRepository.saveTrack(matchedTrack);
                System.out.println("Saved to repository: " + matchedTrack.getTitle()+ " by " + matchedTrack.getArtistName());

                int trackCnt = matchedTrackRepository.getAllTracks().size();
                System.out.println("trackCnt = " + trackCnt);

                return matchedTrack; // 가장 첫 번째 결과 반환
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return getDefaultResponse();
    }

    public List<SearchResponseDto> searchBestMatchForAllSongs(){
        matchedTrackRepository.clearTracks();
        List<SearchResponseDto> matchedTracks= new ArrayList<>();
        List<YoutubePlaylist> youtubePlaylists =playlistRepository.findAll();

        for(YoutubePlaylist youtubePlaylist : youtubePlaylists){
            log.info("Playlist: "+ youtubePlaylist.getTitle());

            for(Song song: youtubePlaylist.getSongs()){
                log.info("Song: "+song.getArtist()+" "+song.getTitle());

                SearchResponseDto result=searchBestMatchTrack(song.getArtist(), song.getTitle());

                if(result!=null)matchedTracks.add(result);
            }
        }
        return matchedTracks;
    }
    public List<SearchResponseDto> searchBestMatchForLatestPlaylist(){
        matchedTrackRepository.clearTracks();
        List<YoutubePlaylist> youtubePlaylists = playlistRepository.findAll();

        if(youtubePlaylists.isEmpty()){
            return new ArrayList<>();
        }

        // 최신 플레이리스트만 선택
        YoutubePlaylist latest = youtubePlaylists.get(youtubePlaylists.size() - 1);
        log.info("Processing latest playlist: " + latest.getTitle());

        List<SearchResponseDto> matchedTracks = new ArrayList<>();
        for(Song song: latest.getSongs()){
            log.info("Song: " + song.getArtist() + " " + song.getTitle());
            SearchResponseDto result = searchBestMatchTrack(song.getArtist(), song.getTitle());
            if(result != null) {
                matchedTracks.add(result);
            }
        }
        return matchedTracks;
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

            // 인메모리 저장소에서 저장된 곡 가져오기
            List<SearchResponseDto> matchedTracks = matchedTrackRepository.getAllTracks();
            List<String> trackUris = new ArrayList<>();
            for (SearchResponseDto track : matchedTracks) {
                System.out.println("Title= " + track.getTitle() + " Artist= " + track.getArtistName());
                String uri = getTrackUri(spotifyApi, track.getArtistName(), track.getTitle());
                if (uri != null) {
                    trackUris.add(uri);
                }
            }

            // 플레이리스트에 트랙 추가
            if (!trackUris.isEmpty()) {
                AddItemsToPlaylistRequest addItemsRequest = spotifyApi
                        .addItemsToPlaylist(playlistId, trackUris.toArray(new String[0]))
                        .build();
                addItemsRequest.execute();
            }

            return playlist.getExternalUrls().get("spotify"); // 생성된 플레이리스트 URL 반환

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return "Error creating playlist";
        }
    }

    private String getTrackUri(SpotifyApi spotifyApi, String artist, String title) {
        try {
            String query = "artist:" + artist + " track:" + title;
            SearchTracksRequest searchTrackRequest = spotifyApi.searchTracks(query).limit(1).build();
            Track[] tracks = searchTrackRequest.execute().getItems();
            return (tracks.length > 0) ? tracks[0].getUri() : null;
        } catch (Exception e) {
            System.out.println("Error fetching track URI: " + e.getMessage());
            return null;
        }
    }

    private SearchResponseDto convertTrackToDto(Track track) {
        String title = track.getName();
        AlbumSimplified album = track.getAlbum();
        ArtistSimplified[] artists = album.getArtists();
        String artistName = (artists.length > 0) ? artists[0].getName() : "Unknown Artist";
        Image[] images = album.getImages();
        String imageUrl = (images.length > 0) ? images[0].getUrl() : "NO_IMAGE";
        String albumName = album.getName();

        return spotifyMapper.toSearchDto(artistName, title, albumName, imageUrl);
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
            System.out.println("Error fetching user ID: " + e.getMessage());
            return "Unknown User";
        }
    }
}
