package com.muun.health;

import com.codahale.metrics.health.HealthCheck;

public class LockFreeBlackListHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
