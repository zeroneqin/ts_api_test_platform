package io.zeroneqin.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = KafkaProperties.KAFKA_PREFIX)
@Getter
@Setter
public class KafkaProperties {
    public static final String KAFKA_PREFIX = "kafka";

    private String acks = "all";
    private String topic;
    private String fields;
    private String timestamp;
    private String bootstrapServers;
    private String sampleFilter;
    private String testMode;
    private String parseAllReqHeaders;
    private String parseAllResHeaders;
    private String compressionType;
    private String batchSize;
    private String clientId;
    private String connectionsMaxIdleMs;
    private KafkaProperties.Ssl ssl = new KafkaProperties.Ssl();
    private KafkaProperties.Log log = new KafkaProperties.Log();

    @Getter
    @Setter
    public static class Ssl {
        private String enabled = "false";
        private String keyPassword;
        private String keystoreLocation;
        private String keystorePassword;
        private String keystoreType;
        private String truststoreLocation;
        private String truststorePassword;
        private String truststoreType;
        private String protocol;
        private String enabledProtocols;
        private String provider;
    }

    @Getter
    @Setter
    public static class Log {
        private String topic;
    }
}
