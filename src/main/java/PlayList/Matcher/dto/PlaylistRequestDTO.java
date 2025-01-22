package PlayList.Matcher.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaylistRequestDTO {
    private String title;
    private List<String> songIds;
}
