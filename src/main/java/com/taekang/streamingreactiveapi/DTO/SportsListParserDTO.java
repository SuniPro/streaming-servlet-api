package com.taekang.streamingreactiveapi.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SportsListParserDTO {
  private String ch;
  private String ch2;
  private String ca;
  private String league;
  private String title;
  private String bold;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime time; // ← 여기에 포맷 명시

  private String onoff;
}
