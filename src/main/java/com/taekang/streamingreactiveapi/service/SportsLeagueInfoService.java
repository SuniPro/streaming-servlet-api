package com.taekang.streamingreactiveapi.service;

import com.taekang.streamingreactiveapi.DTO.*;
import com.taekang.streamingreactiveapi.entity.SportsLeague;
import com.taekang.streamingreactiveapi.entity.SportsLeagueBetting;
import com.taekang.streamingreactiveapi.entity.SportsType;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SportsLeagueInfoService {

  Flux<PerfectSportsLeagueDTO> createSportsLeague(
      List<PerfectSportsLeagueDTO> perfectSportsLeagueDTOList);

  Mono<SportsLeague> updateSportsLeague(SportsLeagueDTO sportsLeagueDTO);

  Mono<SportsLeague> updateImportant(LeagueImportantDTO importantDTO);

  Mono<SportsLeague> updateLive(LeagueLiveDTO leagueLiveDTO);

  Flux<SportsLeagueBetting> updateBettingList(
      Long sportLeagueId, List<SportsLeagueBettingDTO> sportsLeagueBettingDTOList);

  Flux<SportsLeague> getAllLeagueInfoByImportant();

  Mono<List<SportsLeagueDTO>> getAllSportsLeagueIsLive(int page, int size);

  Mono<List<SportsLeagueDTO>> getAllSportsLeague(int page, int size);

  Mono<List<PerfectSportsLeagueDTO>> getLeagueInfoBySportsType(
      SportsType sportsType, int page, int size);

  Mono<PerfectSportsLeagueDTO> getLeagueInfoById(Long id);

  Mono<Void> deleteLeagueInfoById(Long id);
}
