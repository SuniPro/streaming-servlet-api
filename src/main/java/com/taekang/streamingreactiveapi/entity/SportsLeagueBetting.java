package com.taekang.streamingreactiveapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("sports_league_betting")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SportsLeagueBetting {

    @Id
    @Column("id")
    private Long id;

    @Column("sports_league_id")
    private Long sportsLeagueId;

    @Column("betting_type")
    private String bettingType;

    @Column("betting_name")
    private String bettingName;

    @Column("home_odds_name")
    private String homeOddsName;

    @Column("home_odds")
    private BigDecimal homeOdds;

    @Column("away_odds_name")
    private String awayOddsName;

    @Column("away_odds")
    private BigDecimal awayOdds;
}
