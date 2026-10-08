package com.gadisaath.mongoconnection;

import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class MongoConnectionTestApplication implements ApplicationRunner {
    private final MongoTemplate mongoTemplate;

    public MongoConnectionTestApplication(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public static void main(String[] args) {
        try {
            SpringApplication.run(MongoConnectionTestApplication.class, args);
        } catch (RuntimeException exception) {
            System.err.println("MongoDB connection failed: " + exception.getMessage());
            throw exception;
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        mongoTemplate.getDb().runCommand(new Document("ping", 1));
        System.out.println("MongoDB connection successful");
    }
}