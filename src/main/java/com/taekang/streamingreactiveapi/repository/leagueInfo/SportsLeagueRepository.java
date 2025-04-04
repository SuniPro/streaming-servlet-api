package com.taekang.streamingreactiveapi.repository.leagueInfo;

import com.taekang.streamingreactiveapi.entity.SportsLeague;
import java.time.LocalDateTime;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SportsLeagueRepository extends R2dbcRepository<SportsLeague, Long> {
  Flux<SportsLeague> findAllByImportant(boolean important);

  Flux<SportsLeague> findByLeagueNameAndLeagueDate(String leagueName, LocalDateTime leagueDate);
}
