package com.taekang.streamingreactiveapi.DTO;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SportsLeagueBettingDTO {
    private Long id;

    private Long sportsLeagueId;

    private String bettingType;

    private String bettingName;

    private String homeOddsName;
    private BigDecimal homeOdds;

    private String awayOddsName;
    private BigDecimal awayOdds;

}
