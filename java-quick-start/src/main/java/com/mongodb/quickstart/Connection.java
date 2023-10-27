package com.mongodb.quickstart;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;



public class Connection {

    private static final Random rand = new Random();

    public static void main(String[] args) {
        String connectionString = System.getProperty("mongodb.uri");

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase sampleDB = mongoClient.getDatabase("sample_training");
            MongoCollection<Document> grades = sampleDB.getCollection("grades");
            
            insertManyDocuments(grades);

            // add a new field to each document with the called missed assesments with the value 0
            grades.updateMany(new Document(), new Document("$set", new Document("missed_assessments", 0)));

            // push the type extra credit to the scores array
            grades.updateMany(new Document(), new Document("$push", new Document("scores", new Document("type", "extra credit"))));

            // find one document and print it out
            Document doc = grades.find().first();
            System.out.println("Found document: " + doc);

            // delete all documents with student_id = 10000
            grades.deleteMany(eq("student_id", 10000));

            
            // drop the collection
            grades.drop();

        }
    }

    private static void insertManyDocuments(MongoCollection<Document> gradesCollection) {
        List<Document> grades = new ArrayList<>();
        for (double classId = 1d; classId <= 10d; classId++) {
            grades.add(generateNewGrade(10001d, classId));
        }

        // insert these documents into our db
        gradesCollection.insertMany(grades, new InsertManyOptions().ordered(false));
        System.out.println("Inserted " + grades.size() + " documents");
    }

    private static void updateOneDocuments(MongoCollection<Document> gradesCollection) {
                    // update one document
            Bson filter = eq("student_id", 10000);
            Bson updateOperation = set("comment", "You should learn MongoDB!");
            UpdateResult updateResult = gradesCollection.updateOne(filter, updateOperation);
            System.out.println("=> Updating the doc with {\"student_id\":10000}. Adding comment.");
            System.out.println(updateResult);
    }


    private static Document generateNewGrade(double studentId, double classId) {
        List<Document> scores = asList(new Document("type", "exam").append("score", rand.nextDouble() * 100),
                                       new Document("type", "quiz").append("score", rand.nextDouble() * 100),
                                       new Document("type", "homework").append("score", rand.nextDouble() * 100),
                                       new Document("type", "homework").append("score", rand.nextDouble() * 100));
        return new Document("_id", new ObjectId()).append("student_id", studentId)
                                                  .append("class_id", classId)
                                                  .append("scores", scores);
    }


}
