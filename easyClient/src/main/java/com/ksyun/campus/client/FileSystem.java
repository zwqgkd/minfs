package com.ksyun.campus.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.campus.client.util.ZkUtil;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 基类，定义了通用的文件系统方法和变量
 * 整体的文件组织结构为以下形式
 * {namespace}/{dir}
 *                  /{subdir}
 *                  /{subdir}/file
 *                  /file
 */
public abstract class FileSystem {

    //文件系统名称，可理解成命名空间，可以存在多个命名空间，多个命名空间下的文件目录结构是独立的
    protected String fileSystemName;

    protected static ObjectMapper mapper = new ObjectMapper();

    protected static ZkUtil zkUtil = new ZkUtil();

//    protected String acquireIpAddress(String param) {
//        String res = "";
//        switch (param) {
//            case "dataServer":
//                res = zkUtil.getDataServerUrl();
//                break;
//            case "metaServer":
//                res = zkUtil.getMasterMetaDataServerUrl();
//                break;
//        }
//        return res;
//    }

    protected <T> ResponseEntity<T> sendGetRequest(String url, String interfaceName, String path, Class<T> responseType) {
        try {
            String[] parts = url.split(":");

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            uriBuilder.scheme("http") // 设置协议为http
                    .host(host)
                    .port(port)
                    .pathSegment(interfaceName)
                    .queryParam("path", path);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("fileSystemName", fileSystemName);

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

            URI uri = uriBuilder.build().toUri();
            return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType);
        } catch (HttpServerErrorException e) {
            HttpStatus status = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            return ResponseEntity.status(status).body(null);
        }
    }

    protected <T> ResponseEntity<T> sendGetRequest(String url, String interfaceName, String path, ParameterizedTypeReference<T> responseType) {
        try {
            String[] parts = url.split(":");

            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();
            uriBuilder.scheme("http") // 设置协议为http
                    .host(host)
                    .port(port)
                    .pathSegment(interfaceName)
                    .queryParam("path", path);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("fileSystemName", fileSystemName);

            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

            URI uri = uriBuilder.build().toUri();
            return restTemplate.exchange(uri, HttpMethod.GET, entity, responseType);
        } catch (HttpServerErrorException e) {
            HttpStatus status = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            return ResponseEntity.status(status).body(null);
        }
    }

    protected ResponseEntity sendPostRequest(String url, String interfaceName, Object param) {

        try {
            // 使用UriComponentsBuilder构建URL
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

    //远程调用
    protected void callRemote(){
//        httpClient.execute();
    }

}
