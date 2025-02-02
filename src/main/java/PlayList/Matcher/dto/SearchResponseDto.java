package PlayList.Matcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SearchResponseDto {
    private String artistName;
    private String title;
    private String albumName;
    private String imageUrl;
}
