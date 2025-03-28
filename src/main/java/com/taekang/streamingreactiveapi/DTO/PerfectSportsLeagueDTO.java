package com.taekang.streamingreactiveapi.DTO;

import com.taekang.streamingreactiveapi.entity.SportsType;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder(toBuilder = true)
@ToString
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

  private String homeName;

  private String awayName;

  private boolean important;

  private boolean live;

  @Nullable private List<SportsLeagueBettingDTO> bettingList;
}
