package com.taekang.streamingreactiveapi.DTO;

import com.taekang.streamingreactiveapi.entity.SportsType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class PerfectSportsLeagueDTO {

    private Long id;

    private String channelName;

    private String liveTitle;

    private String thumbnailUrl;

    private SportsType sportsType;
    
    private String sportsTypeSub;

    private String leagueName;

    private String streamUrl;

    private String streamUrlSub;

    private LocalDateTime leagueDate;

    private String home_name;

    private String away_name;

    private boolean important;

    private boolean live;

    private List<SportsLeagueBettingDTO> bettingList;
}
