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

    private Long fromId;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;
}
