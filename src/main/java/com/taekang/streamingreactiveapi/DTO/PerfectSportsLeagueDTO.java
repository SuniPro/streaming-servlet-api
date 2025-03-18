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

    private SportsType sportsType;

    private String leagueName;

    private String streamUrl;

    private LocalDateTime leagueDate;

    private String home_name;

    private String away_name;

    private boolean important;

    private List<SportsLeagueBettingDTO> bettingDTOList;
}
