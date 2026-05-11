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

    private static final Set<String> ALLOWED_SIGNAL_TYPES = Set.of("execute-snapshot");

    private static final Set<String> ALLOWED_SNAPSHOT_TYPES = Set.of("incremental", "blocking");

    private final DebeziumSignalRepository debeziumSignalRepository;
    private final ObjectMapper objectMapper;

    public void triggerSnapshot(DebeziumSnapshotRequest request) {
        validateRequest(request);

        String dataCollection = DATABASE_NAME + "." + request.getTableName();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("data-collections", List.of(dataCollection));
        data.put("type", request.getSnapshotType());

        String filter = buildFilter(request);

        if (!filter.isBlank()) {
            data.put("additional-conditions", List.of(Map.of(
                    "data-collection", dataCollection,
                    "filter", filter
            )));
        }

        DebeziumSignalEntity signal = DebeziumSignalEntity.builder()
                .id("snapshot-" + request.getTableName() + "-" + UUID.randomUUID())
                .type(request.getSignalType())
                .data(toString(data))
                .build();

        debeziumSignalRepository.save(signal);
    }

    private void validateRequest(DebeziumSnapshotRequest request) {
        if (!ALLOWED_SIGNAL_TYPES.contains(request.getSignalType())) {
            throw new IllegalArgumentException("Unsupported Debezium signal type: " + request.getSignalType());
        }

        if (!ALLOWED_SNAPSHOT_TYPES.contains(request.getSnapshotType())) {
            throw new IllegalArgumentException("Unsupported Debezium snapshot type: " + request.getSnapshotType());
        }
    }

    private String buildFilter(DebeziumSnapshotRequest request) {
        List<String> conditions = new ArrayList<>();

        if (request.getFromIdInclusive() != null) {
            conditions.add("id >= " + request.getFromIdInclusive());
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
