package ru.practicum.statclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

@Component
public class StatClientImpl implements StatClient {
    private static final String PREFIX_HIT = "/hit";
    private static final String PREFIX_STATS = "/stats";

    protected final RestTemplate rest;

    @Autowired
    public StatClientImpl(@Value("${stat-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    private static ResponseEntity<Object> prepareClientResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());
        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }

    @Override
    public ResponseEntity<Object> get() {
        return get(null);
    }

    @Override
    public ResponseEntity<Object> get(@Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, PREFIX_STATS, parameters, null);
    }

    @Override
    public <T> ResponseEntity<Object> post(T body) {
        return makeAndSendRequest(HttpMethod.POST, PREFIX_HIT, null, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body);
        ResponseEntity<Object> statServerResponse;
        try {
            if (parameters != null) {
                StringBuilder stringParametrs = new StringBuilder(path);
                stringParametrs.append("?");
                for (String key : parameters.keySet()) {
                    stringParametrs.append(key + "={" + key + "}&");
                }
                statServerResponse = rest.exchange(stringParametrs.toString(), method, requestEntity, Object.class, parameters);
            } else {
                statServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareClientResponse(statServerResponse);
    }
}
