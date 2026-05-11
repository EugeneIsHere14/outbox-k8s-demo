package com.demo.order.controller;

import com.demo.order.dto.DebeziumSnapshotRequest;
import com.demo.order.service.DebeziumSignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/debezium/snapshots")
@RequiredArgsConstructor
public class DebeziumSnapshotController {

    private final DebeziumSignalService debeziumSignalService;

    @PostMapping
    public ResponseEntity<Void> triggerSnapshot(@Valid @RequestBody DebeziumSnapshotRequest request) {
        debeziumSignalService.triggerSnapshot(request);

        return ResponseEntity.accepted().build();
    }
}