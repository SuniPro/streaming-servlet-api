package com.taekang.streamingreactiveapi.DTO;

import com.taekang.streamingreactiveapi.entity.SportsType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SportsLeagueDTO {

    private Long id;

    private SportsType sportsType;
    private String leagueName;
    private String streamUrl;
    private LocalDateTime leagueDate;

    private String home_name;
    private String away_name;
    private boolean important;
}
