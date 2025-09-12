package co.com.pragma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;

@Configuration
public class AwsSdkConfig {

    @Bean
    public MetricPublisher metricPublisher() {
        return new MetricPublisher() {
            @Override
            public void publish(MetricCollection metricCollection) {
                // No-op para desarrollo
            }

            @Override
            public void close() {
                // No-op para desarrollo
            }
        };
    }
}