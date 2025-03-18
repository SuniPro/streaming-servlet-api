package com.taekang.streamingreactiveapi.service;

import com.taekang.streamingreactiveapi.DTO.PerfectSportsLeagueDTO;
import com.taekang.streamingreactiveapi.DTO.SportsLeagueBettingDTO;
import com.taekang.streamingreactiveapi.entity.SportsLeague;
import com.taekang.streamingreactiveapi.entity.SportsLeagueBetting;
import com.taekang.streamingreactiveapi.entity.SportsType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SportsLeagueInfoService {

    Flux<PerfectSportsLeagueDTO> createSportsLeague(List<PerfectSportsLeagueDTO> perfectSportsLeagueDTOList);

    Flux<SportsLeagueBetting> updateBettingList(Long sportLeagueId, List<SportsLeagueBettingDTO> sportsLeagueBettingDTOList);

    Flux<SportsLeague> deleteLeagueInfoById(Long id);

    Flux<SportsLeague> getAllLeagueInfo();

    Mono<List<PerfectSportsLeagueDTO>> getAllPerfectSportsLeagueDTO(int page, int size);

    Mono<List<PerfectSportsLeagueDTO>> getLeagueInfoBySportsType(SportsType sportsType, int page, int size);

    Mono<PerfectSportsLeagueDTO> getLeagueInfoById(Long id);

}
