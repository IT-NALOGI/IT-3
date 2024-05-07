package org.acme.resource;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
public class SimpleReadinessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        // Implement some check
        boolean ready = checkNetworkConnections();
        return ready ? HealthCheckResponse.up("I'm ready") : HealthCheckResponse.down("Not ready");
    }

    private boolean checkNetworkConnections() {
        // Check that the application can connect to its required services
        return true;
    }
}
