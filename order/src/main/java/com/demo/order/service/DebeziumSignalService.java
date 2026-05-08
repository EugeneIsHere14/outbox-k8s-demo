package com.demo.order.service;

import com.demo.order.dto.DebeziumSnapshotRequest;
import com.demo.order.entity.DebeziumSignalEntity;
import com.demo.order.repository.DebeziumSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DebeziumSignalService {

    private static final String DATABASE_NAME = "orders";
    private static final String SIGNAL_TYPE_EXECUTE_SNAPSHOT = "execute-snapshot";
    private static final String SNAPSHOT_TYPE_INCREMENTAL = "incremental";

    private final DebeziumSignalRepository debeziumSignalRepository;
    private final ObjectMapper objectMapper;

    public void triggerSnapshot(DebeziumSnapshotRequest request) {
        String dataCollection = DATABASE_NAME + "." + request.getTableName();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("data-collections", List.of(dataCollection));
        data.put("type", SNAPSHOT_TYPE_INCREMENTAL);

        String filter = buildFilter(request);

        if (!filter.isBlank()) {
            data.put("additional-conditions", List.of(Map.of(
                    "data-collection", dataCollection,
                    "filter", filter
            )));
        }

        DebeziumSignalEntity signal = DebeziumSignalEntity.builder()
                .id("snapshot-" + request.getTableName() + "-" + UUID.randomUUID())
                .type(SIGNAL_TYPE_EXECUTE_SNAPSHOT)
                .data(toString(data))
                .build();

        debeziumSignalRepository.save(signal);
    }

    private String buildFilter(DebeziumSnapshotRequest request) {
        List<String> conditions = new ArrayList<>();

        if (request.getFromId() != null) {
            conditions.add("id > " + request.getFromId());
        }

        if (request.getCreatedFrom() != null) {
            conditions.add("created_at >= '" + request.getCreatedFrom() + "'");
        }

        if (request.getCreatedTo() != null) {
            conditions.add("created_at <= '" + request.getCreatedTo() + "'");
        }

        return String.join(" AND ", conditions);
    }

    private String toString(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Debezium signal data", e);
        }
    }
}
