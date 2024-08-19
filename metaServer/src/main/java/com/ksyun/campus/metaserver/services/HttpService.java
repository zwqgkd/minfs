package com.ksyun.campus.metaserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class HttpService {

    protected static ObjectMapper mapper = new ObjectMapper();

    public ResponseEntity sendPostRequest(String url, String interfaceName, String fileSystemName, Object param) {
        try {
            String[] parts = url.split(":");

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            uriBuilder.scheme("http") // 设置协议为http
                    .host(host)
                    .port(port)
                    .pathSegment(interfaceName);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("fileSystemName", fileSystemName);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = mapper.writeValueAsString(param);
            System.out.println(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);


            URI uri = uriBuilder.build().toUri();
            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
            return responseEntity;
        } catch (HttpServerErrorException e) {
            HttpStatus status = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            return ResponseEntity.status(status).body("An error occurred: " + responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
