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

    @Builder.Default
    private String signalType = "execute-snapshot";

    @Builder.Default
    private String snapshotType = "incremental";

    private Long fromIdInclusive;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;
}
