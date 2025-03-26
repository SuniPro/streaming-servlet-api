package com.taekang.streamingreactiveapi.DTO;

import com.taekang.streamingreactiveapi.entity.SportsType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SportsLeagueDTO {

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
}
