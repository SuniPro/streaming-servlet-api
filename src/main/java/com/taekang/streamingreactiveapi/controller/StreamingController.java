package com.taekang.streamingreactiveapi.controller;

import com.taekang.streamingreactiveapi.service.StreamingSiteFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

//https://api.chzzk.naver.com/service/v2/channels/06f9aa72cc7cec538394aab5017199e4/live-detail
@Slf4j
@RestController // ✅ Bean 이름 설정 제거
@RequestMapping("streaming/fetch")
public class StreamingController {

    private final StreamingSiteFetcherService streamingSiteFetcherService;

    @Autowired
    public StreamingController(StreamingSiteFetcherService streamingSiteFetcherService) {
        this.streamingSiteFetcherService = streamingSiteFetcherService;
    }

    @PostMapping("stream/hls")
    public Mono<List<Map<String, String>>> fetchGameData(@RequestBody List<String> urls) {

        return streamingSiteFetcherService.getStreamingUrl(urls);
    }
}
