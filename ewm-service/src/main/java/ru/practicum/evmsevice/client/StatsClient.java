package ru.practicum.evmsevice.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statclient.BaseClient;
import ru.practicum.statdto.HitDto;
import ru.practicum.statdto.StatsDto;
import ru.practicum.statdto.StatsDtoList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StatsClient extends BaseClient {
    private static final String PREFIX_HIT = "/hit";
    private static final String PREFIX_STATS = "/stats";
    private static final String PREFIX_EVETS = "/events/";

    @Autowired
    public StatsClient(@Value("${statserver.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public void post(HitDto dto) {
        makeAndSendRequest(HttpMethod.POST, PREFIX_HIT, null, dto);
    }

    public ResponseEntity<Object> get(Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, PREFIX_STATS, parameters, null);
    }

    public void hitInfo(String appName, HttpServletRequest request) {
        HitDto hitDto = new HitDto();
        hitDto.setApp(appName);
        hitDto.setUri(request.getRequestURI());
        hitDto.setIp(request.getRemoteAddr());
        hitDto.setTimestamp(LocalDateTime.now());
        post(hitDto);
    }

    public Integer getEventViews(Integer eventId, Boolean unique) {
        Map<String, Object> parameters = Map.of("uris", PREFIX_EVETS + eventId,
                "unique", unique);
        List<StatsDto> dtos = getStatsList(PREFIX_STATS, parameters);
        if (dtos.isEmpty()) {
            return 0;
        }
        return dtos.get(0).getHits();
    }

    public List<StatsDto> getEventViewsByUris(List<String> eventUris, Boolean unique) {
        StringBuilder urisBuilder = new StringBuilder(eventUris.get(0));
        for (int i = 1; i < eventUris.size(); i++) {
            urisBuilder.append(",").append(eventUris.get(i));
        }
        Map<String, Object> parameters = Map.of("uris", urisBuilder.toString(),
                "unique", unique);
        List<StatsDto> dtos = getStatsList(PREFIX_STATS, parameters);
        return dtos;
    }
}
