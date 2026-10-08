package com.gadisaath.mongoconnection.service;

public class MongoUnavailableException extends RuntimeException {
    public MongoUnavailableException(Throwable cause) {
        super("MongoDB is unavailable", cause);
    }
}