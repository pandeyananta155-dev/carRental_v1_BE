package com.gadisaath.mongoconnection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Boots the Spring application and discovers components in this package. */
@SpringBootApplication
public class MongoConnectionTestApplication {
    /**
     * Starts the embedded web server and the configured Spring components.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(MongoConnectionTestApplication.class, args);
    }
}