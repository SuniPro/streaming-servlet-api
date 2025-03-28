package com.taekang.streamingreactiveapi.controller;

import com.taekang.streamingreactiveapi.DTO.LeagueImportantDTO;
import com.taekang.streamingreactiveapi.DTO.LeagueLiveDTO;
import com.taekang.streamingreactiveapi.DTO.PerfectSportsLeagueDTO;
import com.taekang.streamingreactiveapi.DTO.SportsLeagueDTO;
import com.taekang.streamingreactiveapi.entity.SportsLeague;
import com.taekang.streamingreactiveapi.entity.SportsType;
import com.taekang.streamingreactiveapi.service.SportsLeagueInfoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("league")
public class LeagueController {

  private final SportsLeagueInfoService sportsLeagueInfoService;

  @Autowired
  public LeagueController(
      SportsLeagueInfoService sportsLeagueInfoService) {
    this.sportsLeagueInfoService = sportsLeagueInfoService;
  }

  @PostMapping("create")
  public Flux<PerfectSportsLeagueDTO> createLeagueInfo(
      @RequestBody List<PerfectSportsLeagueDTO> perfectSportsLeagueDTOList) {

    return sportsLeagueInfoService.createSportsLeague(perfectSportsLeagueDTOList);
  }

  @PostMapping("update/important")
  public Mono<SportsLeague> updateLeagueImportant(
          @RequestBody LeagueImportantDTO leagueImportantDTO) {

    return sportsLeagueInfoService.updateImportant(leagueImportantDTO);
  }

  @PostMapping("update/live")
  public Mono<SportsLeague> updateLeagueLive(
          @RequestBody LeagueLiveDTO leagueLiveDTO) {

    return sportsLeagueInfoService.updateLive(leagueLiveDTO);
  }

  @GetMapping("read/all/by/important")
  public Flux<SportsLeague> getAllLeagueInfoByImportant() {
    return sportsLeagueInfoService.getAllLeagueInfoByImportant();
  }

  @GetMapping("read/live/all/{page}/{size}")
  public Mono<List<SportsLeagueDTO>> getAllLeagueInfoIsLive(@PathVariable int page, @PathVariable int size) {
    return sportsLeagueInfoService.getAllSportsLeagueIsLive(page, size);
  }

  @GetMapping("read/all/{page}/{size}")
  public Mono<List<SportsLeagueDTO>> getAllLeagueInfo(@PathVariable int page, @PathVariable int size) {
    return sportsLeagueInfoService.getAllSportsLeague(page, size);
  }

  @GetMapping("read/{sportsType}")
  public Mono<List<PerfectSportsLeagueDTO>> getLeagueInfoBySportsType(
      @PathVariable SportsType sportsType, @RequestParam int page, @RequestParam int size) {
    return sportsLeagueInfoService.getLeagueInfoBySportsType(sportsType, page, size);
  }

  @GetMapping("read/by/{id}")
  public Mono<PerfectSportsLeagueDTO> getLeagueInfoById(@PathVariable Long id) {
    return sportsLeagueInfoService.getLeagueInfoById(id);
  }

  @DeleteMapping("delete/by/{id}")
  public Mono<Void> deleteLeagueInfoById(@PathVariable Long id) {
    return sportsLeagueInfoService.deleteLeagueInfoById(id);
  }

}
