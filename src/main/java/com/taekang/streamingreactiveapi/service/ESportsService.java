package com.taekang.streamingreactiveapi.service;

import com.taekang.streamingreactiveapi.DTO.ESportsPlayerInfoDTO;
import reactor.core.publisher.Mono;

public interface ESportsService {

  public Mono<ESportsPlayerInfoDTO> getPlayerInfoRecentTwenty(String playerName, String tag);
}
