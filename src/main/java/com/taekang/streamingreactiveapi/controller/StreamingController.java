package com.taekang.streamingreactiveapi.controller;

import com.taekang.streamingreactiveapi.service.StreamingSiteFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController // ✅ Bean 이름 설정 제거
@RequestMapping("streaming/fetch")
public class StreamingController {

    private final StreamingSiteFetcherService streamingSiteFetcherService;

    @Autowired
    public StreamingController(StreamingSiteFetcherService streamingSiteFetcherService) {
        this.streamingSiteFetcherService = streamingSiteFetcherService;
    }

}
