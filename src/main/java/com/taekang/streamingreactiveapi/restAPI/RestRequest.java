package com.taekang.streamingreactiveapi.restAPI;

import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public interface RestRequest {

    Mono<String> get(String url, HttpHeaders headers);

    Mono<String> delete(String url, HttpHeaders headers);
}
