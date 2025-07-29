package com.example.bajaj;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class StartRun implements CommandLineRunner {

    private final WebClient.Builder webClientBuilder;

    public StartRun(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public void run(String... args) {

        WebClient client = webClientBuilder.baseUrl("https://bfhldevapigw.healthrx.co.in").build();

        Map<String, String> data = Map.of(
                "name", "Lakshay",
                "regNo", "2210991838",
                "email", "lakshay1838.be22@chitkara.edu.in"
        );


        WebhookRes res = client.post()
                .uri("/hiring/generateWebhook/JAVA")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(WebhookRes.class)
                .block();

        if (res != null) {
            // Print webhook URL and token
            System.out.println("Webhook: " + res.getWebhook());
            System.out.println("Token: " + res.getAccessToken());

            String sql = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                    "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                    "FROM EMPLOYEE e1 " +
                    "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                    "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                    "AND e2.DOB > e1.DOB " +
                    "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                    "ORDER BY e1.EMP_ID DESC;";

            Map<String, String> answer = Map.of("finalQuery", sql);

            client.post()
                    .uri(res.getWebhook())
                    .header("Authorization", res.getAccessToken())
                    .header("Content-Type", "application/json")
                    .bodyValue(answer)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(System.out::println)
                    .block();
        } else {
            System.out.println("null value : error");
        }
    }
}
