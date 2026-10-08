package com.gadisaath.mongoconnection.service;

import com.mongodb.MongoException;
import org.bson.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Runs MongoDB connectivity checks and retrieves application configuration. */
@Service
public class MongoConfigurationService {
    private static final String COLLECTION_NAME = "app_config";

    private final MongoTemplate mongoTemplate;

    /** Creates the service with Spring Data's MongoDB access helper. */
    public MongoConfigurationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Sends MongoDB's ping command so health checks verify a live database connection.
     *
     * @throws MongoUnavailableException when MongoDB cannot execute the ping
     */
    public void ping() {
        try {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
        } catch (MongoException | DataAccessException exception) {
            throw new MongoUnavailableException(exception);
        }
    }

    /**
     * Finds the first configuration in {@code app_config} with the requested type code.
     * The MongoDB identifier field is excluded from the returned document.
     *
     * @param typeCode exact configuration identifier to search for
     * @return the matching document, or empty when no document exists
     * @throws MongoUnavailableException when MongoDB cannot execute the query
     */
    public Optional<Document> findByTypeCode(String typeCode) {
        Query query = Query.query(Criteria.where("typeCode").is(typeCode));
        query.fields().exclude("_id");

        try {
            return Optional.ofNullable(mongoTemplate.findOne(query, Document.class, COLLECTION_NAME));
        } catch (MongoException | DataAccessException exception) {
            throw new MongoUnavailableException(exception);
        }
    }
}