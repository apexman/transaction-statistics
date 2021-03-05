package com.maksimov.transactionstatistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maksimov.transactionstatistics.model.Statistics;
import com.maksimov.transactionstatistics.model.Transaction;
import com.maksimov.transactionstatistics.service.TransactionStatisticsService;
import com.maksimov.transactionstatistics.dto.StatisticsDTO;
import com.maksimov.transactionstatistics.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class AppController {

    @Autowired
    private TransactionStatisticsService transactionStatisticsService;
    @Autowired
    private ObjectMapper mapper;

    @PostMapping(path = "/api/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveTransaction(@RequestBody TransactionDTO transactionDTO) {
        Transaction transaction = mapper.convertValue(transactionDTO, Transaction.class);
        if (!transactionStatisticsService.canSave(transaction)) {
            return ResponseEntity.noContent().build();
        }
        transactionStatisticsService.saveTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(path = "/api/statistics")
    public ResponseEntity<StatisticsDTO> getStatistics() {
        Statistics statistics = transactionStatisticsService.getStatistics();
        StatisticsDTO statisticsDTO = mapper.convertValue(statistics, StatisticsDTO.class);
        return ResponseEntity.ok(statisticsDTO);
    }

}
