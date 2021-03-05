package com.maksimov.transactionstatistics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {
    private double sum = 0L;
    private double avg = 0L;
    private double max = 0L;
    private double min = 0L;
    private long count = 0L;
    private boolean isNew = true;
}
