package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarryForwardBatchDetailDto {
    private String id;
    private Integer year;
    private String status;
    private List<LocationGroupDto> locations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationGroupDto {
        private String locationName;
        private List<CarryForwardEntryDto> entries;
    }
}
