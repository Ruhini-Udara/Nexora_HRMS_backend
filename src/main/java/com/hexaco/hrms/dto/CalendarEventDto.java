package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEventDto {
    private String id;
    private String title;
    private String date; // Format: YYYY-MM-DD
    private String endDate; // Format: YYYY-MM-DD (inclusive end date)
    private String time; // Format: hh:mm a (optional)
}
