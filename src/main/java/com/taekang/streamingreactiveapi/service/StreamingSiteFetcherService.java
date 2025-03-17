package com.taekang.streamingreactiveapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taekang.streamingreactiveapi.restAPI.RestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StreamingSiteFetcherService {
  private final RestRequest restRequest;
  private final ObjectMapper objectMapper;

  @Autowired
  public StreamingSiteFetcherService(RestRequest restRequest, ObjectMapper objectMapper) {
    this.restRequest = restRequest;
    this.objectMapper = objectMapper;
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

  public Mono<List<Map<String, String>>> getStreamingUrl(List<String> urls) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    List<Map<String, String>> errorMapList = new ArrayList<>();
    Map<String, String> errorResponseMap = new HashMap<>();
    errorResponseMap.put("liveTitle", "Error");
    errorResponseMap.put("hlsPath", "Error");
    errorMapList.add(errorResponseMap);

    return Flux.fromIterable(urls) // 🔥 URL 리스트를 Flux로 변환
            .flatMap(url -> restRequest.get(url, headers)
                    .flatMap(this::parseHlsUrl)
                    .onErrorResume(e -> { // 🔥 요청 실패 시 기본 에러 데이터 반환
                      log.error("Error processing URL {}: {}", url, e.getMessage());
                      return Mono.just(errorResponseMap);
                    })
            )
            .collectList() // 🔥 결과를 List로 변환
            .onErrorReturn(errorMapList); // 🔥 전체 실패 시 errorMapList 반환
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
      List<Map<String, Object>> responseList = objectMapper.readValue(json, new TypeReference<>() {});
      return Mono.just(responseList);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("JSON 배열 변환 오류", e));
    }
  }

  public List<String> extractHLSPaths(Map<String, Object> livePlaybackJson) {
    if (livePlaybackJson.containsKey("media") && livePlaybackJson.get("media") instanceof List) {
      List<Map<String, Object>> mediaList = (List<Map<String, Object>>) livePlaybackJson.get("media");

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

  private Mono<Map<String, String>> parseHlsUrl(String json) {
    Map<String, String> streamingInfoMap = new HashMap<>();
    try {
      Map<String, Object> responseMap = objectMapper.readValue(json, new TypeReference<>() {});
      if (responseMap.containsKey("content") && responseMap.get("content") instanceof Map) {
        Map<String, Object> contentMap = (Map<String, Object>) responseMap.get("content");

        streamingInfoMap.put("liveTitle", contentMap.get("liveTitle").toString());

        if (contentMap.containsKey("livePlaybackJson") && contentMap.get("livePlaybackJson") instanceof String) {
          Map<String, Object> livePlaybackJsonMap = objectMapper.readValue(contentMap.get("livePlaybackJson").toString(), new TypeReference<>() {});
          contentMap.put("livePlaybackJson", livePlaybackJsonMap);

          // 🔥 HLS media에서 path 값만 추출
          List<String> hlsPaths = extractHLSPaths(livePlaybackJsonMap);
          log.info("Extracted HLS Paths: {}", hlsPaths);
          streamingInfoMap.put("hlsPath", hlsPaths.get(0));
        }
      }
    } catch (Exception e) {
      return Mono.error(e);
    }

    return Mono.just(streamingInfoMap);
  }
}
