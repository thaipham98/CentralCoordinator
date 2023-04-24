package com.example.centralcoordinator.Health;

public class HealthCheckPayload {
    private String replicaId;
    private String timestamp;
    private String status;
    private double cpuUsage;
    private double memoryUsage;
    private double responseTime;

    // Constructor,
    public HealthCheckPayload(String replicaId, String timestamp, String status, double cpuUsage, double memoryUsage, double responseTime) {
        this.replicaId = replicaId;
        this.timestamp = timestamp;
        this.status = status;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.responseTime = responseTime;
    }

    public HealthCheckPayload() {

    }

    //generate all setters and getters


    public String getReplicaId() {
        return replicaId;
    }

    public void setReplicaId(String replicaId) {
        this.replicaId = replicaId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }


    @Override
    public String toString() {
        return "Replica: " + replicaId + " health status: " + status + " at " + timestamp +
                ", CPU usage = " + cpuUsage +
                ", memory usage = " + memoryUsage +
                ", response time = " + responseTime;
    }
}