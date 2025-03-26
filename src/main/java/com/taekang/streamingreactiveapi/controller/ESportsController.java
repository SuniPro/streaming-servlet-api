package com.taekang.streamingreactiveapi.controller;

import com.taekang.streamingreactiveapi.DTO.ESportsPlayerInfoDTO;
import com.taekang.streamingreactiveapi.service.ESportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("esports")
public class ESportsController {

    private final ESportsService esportsService;

    @Autowired
    public ESportsController(ESportsService esportsService) {
        this.esportsService = esportsService;
    }

    @GetMapping("lol/get/{playerName}/{tag}")
    public Mono<ESportsPlayerInfoDTO> getPlayerInfo(@PathVariable String playerName, @PathVariable String tag) {
        return esportsService.getPlayerInfoRecentTwenty(playerName, tag);
    }
}
