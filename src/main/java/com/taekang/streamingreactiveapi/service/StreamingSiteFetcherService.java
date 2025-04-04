package com.taekang.streamingreactiveapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taekang.streamingreactiveapi.restAPI.RestRequest;
import com.taekang.streamingreactiveapi.tool.Tools;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class StreamingSiteFetcherService {

  private final RestRequest restRequest;
  private final ObjectMapper objectMapper;
  private final WebClient webClient;

  @Autowired
  public StreamingSiteFetcherService(
      RestRequest restRequest, ObjectMapper objectMapper, WebClient.Builder webClientBuilder) {
    this.restRequest = restRequest;
    this.objectMapper = objectMapper;
    this.webClient = webClientBuilder.build();
  }

  public Mono<String> getSoopStreamingUrl(String inputUrl) throws URISyntaxException {
    String bjName = Tools.getPathSegments(inputUrl, 1);
    String bjNumber = Tools.getPathSegments(inputUrl, 2);

    return fetchAid(bjName, bjNumber)
        .zipWith(fetchCdnInfo(bjName, bjNumber)) // aid + cdn/bno 같이 요청
        .flatMap(
            tuple -> {
              String aid = tuple.getT1();
              CdnMeta cdnMeta = tuple.getT2();
              return fetchViewUrl(cdnMeta)
                  .flatMap(viewBaseUrl -> fetch1080pUrlFromAid(aid, viewBaseUrl));
            });
  }

  private Mono<CdnMeta> fetchCdnInfo(String bjName, String bjNumber) {

    String postUrl = "https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=" + bjName;

    return webClient
        .post()
        .uri(postUrl)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(buildFormData(bjName, bjNumber, "live")))
        .retrieve()
        .bodyToMono(String.class)
        .map(
            body -> {
              try {
                JsonNode root = objectMapper.readTree(body);
                String cdn = root.path("CHANNEL").path("CDN").asText();
                String bno = root.path("CHANNEL").path("BNO").asText();
                return new CdnMeta(cdn, bno);
              } catch (Exception e) {
                throw new RuntimeException("CDN 파싱 실패", e);
              }
            });
  }

  private Mono<String> fetchAid(String bjName, String bjNumber) {
    String postUrl = "https://live.sooplive.co.kr/afreeca/player_live_api.php?bjid=" + bjName;

    return webClient
        .post()
        .uri(postUrl)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(buildFormData(bjName, bjNumber, "aid")))
        .retrieve()
        .bodyToMono(String.class)
        .map(
            body -> {
              try {
                return objectMapper.readTree(body).path("CHANNEL").path("AID").asText();
              } catch (Exception e) {
                throw new RuntimeException("AID 파싱 실패", e);
              }
            });
  }

  private MultiValueMap<String, String> buildFormData(String bjName, String bjNumber, String type) {
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("bid", bjName);
    formData.add("bno", bjNumber);
    formData.add("type", type); // ✅ only 차이점
    formData.add("player_type", "html5");
    formData.add("stream_type", "common");
    formData.add("quality", "master");
    formData.add("mode", "landing");
    formData.add("form_api", "0");
    formData.add("is_revive", "false");
    return formData;
  }

  record CdnMeta(String cdnType, String bjNumber) {
    String getBroadKey() {
      return bjNumber + "-common-master-hls";
    }
  }

  private Mono<String> fetchViewUrl(CdnMeta cdnMeta) {
    String url =
        UriComponentsBuilder.fromHttpUrl(
                "https://livestream-manager.sooplive.co.kr/broad_stream_assign.html")
            .queryParam(
                "return_type",
                Objects.equals(cdnMeta.cdnType(), "") ? "gcp_cdn" : cdnMeta.cdnType())
            .queryParam("use_cors", true)
            .queryParam("cors_origin_url", "play.sooplive.co.kr")
            .queryParam("broad_key", cdnMeta.getBroadKey())
            .queryParam("player_mode", "landing")
            .toUriString();

    return webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .map(
            body -> {
              try {
                JsonNode node = objectMapper.readTree(body);
                String viewUrl = node.path("view_url").asText(); // 전체 URL
                return viewUrl.substring(0, viewUrl.lastIndexOf("/") + 1); // base URL만
              } catch (Exception e) {
                throw new RuntimeException("view_url 파싱 실패", e);
              }
            });
  }

  private Mono<String> fetch1080pUrlFromAid(String aid, String baseUrl) {
    String masterUrl =
        UriComponentsBuilder.fromHttpUrl(baseUrl + "auth_master_playlist.m3u8")
            .queryParam("aid", aid)
            .toUriString();
    return webClient
        .get()
        .uri(masterUrl)
        .retrieve()
        .bodyToMono(String.class)
        .doOnNext(body -> log.info("🎯 master.m3u8 응답:\n{}", body))
        .flatMapMany(body -> Flux.fromArray(body.split("\n")))
        .map(String::trim)
        .index()
        .buffer(2, 1)
        .filter(
            pair -> {
              if (pair.size() < 2) return false;
              String currentLine = pair.get(0).getT2();
              String nextLine = pair.get(1).getT2();
              return currentLine.contains("RESOLUTION=1920x1080")
                  && nextLine.startsWith("auth_playlist.m3u8?aid=");
            })
        .next()
        .map(pair -> pair.get(1).getT2().trim())
        .map(
            suffix -> {
              // 🔥 baseUrl에서 마지막 부분만 제거해서 suffix 붙이기
              String finalUrl = baseUrl + suffix;
              log.info("🎬 최종 m3u8 URL: {}", finalUrl);
              return finalUrl;
            });
  }

  public Mono<String> getChzzkStreamingUrl(String url) {
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

  public Mono<Object> processJsonResponse(String json) {
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

  public Mono<Map<String, Object>> parseJsonToMap(String json) {
    try {
      Map<String, Object> responseMap = objectMapper.readValue(json, new TypeReference<>() {});
      return Mono.just(responseMap);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("JSON Map 변환 오류", e));
    }
  }

  public Mono<List<String>> parseJsonToList(String json) {
    try {
      List<String> list = objectMapper.readValue(json, new TypeReference<>() {});
      return Mono.just(list);
    } catch (Exception e) {
      return Mono.error(new RuntimeException("JSON List 변환 오류", e));
    }
  }
}
