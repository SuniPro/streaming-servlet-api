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

  /** m3u8의 내부 TS 조각들에 proxy 주소를 rewrite 하여 CORS 문제를 회피하게 합니다. */
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
        .elapsed()
        .map(
            tuple -> {
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

              return ResponseEntity.ok()
                  .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
                  .body(rewritten);
            });
  }

  /** 프록시 된 TS 조각 들을 받아 하나의 TS 조각이 아닌, 받은 모든 TS 조각을 hls js가 재생시키게 합니다. */
  @GetMapping("ts/{encodedBase}/**")
  public Mono<ResponseEntity<Flux<DataBuffer>>> proxyTsFile(
      @PathVariable String encodedBase, ServerHttpRequest request) {
    String fullPath = request.getURI().getPath();
    String basePrefix = "/broadcast/ts/" + encodedBase + "/";
    String tsPath = fullPath.substring(fullPath.indexOf(basePrefix) + basePrefix.length());

    String baseCdnUrl =
        new String(Base64.getUrlDecoder().decode(encodedBase), StandardCharsets.UTF_8);
    String originUrl = baseCdnUrl + tsPath;

    Flux<DataBuffer> tsBody =
        webClient.get().uri(originUrl).retrieve().bodyToFlux(DataBuffer.class);

    return Mono.just(
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "video/MP2T")
            .body(tsBody) // ❗ collectList 안 씀!!
        );
  }
}
