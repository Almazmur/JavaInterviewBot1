package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

public class Utils {
    public static String getUrl(String nasaUrl) {
        CloseableHttpClient client = HttpClients.createDefault();
        ObjectMapper mapper = new ObjectMapper();
        HttpGet requestImage = new HttpGet(nasaUrl);
        try {
            CloseableHttpResponse response = client.execute(requestImage);
            NasaAnswer answer = mapper.readValue(response.getEntity().getContent(), NasaAnswer.class);
            return answer.url;
        } catch (IOException e) {
            System.out.println("Произошла ошибка");
        }
        return "";
    }
}