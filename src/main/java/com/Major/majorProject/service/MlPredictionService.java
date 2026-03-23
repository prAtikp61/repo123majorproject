package com.Major.majorProject.service;

import com.Major.majorProject.dto.MlPredictionResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MlPredictionService {

    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:5000/predict}")
    private String mlServiceUrl;

    public MlPredictionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int predictDemand(int timeSlot, int dayOfWeek) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("time_slot", timeSlot);
            payload.put("day", dayOfWeek);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            MlPredictionResponseDto response = restTemplate.postForObject(
                    mlServiceUrl,
                    new HttpEntity<>(payload, headers),
                    MlPredictionResponseDto.class
            );

            return response != null ? response.getPredicted() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
