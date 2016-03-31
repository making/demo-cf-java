package com.example;

import am.ik.voicetext4j.Speaker;
import org.cloudfoundry.logging.LogMessage;
import org.cloudfoundry.logging.LoggingClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.spring.logging.SpringLoggingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.timer.Timer;

import java.time.Duration;
import java.util.List;

@SpringBootApplication
public class DemoCfJavaApplication {
    @Autowired
    CloudFoundryProperties props;

    @Bean
    SpringCloudFoundryClient cloudFoundryClient() {
        return SpringCloudFoundryClient.builder()
                .host(props.getHost())
                .username(props.getUsername())
                .password(props.getPassword())
                .skipSslValidation(props.isSkipSslValidation())
                .build();
    }

    @Bean
    LoggingClient loggingClient() {
        return SpringLoggingClient.builder()
                .cloudFoundryClient(cloudFoundryClient())
                .build();
    }

    @Bean
    CloudFoundryOperations cloudFoundryOperations() {
        return new CloudFoundryOperationsBuilder()
                .cloudFoundryClient(cloudFoundryClient())
                .loggingClient(loggingClient())
                .target(props.getOrganization(), props.getSpace())
                .build();
    }

    @Bean
    CommandLineRunner commandLineRunner(CloudFoundryOperations operations) {
        return args -> {
            Flux<LogMessage> logs = operations.applications()
                    .logs(LogsRequest.builder()
                            .name(props.getApplicationName()).build());
            logs.filter(log -> "RTR".equals(log.getSourceName()))
                    .map(LogMessage::getMessage)
                    .filter(msg -> msg.contains(" 500 ")) // 500 error
                    .useTimer(Timer.create())
                    .buffer(Duration.ofSeconds(10))
                    .map(List::size)
                    .filter(x -> x > 5)// 5 errors in 10 sec
                    .consume(x -> {
                        System.out.println(x + " errors in 10 seconds!");
                        // some alerts
                        Speaker.SHOW.ready().speak("緊急事態発生"); // Emergency!
                    });
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoCfJavaApplication.class, args);
    }
}
