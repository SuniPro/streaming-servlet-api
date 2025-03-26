package com.taekang.streamingreactiveapi.service;

import com.taekang.streamingreactiveapi.DTO.PerfectSportsLeagueDTO;
import com.taekang.streamingreactiveapi.DTO.SportsLeagueBettingDTO;
import com.taekang.streamingreactiveapi.DTO.SportsLeagueDTO;
import com.taekang.streamingreactiveapi.entity.SportsLeague;
import com.taekang.streamingreactiveapi.entity.SportsLeagueBetting;
import com.taekang.streamingreactiveapi.entity.SportsType;
import com.taekang.streamingreactiveapi.repository.leagueInfo.SportsLeagueBettingRepository;
import com.taekang.streamingreactiveapi.repository.leagueInfo.SportsLeagueRepository;
import io.r2dbc.spi.R2dbcException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SportsLeagueInfoServiceImpl implements SportsLeagueInfoService {

  private final SportsLeagueRepository sportsLeagueRepository;
  private final SportsLeagueBettingRepository sportsLeagueBettingRepository;
  private final StreamingSiteFetcherService streamingSiteFetcherService;
  private final R2dbcEntityTemplate r2dbcEntityTemplate;
  private final ModelMapper modelMapper;

  @Autowired
  public SportsLeagueInfoServiceImpl(
      SportsLeagueRepository sportsLeagueRepository,
      SportsLeagueBettingRepository sportsLeagueBettingRepository,
      StreamingSiteFetcherService streamingSiteFetcherService,
      R2dbcEntityTemplate r2dbcEntityTemplate,
      ModelMapper modelMapper) {
    this.sportsLeagueRepository = sportsLeagueRepository;
    this.sportsLeagueBettingRepository = sportsLeagueBettingRepository;
    this.streamingSiteFetcherService = streamingSiteFetcherService;
    this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    this.modelMapper = modelMapper;
  }

  @Override
  public Flux<PerfectSportsLeagueDTO> createSportsLeague(
      List<PerfectSportsLeagueDTO> perfectSportsLeagueDTOList) {
    LocalDateTime leagueDate = LocalDateTime.now();

    return Flux.fromIterable(perfectSportsLeagueDTOList)
        .flatMap(
            dto -> {
              Mono<String> streamUrlMono =
                  dto.getStreamUrl().contains("chzzk")
                      ? streamingSiteFetcherService.getChzzkStreamingUrl(dto.getStreamUrl())
                      : Mono.defer(
                          () -> {
                            try {
                              return streamingSiteFetcherService.getSoopStreamingUrl(
                                  dto.getStreamUrl());
                            } catch (URISyntaxException e) {
                              return Mono.error(new RuntimeException(e));
                            }
                          });

              return streamUrlMono.flatMap(
                  streamUrl -> {
                    SportsLeague sportsLeague =
                        SportsLeague.builder()
                            .channelName(dto.getChannelName())
                            .liveTitle(dto.getLiveTitle())
                            .thumbnailUrl(dto.getThumbnailUrl())
                            .sportsType(dto.getSportsType())
                            .sportsTypeSub(dto.getSportsTypeSub())
                            .streamUrl(streamUrl)
                            .leagueDate(leagueDate)
                            .leagueName(dto.getLeagueName())
                            .home_name(dto.getHome_name())
                            .away_name(dto.getAway_name())
                            .important(dto.isImportant())
                            .build();

                    return sportsLeagueRepository
                        .save(sportsLeague)
                        .flatMap(
                            savedLeague -> {
                              List<SportsLeagueBetting> bettingList =
                                  dto.getBettingList().stream()
                                      .map(
                                          bettingDTO ->
                                              SportsLeagueBetting.builder()
                                                  .sportsLeagueId(savedLeague.getId())
                                                  .bettingType(bettingDTO.getBettingType())
                                                  .bettingName(bettingDTO.getBettingName())
                                                  .homeOddsName(bettingDTO.getHomeOddsName())
                                                  .awayOddsName(bettingDTO.getAwayOddsName())
                                                  .homeOdds(bettingDTO.getHomeOdds())
                                                  .awayOdds(bettingDTO.getAwayOdds())
                                                  .build())
                                      .collect(Collectors.toList());

                              return sportsLeagueBettingRepository
                                  .saveAll(bettingList)
                                  .then(Mono.just(dto));
                            });
                  });
            });
  }

  @Override
  public Flux<SportsLeagueBetting> updateBettingList(
      Long sportLeagueId, List<SportsLeagueBettingDTO> sportsLeagueBettingDTOList) {

    return Flux.fromIterable(sportsLeagueBettingDTOList)
        .flatMap(
            sportsLeagueBettingDTO ->
                sportsLeagueRepository
                    .findById(sportLeagueId)
                    .flatMap(
                        league -> {
                          SportsLeagueBetting sportsLeagueBetting =
                              SportsLeagueBetting.builder()
                                  .id(sportsLeagueBettingDTO.getId())
                                  .sportsLeagueId(league.getId())
                                  .bettingType(sportsLeagueBettingDTO.getBettingType())
                                  .bettingName(sportsLeagueBettingDTO.getBettingName())
                                  .homeOdds(sportsLeagueBettingDTO.getHomeOdds())
                                  .homeOddsName(sportsLeagueBettingDTO.getHomeOddsName())
                                  .awayOdds(sportsLeagueBettingDTO.getAwayOdds())
                                  .awayOddsName(sportsLeagueBettingDTO.getAwayOddsName())
                                  .build();

                          return sportsLeagueBettingRepository.save(sportsLeagueBetting);
                        }));
  }

  @Override
  public Mono<Void> deleteLeagueInfoById(Long id) {
    return sportsLeagueRepository.deleteById(id);
  }

  @Override
  public Flux<SportsLeague> getAllLeagueInfoByImportant() {
    try {
      return sportsLeagueRepository.findAllByImportant(true);
    } catch (Exception e) {
      log.info("Error getting all league info {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public Mono<List<SportsLeagueDTO>> getAllSportsLeague(int page, int size) {
    try {
      int offset = page * size;
      LocalDate yesterday = LocalDate.now().minusDays(1);
      LocalDateTime startOfYesterday = yesterday.atStartOfDay();
      LocalDateTime endOfYesterday = yesterday.atTime(LocalTime.MAX);

      return r2dbcEntityTemplate
          .select(SportsLeague.class)
          .matching(
              Query.query(Criteria.where("live").isTrue())
                  .sort(Sort.by(Sort.Order.desc("league_date")).and(Sort.by(Sort.Order.desc("id"))))
                  .limit(size)
                  .offset(offset))
          .all()
          .map(league -> modelMapper.map(league, SportsLeagueDTO.class)) // 💡 개별 변환
          .collectList();
    } catch (R2dbcException e) {
      log.error("Database error: {}", e.getMessage());
      return Mono.error(new RuntimeException("Database error occurred", e));
    }
  }

  @Override
  public Mono<List<PerfectSportsLeagueDTO>> getLeagueInfoBySportsType(
      SportsType sportsType, int page, int size) {
    try {
      int offset = 0;

      return r2dbcEntityTemplate
          .select(SportsLeague.class)
          .matching(
              Query.query(Criteria.where("sportsType").is(sportsType).and("live").isTrue())
                  .sort(Sort.by(Sort.Order.desc("important")))
                  .sort(Sort.by(Sort.Order.desc("league_date")))
                  .sort(Sort.by(Sort.Order.desc("id")))
                  .limit(size)
                  .offset(offset))
          .all()
          .flatMap(
              league ->
                  r2dbcEntityTemplate
                      .select(SportsLeagueBetting.class)
                      .matching(Query.query(Criteria.where("sports_league_id").is(league.getId())))
                      .all()
                      .collectList()
                      .flatMap(
                          bettingList -> {
                            List<SportsLeagueBettingDTO> bettingDTOList =
                                bettingList.stream()
                                    .map(
                                        betting ->
                                            modelMapper.map(
                                                betting,
                                                SportsLeagueBettingDTO.class)) // 엔티티를 DTO로 변환
                                    .collect(Collectors.toList());

                            return Mono.just(
                                PerfectSportsLeagueDTO.builder()
                                    .id(league.getId())
                                    .channelName(league.getChannelName())
                                    .liveTitle(league.getLiveTitle())
                                    .thumbnailUrl(league.getThumbnailUrl())
                                    .sportsType(league.getSportsType())
                                    .sportsTypeSub(league.getSportsTypeSub())
                                    .leagueName(league.getLeagueName())
                                    .streamUrl(league.getStreamUrl())
                                    .streamUrlSub(league.getStreamUrlSub())
                                    .leagueDate(league.getLeagueDate())
                                    .home_name(league.getHome_name())
                                    .away_name(league.getAway_name())
                                    .important(league.isImportant())
                                    .live(league.isLive())
                                    .bettingList(bettingDTOList) // DTO 리스트 설정
                                    .build());
                          }))
          .collectList();
    } catch (Exception e) {
      log.info("Error getting all perfect sports league info {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public Mono<PerfectSportsLeagueDTO> getLeagueInfoById(Long id) {
    return sportsLeagueRepository
        .findById(id)
        .flatMap(
            league ->
                sportsLeagueBettingRepository
                    .findBySportsLeagueId(league.getId())
                    .collectList()
                    .map(
                        bettingList -> {
                          // ✅ DTO 변환
                          List<SportsLeagueBettingDTO> bettingDTOList =
                              bettingList.stream()
                                  .map(
                                      betting ->
                                          SportsLeagueBettingDTO.builder()
                                              .sportsLeagueId(betting.getSportsLeagueId())
                                              .bettingType(betting.getBettingType())
                                              .bettingName(betting.getBettingName())
                                              .homeOdds(betting.getHomeOdds())
                                              .homeOddsName(betting.getHomeOddsName())
                                              .awayOdds(betting.getAwayOdds())
                                              .awayOddsName(betting.getAwayOddsName())
                                              .build())
                                  .collect(Collectors.toList());

                          return PerfectSportsLeagueDTO.builder()
                              .id(league.getId())
                              .sportsType(league.getSportsType())
                              .leagueName(league.getLeagueName())
                              .streamUrl(league.getStreamUrl())
                              .leagueDate(league.getLeagueDate())
                              .home_name(league.getHome_name())
                              .away_name(league.getAway_name())
                              .important(league.isImportant())
                              .bettingList(bettingDTOList)
                              .build();
                        }));
  }
}
