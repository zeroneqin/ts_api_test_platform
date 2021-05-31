package io.zeroneqin.performance.engine;

public interface Engine {
    Long getStartTime();

    String getReportId();

    void start();

    void stop();
}
