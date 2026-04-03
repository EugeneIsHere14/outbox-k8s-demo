package com.demo.payment.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Component
public class DbHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DbHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT 1")) {
            ps.execute();

            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
