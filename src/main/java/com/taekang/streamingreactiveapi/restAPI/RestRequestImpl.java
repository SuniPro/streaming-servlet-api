package com.taekang.streamingreactiveapi.restAPI;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class RestRequestImpl implements RestRequest {

    private final WebClient webClient;

    @Autowired
    public RestRequestImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<String> post(String url, HttpHeaders headers, String body) {
        return webClient.post()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(new HttpHeaders(headers))) // 기존 헤더 추가
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new RuntimeException("클라이언트 오류 발생: " + response.statusCode()))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("서버 오류 발생: " + response.statusCode()))
                )
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> get(String url, HttpHeaders headers) {

        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(new HttpHeaders(headers))) // 기존 헤더 추가
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new RuntimeException("클라이언트 오류 발생: " + response.statusCode()))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("서버 오류 발생: " + response.statusCode()))
                )
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> delete(String url, HttpHeaders headers) {
        return null;
    }

}
