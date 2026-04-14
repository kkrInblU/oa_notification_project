package com.gdut.oanotification.dto.request;

import lombok.Data;

@Data
public class UpdateCrawlerConfigRequest {

    private Boolean schedulerEnabled;
    private Double schedulerIntervalMinutes;
    private Integer schedulerMaxRuns;
    private Integer maxRecords;
    private Double requestDelayMin;
    private Double requestDelayMax;
}
