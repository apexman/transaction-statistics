package com.maksimov.transactionstatistics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsDTO {
    private double sum;
    private double avg;
    private double max;
    private double min;
    private long count;
}
