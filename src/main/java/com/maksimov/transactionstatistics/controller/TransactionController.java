package com.maksimov.transactionstatistics.controller;

import com.maksimov.transactionstatistics.exception.NoContentException;
import com.maksimov.transactionstatistics.model.Transaction;
import com.maksimov.transactionstatistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("/api/transactions")
public class TransactionController {
    private StatisticsService statisticsService;

    @Autowired
    public TransactionController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(method = RequestMethod.POST,
            consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity saveTransaction(Transaction transaction) {
        // TODO: wrap all exceptions
        try {
            return new ResponseEntity(statisticsService.saveTransaction(transaction));
        } catch (NoContentException e) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

//    @RequestMapping(method = RequestMethod.PUT,
//            consumes = APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity save2Transaction(Transaction transaction) {
//        return new ResponseEntity(statisticsService.save2Transaction(transaction));
//    }
//
//    @RequestMapping(method = RequestMethod.OPTIONS,
//            consumes = APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity save3Transaction(Transaction transaction) {
//        return new ResponseEntity(statisticsService.save3Transaction(transaction));
//    }
}
