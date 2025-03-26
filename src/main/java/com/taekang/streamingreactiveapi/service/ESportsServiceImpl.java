package com.taekang.streamingreactiveapi.service;

import com.taekang.streamingreactiveapi.DTO.ESportsPlayerInfoDTO;
import com.taekang.streamingreactiveapi.restAPI.RestRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ESportsServiceImpl implements ESportsService {

  @Value("${riot.api.key}")
  private String riotApiKey;

  private final RestRequest restRequest;
  private final StreamingSiteFetcherService streamingSiteFetcherService;

  @Autowired
  public ESportsServiceImpl(
      RestRequest restRequest, StreamingSiteFetcherService streamingSiteFetcherService) {
    this.restRequest = restRequest;
    this.streamingSiteFetcherService = streamingSiteFetcherService;
  }

  @Override
  public Mono<ESportsPlayerInfoDTO> getPlayerInfoRecentTwenty(String playerName, String tag) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Riot-Token", riotApiKey);

    ESportsPlayerInfoDTO playerInfoDTO = new ESportsPlayerInfoDTO();
    playerInfoDTO.setPlayerName(playerName);

    String puuidUrl =
        "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/"
            + playerName
            + "/"
            + tag;

    return restRequest
        .get(puuidUrl, headers)
        .flatMap(streamingSiteFetcherService::parseJsonToMap)
        .flatMap(
            accountInfoJson -> {
              String puuid = (String) accountInfoJson.get("puuid");
              if (puuid == null) return Mono.error(new RuntimeException("Puuid 없음"));

              String matchListUrl =
                  "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/"
                      + puuid
                      + "/ids?start=0&count=5";

              return restRequest
                  .get(matchListUrl, headers)
                  .flatMap(streamingSiteFetcherService::parseJsonToList)
                  .flatMap(
                      matchIds -> {
                        playerInfoDTO.setMatchLength(matchIds.size());

                        return Flux.fromIterable(matchIds)
                            .flatMap(
                                matchId -> {
                                  String matchDetailUrl =
                                      "https://asia.api.riotgames.com/lol/match/v5/matches/"
                                          + matchId;

                                  return restRequest
                                      .get(matchDetailUrl, headers)
                                      .flatMap(streamingSiteFetcherService::parseJsonToMap)
                                      .flatMap(
                                          matchJson -> {
                                              log.info("matchId: {} matchDetailUrl: {}", matchId, matchDetailUrl);
                                            Map<String, Object> metadata =
                                                (Map<String, Object>) matchJson.get("metadata");
                                            List<String> participants =
                                                (List<String>) metadata.get("participants");
                                            int index = participants.indexOf(puuid);
                                            if (index == -1) return Mono.empty();

                                            Map<String, Object> info =
                                                (Map<String, Object>) matchJson.get("info");
                                            List<Map<String, Object>> participantInfos =
                                                (List<Map<String, Object>>)
                                                    info.get("participants");
                                            Map<String, Object> playerStats =
                                                participantInfos.get(index);

                                            boolean win =
                                                Boolean.TRUE.equals(playerStats.get("win"));
                                            boolean firstBloodKill =
                                                Boolean.TRUE.equals(
                                                    playerStats.get("firstBloodKill"));
                                            int kills = (Integer) playerStats.get("kills");

                                            if (win)
                                              playerInfoDTO.setWinLength(
                                                  playerInfoDTO.getWinLength() + 1);
                                            if (firstBloodKill)
                                              playerInfoDTO.setFirstKillLength(
                                                  playerInfoDTO.getFirstKillLength() + 1);
                                            playerInfoDTO.setKillLength(
                                                playerInfoDTO.getKillLength() + kills);

                                            log.info("playerInfoDTO: {}", playerInfoDTO.toString());

                                            return Mono.empty();
                                          });
                                })
                            .then(
                                Mono.defer(
                                    () -> {
                                      int matchLength = playerInfoDTO.getMatchLength();
                                      BigDecimal winRatio =
                                          matchLength == 0
                                              ? BigDecimal.ZERO
                                              : BigDecimal.valueOf(playerInfoDTO.getWinLength())
                                                  .multiply(BigDecimal.valueOf(100))
                                                  .divide(
                                                      BigDecimal.valueOf(matchLength),
                                                      2,
                                                      RoundingMode.HALF_UP);

                                      BigDecimal firstKillRatio =
                                          matchLength == 0
                                              ? BigDecimal.ZERO
                                              : BigDecimal.valueOf(
                                                      playerInfoDTO.getFirstKillLength())
                                                  .multiply(BigDecimal.valueOf(100))
                                                  .divide(
                                                      BigDecimal.valueOf(matchLength),
                                                      2,
                                                      RoundingMode.HALF_UP);

                                      playerInfoDTO.setWinRatio(winRatio);
                                      playerInfoDTO.setFirstKillRatio(firstKillRatio);

                                      return Mono.just(playerInfoDTO);
                                    }));
                      });
            });
  }
}
