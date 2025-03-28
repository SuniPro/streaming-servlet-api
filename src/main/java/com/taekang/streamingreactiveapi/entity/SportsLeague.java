package com.taekang.streamingreactiveapi.entity;

import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("sports_league")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
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
  private String homeName;

  @Column("away_name")
  private String awayName;

  @Column("important")
  private boolean important;

  @Column("live")
  private boolean live;
}
