package com.maksimov.transactionstatistics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private double amount = 0L;
    private long  timestamp = 0L;
}
