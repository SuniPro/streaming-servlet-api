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
        String encodedBase = Base64.getUrlEncoder().encodeToString(baseCdnUrl.getBytes(StandardCharsets.UTF_8));

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(originalM3u8 -> {

                    String rewritten = Arrays.stream(originalM3u8.split("\n"))
                            .map(String::trim)
                            .map(line -> {
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

    // 🎯 실제 .ts 파일 프록시 처리
    @GetMapping("ts/{encodedBase}/**")
    public Mono<ResponseEntity<Flux<DataBuffer>>> proxyTsFile(
            @PathVariable String encodedBase,
            ServerHttpRequest request
    ) {
        // 전체 경로
        String fullPath = request.getURI().getPath();
        String basePrefix = "/broadcast/ts/" + encodedBase + "/";

        // .ts 파일 경로 추출
        String tsPath = fullPath.substring(fullPath.indexOf(basePrefix) + basePrefix.length());

        // base64 디코딩
        String baseCdnUrl = new String(Base64.getUrlDecoder().decode(encodedBase), StandardCharsets.UTF_8);

        String originUrl = baseCdnUrl + tsPath;
        log.info("🎯 [proxy] originUrl: {}", originUrl);

        return Mono.just(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "video/MP2T")
                        .body(webClient.get().uri(originUrl).retrieve().bodyToFlux(DataBuffer.class))
        );
    }
}