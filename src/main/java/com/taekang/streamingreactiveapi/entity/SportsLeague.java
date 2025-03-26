package com.taekang.streamingreactiveapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("sports_league")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SportsLeague {

  @Id
  @Column("id")
  private Long id;

  @Column("channel_name")
  private String channelName;

  @Column("live_title")
  private String liveTitle;

  @Column("thumbnail_url")
  private String thumbnailUrl;

  @Column("sports_type")
  private SportsType sportsType;

  @Column("sports_type_sub")
  private String sportsTypeSub;

  @Column("league_name")
  private String leagueName;

  @Column("stream_url")
  private String streamUrl;

  @Column("stream_url_sub")
  private String streamUrlSub;

  @Column("league_date")
  private LocalDateTime leagueDate;

  @Column("home_name")
  private String home_name;

  @Column("away_name")
  private String away_name;

  @Column("important")
  private boolean important;

  @Column("live")
  private boolean live;
}
