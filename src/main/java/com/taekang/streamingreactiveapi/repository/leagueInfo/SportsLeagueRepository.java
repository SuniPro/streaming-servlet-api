package com.taekang.streamingreactiveapi.repository.leagueInfo;

import com.taekang.streamingreactiveapi.entity.SportsLeague;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportsLeagueRepository extends R2dbcRepository<SportsLeague, Long> {}
