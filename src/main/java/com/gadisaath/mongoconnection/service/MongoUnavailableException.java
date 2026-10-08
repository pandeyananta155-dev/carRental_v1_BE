package com.gadisaath.mongoconnection.service;

/** Signals that a MongoDB operation could not be completed. */
public class MongoUnavailableException extends RuntimeException {
    /** Creates the domain exception while preserving the underlying database failure. */
    public MongoUnavailableException(Throwable cause) {
        super("MongoDB is unavailable", cause);
    }
}