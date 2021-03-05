package com.maksimov.transactionstatistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maksimov.transactionstatistics.dto.StatisticsDTO;
import com.maksimov.transactionstatistics.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG)
@Slf4j
public class TransactionControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void sendPastStatistics() throws Exception {
        StatisticsDTO prevStatistics = getServerStatistics();
        long secondAgo = Instant.now().minusSeconds(60).toEpochMilli();
        double amount = 10;
        sendTransaction(amount, secondAgo)
                .andExpect(status().isNoContent());
        StatisticsDTO statistics = getServerStatistics();
        assertEquals(prevStatistics.toString(), statistics.toString());
    }

    @Test
    public void sendPresentStatistics() throws Exception {
        double[] amounts = new double[]{1, 0, 3, 4};
        List<StatisticsDTO> expectedStatistics = new ArrayList<>();
        expectedStatistics.add(0, new StatisticsDTO(1, 1, 1, 1, 1));
        expectedStatistics.add(1, new StatisticsDTO(1, 0.5, 1, 0, 2));
        expectedStatistics.add(2, new StatisticsDTO(4, 1.3333333333333333, 3, 0, 3));
        expectedStatistics.add(3, new StatisticsDTO(8, 2, 4, 0, 4));

        for (int i = 0; i < amounts.length; i++) {
            log.info(String.format("Iteration number %d", i));
            double amount = amounts[i];
            StatisticsDTO expected = expectedStatistics.get(i);
            sendTransaction(amount, Instant.now().toEpochMilli())
                    .andExpect(status().isCreated());
            StatisticsDTO statisticsDTO = getServerStatistics();
            assertEquals(expected.toString(), statisticsDTO.toString(), String.format("Iteration %d, were sent %s", i, Arrays.spliterator(amounts, 0, i + 1)));
        }
    }

    @Test
    public void sendFutureStatistics() throws Exception {
        StatisticsDTO prevStatistics = getServerStatistics();
        long secondAgo = Instant.now().plusSeconds(1).toEpochMilli();
        double amount = 10;
        sendTransaction(amount, secondAgo)
                .andExpect(status().isNoContent());
        StatisticsDTO statistics = getServerStatistics();
        assertEquals(prevStatistics.toString(), statistics.toString());
    }

    private ResultActions sendTransaction(double amount, long timestamp) throws Exception {
        TransactionDTO transaction = new TransactionDTO(amount, timestamp);
        String value = mapper.writeValueAsString(transaction);
        return mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content(value))
                .andDo(print());
    }

    private StatisticsDTO getServerStatistics() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn()
                .getResponse();

        return mapper.readValue(response.getContentAsString(), StatisticsDTO.class);
    }
}

