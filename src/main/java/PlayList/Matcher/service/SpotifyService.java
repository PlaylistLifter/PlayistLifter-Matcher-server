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

        try{
            //엑세스 토큰 가져오기
            //String accessToken = authService.getAccessToken();
            String accessToken = "BQBsBzCQzwDRVMA1pfoPyZk2ljvBzwTLm9GlR3y_yMwjExy5jX4pS3EpbaE2-J0PNcDYVueVOBGAxiKqK2JzvyBK2JL7X-NEmdEUZCYWAKo3NPyx39oLHtC4WO-ymURBmKp3AysjOcRpFMbyXJbQGAi_NyPP_2fx69EKWZ-O72DLyhLUZEC5es72GR_1tBQRgoV1W-D3ACtfljzri_sjfF6FBc7veo6999dicIZjKMoS0kryNHQUk64ds2XVdXlV0OCxSMwuBP_t-GM-n8puiojIe08ktaLNDith0w-g";
            //Spotify API 빌드
            System.out.println("API build에 사용된 accessToken = " + accessToken);
            SpotifyApi spotifyApi = new SpotifyApi.Builder().setAccessToken(accessToken).build();

            //트랙 검색 요쳥
            System.out.println("입력된 keyword = " + keyword);
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(keyword).limit(10).build();

            Paging<Track> searchResult = searchTracksRequest.execute();
            Track[] tracks = searchResult.getItems();

            for(Track track : tracks){
                String title = track.getName();

                AlbumSimplified album = track.getAlbum();

                ArtistSimplified[] artists = album.getArtists();

                String artistName;
                if (artists.length > 0) {
                    artistName = artists[0].getName();
                } else {
                    artistName = "Unknown Artist";
                }

                String imageUrl;
                Image[] images = album.getImages();
                if (artists.length > 0) {
                    imageUrl = images[0].getUrl();
                } else {
                    imageUrl = "No Image";
                }

                String albumName = album.getName();

                searchResponseDtoList.add(spotifyMapper.toSearchDto(artistName,title, albumName, imageUrl));
            }

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return searchResponseDtoList;
    }
}
