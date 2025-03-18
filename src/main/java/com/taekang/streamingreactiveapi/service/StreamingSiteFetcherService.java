package com.taekang.streamingreactiveapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taekang.streamingreactiveapi.repository.leagueInfo.SportsLeagueRepository;
import com.taekang.streamingreactiveapi.restAPI.RestRequest;
import com.taekang.streamingreactiveapi.tool.Tools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StreamingSiteFetcherService {
  private final RestRequest restRequest;
  private final ObjectMapper objectMapper;
  private final SportsLeagueRepository sportsRepository;

  @Autowired
  public StreamingSiteFetcherService(
      RestRequest restRequest, ObjectMapper objectMapper, SportsLeagueRepository sportsRepository) {
    this.restRequest = restRequest;
    this.objectMapper = objectMapper;
    this.sportsRepository = sportsRepository;
  }

  public Mono<String> getGameHTML(String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("content-type", "text/html");

    return restRequest.get(url, headers);
  }

  public Mono<Object> fetchData(String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");

    return restRequest.get(url, headers).flatMap(this::processJsonResponse); // 응답을 처리하는 메서드로 연결
  }

  public Mono<String> getStreamingUrl(String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");

    try {
      String apiUrl =
          "https://api.chzzk.naver.com/service/v2/channels/"
              + Tools.getPathSegments(url, 2)
              + "/live-detail";
      return restRequest
          .get(apiUrl, headers)
          .flatMap(this::parseHlsUrl)
          .onErrorResume(
              e -> {
                log.error("Error processing URL {}: {}", apiUrl, e.getMessage());
                return Mono.just("Error");
              });

    } catch (Exception e) {
      log.error("Invalid URL {}: {}", url, e.getMessage());
      return Mono.just("Error");
    }
  }

  private Mono<Object> processJsonResponse(String json) {
    log.info("Processing json response: {}", json);
    try {
      // JSON이 배열인지 확인
      if (json.trim().startsWith("[")) {
        return convertListJson(json).cast(Object.class); // 배열 처리
      } else {
        return convertJson(json).cast(Object.class); // 단일 객체 처리
      }
    } catch (Exception e) {
      return Mono.error(new RuntimeException("JSON 처리 오류", e));
    }
  }

  private Mono<Map<String, Object>> convertJson(String json) {
    try {
      Map<String, Object> responseMap = objectMapper.readValue(json, new TypeReference<>() {});
      return Mono.just(responseMap);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("JSON 변환 오류", e));
    }
  }

  private Mono<List<Map<String, Object>>> convertListJson(String json) {
    try {
      List<Map<String, Object>> responseList =
          objectMapper.readValue(json, new TypeReference<>() {});
      return Mono.just(responseList);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("JSON 배열 변환 오류", e));
    }
  }

  public List<String> extractHLSPaths(Map<String, Object> livePlaybackJson) {
    if (livePlaybackJson.containsKey("media") && livePlaybackJson.get("media") instanceof List) {
      List<Map<String, Object>> mediaList =
          (List<Map<String, Object>>) livePlaybackJson.get("media");

      return mediaList.stream()
          .filter(
              media ->
                  "HLS".equals(media.get("mediaId"))
                      && media.containsKey("path")) // 🔥 mediaId가 "HLS"인 것만 필터링
          .map(media -> (String) media.get("path")) // 🔥 path 값만 가져오기
          .collect(Collectors.toList());
    }
    return List.of(); // media가 없으면 빈 리스트 반환
  }

  private Mono<String> parseHlsUrl(String json) {
    String hlsPath = "";
    try {
      Map<String, Object> responseMap = objectMapper.readValue(json, new TypeReference<>() {});
      if (responseMap.containsKey("content") && responseMap.get("content") instanceof Map) {
        Map<String, Object> contentMap = (Map<String, Object>) responseMap.get("content");
        if (contentMap.containsKey("livePlaybackJson")
            && contentMap.get("livePlaybackJson") instanceof String) {
          Map<String, Object> livePlaybackJsonMap =
              objectMapper.readValue(
                  contentMap.get("livePlaybackJson").toString(), new TypeReference<>() {});
          contentMap.put("livePlaybackJson", livePlaybackJsonMap);

          // 🔥 HLS media에서 path 값만 추출
          List<String> hlsPaths = extractHLSPaths(livePlaybackJsonMap);
          log.info("Extracted HLS Paths: {}", hlsPaths);

          hlsPath = hlsPaths.get(0);
        }
      }
    } catch (Exception e) {
      return Mono.error(e);
    }

    return Mono.just(hlsPath);
  }
}
