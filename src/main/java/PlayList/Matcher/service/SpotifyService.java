package PlayList.Matcher.service;

import PlayList.Matcher.dto.SearchResponseDto;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SpotifyService {
    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final AuthService authService;
    private final SpotifyMapper spotifyMapper;

    public SpotifyService(AuthService authService, SpotifyMapper spotifyMapper) {
        this.authService = authService;
        this.spotifyMapper = spotifyMapper;
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
                return convertTrackToDto(tracks[0]); // 가장 첫 번째 결과 반환
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return SearchResponseDto.builder()
                .artistName("Unknown Artist")
                .title("Unknown Title")
                .albumName("Unknown Album")
                .imageUrl("NO_IMAGE")
                .build();
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
}
