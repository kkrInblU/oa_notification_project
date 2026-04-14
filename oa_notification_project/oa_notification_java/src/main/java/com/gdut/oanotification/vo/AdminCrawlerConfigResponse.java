package com.gdut.oanotification.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminCrawlerConfigResponse {

    private Boolean schedulerEnabled;
    private Double schedulerIntervalMinutes;
    private Integer schedulerMaxRuns;
    private Integer maxRecords;
    private Double requestDelayMin;
    private Double requestDelayMax;
    private List<String> updatedKeys;
}
