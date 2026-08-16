package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftDto {
    private Long id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
}
