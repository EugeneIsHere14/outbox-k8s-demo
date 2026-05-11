package com.demo.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebeziumSnapshotRequest {

    @NotBlank
    private String tableName;

    private String signalType;

    private String snapshotType;

    private Long fromIdInclusive;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;
}
