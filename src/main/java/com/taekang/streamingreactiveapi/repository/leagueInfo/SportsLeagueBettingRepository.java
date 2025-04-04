package com.taekang.streamingreactiveapi.repository.leagueInfo;

import com.taekang.streamingreactiveapi.entity.SportsLeagueBetting;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SportsLeagueBettingRepository extends R2dbcRepository<SportsLeagueBetting, Long> {
  Flux<SportsLeagueBetting> findBySportsLeagueId(Long sportsLeagueId);
}
