package com.maksimov.transactionstatistics.controller;

import com.maksimov.transactionstatistics.model.Statistics;
import io.restassured.http.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TransactionControllerTests {
    private long LESS_MINUTE = 55 * 1000;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void sendPastStatistics() {
    }

    @Test
    public void sendPresentStatistics() {
        long start = System.currentTimeMillis();
        long current = System.currentTimeMillis();

        double amount = 0;

        BigDecimal sum = BigDecimal.valueOf(0);
        BigDecimal min = BigDecimal.valueOf(1000);
        BigDecimal max = BigDecimal.valueOf(-1);
        BigDecimal count = BigDecimal.valueOf(0);
        BigDecimal avg = BigDecimal.valueOf(0);

        while (current < start + LESS_MINUTE) {
            amount = Math.floor(Math.random() * 10 + 1);

            sendTransaction(amount, current);

            BigDecimal amountBigDecimal = BigDecimal.valueOf(amount);
            sum = sum.add(amountBigDecimal);
            min = min.min(amountBigDecimal);
            max = max.max(amountBigDecimal);
            count = count.add(BigDecimal.ONE);
            avg = sum.divide(count, 2, RoundingMode.HALF_UP);

            Statistics statistics2 = given()
                    .mockMvc(mockMvc)
                    .contentType(ContentType.JSON)
                    .get("/api/statistics")
                    .then()
                    .extract().as(Statistics.class);

            assertEquals(statistics2.getSum(), sum);
            assertEquals(statistics2.getCount(), count);
            assertEquals(statistics2.getMin(), min);
            assertEquals(statistics2.getMax(), max);
            assertEquals(statistics2.getAvg(), avg);

            current = System.currentTimeMillis();
        }
    }

    @Test
    public void sendFutureStatistics() {
    }

    @Test
    public void sendRandomStatistics() {
    }


    private void sendTransaction(double amount, long timestamp) {
        given()
                .mockMvc(mockMvc)
                .contentType(ContentType.JSON)
                .param("amount", amount)
                .param("timestamp", timestamp)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(HTTP_CREATED);
    }
}

