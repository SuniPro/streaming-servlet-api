package com.taekang.streamingreactiveapi.DTO;

import com.taekang.streamingreactiveapi.entity.SportsType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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

    private String homeName;
    private String awayName;
    private boolean important;
    private boolean live;
}
