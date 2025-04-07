package com.taekang.streamingreactiveapi.controller;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("broadcast")
public class BroadCastController {

  private final WebClient webClient;

  @Autowired
  public BroadCastController(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  @GetMapping("soop")
  public Mono<ResponseEntity<String>> getSoopStreamingUrl(@RequestParam String url) {
    String baseCdnUrl = url.substring(0, url.lastIndexOf("/") + 1);
    String encodedBase =
        Base64.getUrlEncoder().encodeToString(baseCdnUrl.getBytes(StandardCharsets.UTF_8));

    return webClient
        .get()
        .uri(url)
        .retrieve()
        .bodyToMono(String.class)
        .doOnSubscribe(sub -> log.info("🚀 [M3U8] 요청 준비 시작: {}", url))
        .doOnRequest(request -> log.info("📡 [M3U8] WebClient 요청 시작: {}", url))
        .doOnSuccess(resp -> log.info("✅ [M3U8] 응답 수신 완료"))
        .doOnError(error -> log.error("❌ [M3U8] 요청 실패: {} | {}", url, error.getMessage(), error))
        .elapsed()
        .map(
            tuple -> {
              long duration = tuple.getT1(); // 요청 → 응답까지 걸린 시간 (ms)
              String result = tuple.getT2();

              // .ts 세그먼트 경로 프록시 처리
              String rewritten =
                  Arrays.stream(result.split("\n"))
                      .map(String::trim)
                      .map(
                          line -> {
                            if (line.toLowerCase().endsWith(".ts")) {
                              return "/streaming/broadcast/ts/" + encodedBase + "/" + line;
                            }
                            return line;
                          })
                      .collect(Collectors.joining("\n"));

              long tsCount =
                  Arrays.stream(result.split("\n"))
                      .map(String::trim)
                      .filter(line -> line.toLowerCase().endsWith(".ts"))
                      .count();

              log.info("📦 [M3U8] 포함된 TS 세그먼트 수: {}", tsCount);
              log.info("⏱️ [M3U8] {} 응답 시간: {}ms", url, duration);

              return ResponseEntity.ok()
                  .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                  .body(rewritten);
            });
  }

  // 🎯 실제 .ts 파일 프록시 처리
  @GetMapping("ts/{encodedBase}/**")
  public Mono<ResponseEntity<Flux<DataBuffer>>> proxyTsFile(
      @PathVariable String encodedBase, ServerHttpRequest request) {
    String fullPath = request.getURI().getPath();
    String basePrefix = "/broadcast/ts/" + encodedBase + "/";
    String tsPath = fullPath.substring(fullPath.indexOf(basePrefix) + basePrefix.length());

    String baseCdnUrl =
        new String(Base64.getUrlDecoder().decode(encodedBase), StandardCharsets.UTF_8);
    String originUrl = baseCdnUrl + tsPath;

    log.info("📥 TS 프록시 요청 수신: {}", request.getURI());
    log.info("🎯 [proxy] TS 요청 시작: {}", originUrl);

    return webClient
        .get()
        .uri(originUrl)
        .retrieve()
        .bodyToFlux(DataBuffer.class)
        .elapsed()
        .map(
            tuple -> {
              long duration = tuple.getT1();
              DataBuffer data = tuple.getT2();
              log.info("⏱️ [proxy] TS 응답 시간: {}ms | {}", duration, tsPath);
              return data;
            })
        .collectList()
        .map(
            list ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/MP2T")
                    .body(Flux.fromIterable(list)));
  }
}
