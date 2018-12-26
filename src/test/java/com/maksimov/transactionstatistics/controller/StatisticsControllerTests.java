package com.maksimov.transactionstatistics.controller;

import io.restassured.http.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class StatisticsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getStatisticsOnce() {
        given()
                .mockMvc(mockMvc)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/statistics")
                .then()
                .statusCode(HTTP_OK)
                .body("sum", equalTo(0))
                .body("count", equalTo(0))
                .body("min", equalTo(0))
                .body("max", equalTo(0))
                .body("avg", equalTo(0));
    }
}

