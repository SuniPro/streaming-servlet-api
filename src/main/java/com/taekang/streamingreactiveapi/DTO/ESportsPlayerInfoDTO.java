package com.taekang.streamingreactiveapi.DTO;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ESportsPlayerInfoDTO {

    private String PlayerName;

    private int matchLength;

    private int firstKillLength;

    private BigDecimal firstKillRatio;

    private int winLength;

    private BigDecimal winRatio;

    private int killLength;
}
