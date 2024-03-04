package com.gridnine.testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class CrptApi {
    private String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private Semaphore limit;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.limit = new Semaphore(requestLimit);
        if (requestLimit >= 0) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(
                    this.limit::release,
                    0, timeUnit.toMillis(1),
                    TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalArgumentException("Значение должно быть больше 0");
        }
    }


    public void createDocument(Document document, String signature) throws InterruptedException {
        limit.acquire();
        //преобразование json
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //преобразование в json строку
            String jsonDocument = objectMapper.writeValueAsString(document);
            //передаём запрос
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity stringEntity = new StringEntity(jsonDocument);
            httpPost.setEntity(stringEntity);
            //создаем объект для передачи http запроса
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();
            System.out.println(entity);

            httpClient.close();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        CrptApi cprt = new CrptApi(TimeUnit.SECONDS, 4);
        cprt.createDocument(new Document(), "signature");

    }

    @Data
    @NoArgsConstructor
    private static class Document {
        private Description description;
        private String doc_id;
        private String dog_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;

    }

    @Data
    @NoArgsConstructor
    private static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

    }
    @Data
    @NoArgsConstructor
    private static class Description {
        private String participantInn;
    }
}
