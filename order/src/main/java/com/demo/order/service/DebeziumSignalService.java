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

    private static final String DEFAULT_SIGNAL_TYPE = "execute-snapshot";
    private static final String DEFAULT_SNAPSHOT_TYPE = "incremental";

    private static final Set<String> ALLOWED_SIGNAL_TYPES = Set.of("execute-snapshot");

    private static final Set<String> ALLOWED_SNAPSHOT_TYPES = Set.of("incremental", "blocking");

    private final DebeziumSignalRepository debeziumSignalRepository;
    private final ObjectMapper objectMapper;

    public void triggerSnapshot(DebeziumSnapshotRequest request) {
        String signalType = resolveSignalType(request);
        String snapshotType = resolveSnapshotType(request);

        validateRequest(signalType, snapshotType);

        String dataCollection = DATABASE_NAME + "." + request.getTableName();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("data-collections", List.of(dataCollection));
        data.put("type", snapshotType);

        String filter = buildFilter(request);

        if (!filter.isBlank()) {
            data.put("additional-conditions", List.of(Map.of(
                    "data-collection", dataCollection,
                    "filter", buildDebeziumFilter(snapshotType, dataCollection, filter)
            )));
        }

        DebeziumSignalEntity signal = DebeziumSignalEntity.builder()
                .id("snapshot-" + request.getTableName() + "-" + UUID.randomUUID())
                .type(signalType)
                .data(toString(data))
                .build();

        debeziumSignalRepository.save(signal);
    }

    private String resolveSignalType(DebeziumSnapshotRequest request) {
        return Optional.ofNullable(request.getSignalType())
                .filter(value -> !value.isBlank())
                .orElse(DEFAULT_SIGNAL_TYPE);
    }

    private String resolveSnapshotType(DebeziumSnapshotRequest request) {
        return Optional.ofNullable(request.getSnapshotType())
                .filter(value -> !value.isBlank())
                .orElse(DEFAULT_SNAPSHOT_TYPE);
    }

    private void validateRequest(String signalType, String snapshotType) {
        if (!ALLOWED_SIGNAL_TYPES.contains(signalType)) {
            throw new IllegalArgumentException("Unsupported Debezium signal type: " + signalType);
        }

        if (!ALLOWED_SNAPSHOT_TYPES.contains(snapshotType)) {
            throw new IllegalArgumentException( "Unsupported Debezium snapshot type: " + snapshotType);
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

    private String buildDebeziumFilter(String snapshotType, String dataCollection, String filter) {
        if ("blocking".equals(snapshotType)) {
            return "SELECT * FROM " + dataCollection + " WHERE " + filter;
        }

        return filter;
    }

    private String toString(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Debezium signal data", e);
        }
    }
}
