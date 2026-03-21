package com.bcbbs.backend.service;

import com.bcbbs.backend.dto.LotteryGameOption;
import com.bcbbs.backend.dto.LotteryListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class LotteryServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private LotteryService lotteryService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        lotteryService = new LotteryService(restTemplate);
    }

    @Test
    void getHistoryListPassesDateAndMapsResponse() {
        String body = """
                {
                  "code": 0,
                  "data": {
                    "list": [
                      {
                        "preDrawIssue": "3410441",
                        "preDrawCode": "8,0,5,7,9",
                        "preDrawTime": "2026-03-20 23:59:00",
                        "attr1": "29",
                        "attr2": "大",
                        "attr3": "单"
                      }
                    ],
                    "total": 701
                  }
                }
                """;

        server.expect(requestTo(containsString("https://bw1284.cc/api/lottery_code/getLotteryList")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("lotCode", "720"))
                .andExpect(queryParam("pageNo", "1"))
                .andExpect(queryParam("pageSize", "20"))
                .andExpect(queryParam("date", "2026-03-20"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        LotteryListResponse result = lotteryService.getHistoryList(720, 1, 20, "2026-03-20");

        assertThat(result.getTotal()).isEqualTo(701);
        assertThat(result.getList()).hasSize(1);
        assertThat(result.getList().get(0).getPreDrawIssue()).isEqualTo("3410441");
        assertThat(result.getList().get(0).getPreDrawCode()).isEqualTo("8,0,5,7,9");
        assertThat(result.getList().get(0).getSumValue()).isEqualTo("29");
        assertThat(result.getList().get(0).getSizeLabel()).isEqualTo("大");
        assertThat(result.getList().get(0).getParityLabel()).isEqualTo("单");
        server.verify();
    }

    @Test
    void getAvailableGamesMapsCatalog() {
        String body = """
                {
                  "code": 0,
                  "data": [
                    {
                      "lotCode": 720,
                      "lotName": "加拿大pc28",
                      "lotType": 4,
                      "lotLabel": 1,
                      "sort": 1
                    },
                    {
                      "lotCode": 797,
                      "lotName": "澳洲幸运10",
                      "lotType": 2,
                      "lotLabel": 0,
                      "sort": 3
                    }
                  ]
                }
                """;

        server.expect(requestTo(containsString("https://bw1284.cc/api/lottery_code/allLottery")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        List<LotteryGameOption> result = lotteryService.getAvailableGames();

        assertThat(result)
                .extracting(LotteryGameOption::getLotCode, LotteryGameOption::getLotName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(720, "加拿大pc28"),
                        org.assertj.core.groups.Tuple.tuple(797, "澳洲幸运10")
                );
        server.verify();
    }
}
