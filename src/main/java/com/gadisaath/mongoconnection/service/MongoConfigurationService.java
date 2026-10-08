package com.gadisaath.mongoconnection.service;

import com.mongodb.MongoException;
import org.bson.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MongoConfigurationService {
    private static final String COLLECTION_NAME = "app_config";

    private final MongoTemplate mongoTemplate;

    public MongoConfigurationService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void ping() {
        try {
            mongoTemplate.getDb().runCommand(new Document("ping", 1));
        } catch (MongoException | DataAccessException exception) {
            throw new MongoUnavailableException(exception);
        }
    }

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