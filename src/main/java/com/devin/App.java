package com.devin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.*;
import org.mongojack.JacksonDBCollection;

import javax.persistence.Id;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {

    @Id
    private String id;

    private String name;
    private String desc;

    public App() {
    }

    public App(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "App{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }

    private static class MyNameString {
        private String name;

        private MyNameString(String name) {
            this.name = name;
        }

        @JsonProperty
        public String getName() {
            return name;
        }
    }

    private static class MyDescString {
        private String desc;

        private MyDescString(String desc) {
            this.desc = desc;
        }

        @JsonProperty
        public String getDesc() {
            return desc;
        }
    }

    public static void main( String[] args ) throws UnknownHostException {
        final Mongo mongo = new MongoClient();
        try {
            final DB db = mongo.getDB("mongojack-basicdblist-bug");
            final DBCollection collection = db.getCollection("test");
            collection.drop();

            final JacksonDBCollection<App, String> coll = JacksonDBCollection.wrap(collection, App.class, String.class);

            final App obj1 = new App("devin", "smith");
            final App obj2 = new App("some", "body");
            coll.insert(obj1);
            coll.insert(obj2);

            // ArrayList works
            //final ArrayList<Object> orClauses = new ArrayList<Object>() {{
            //    add(new MyNameString("devin"));
            //    add(new MyDescString("body"));
            //}};

            // BasicDBList does not work
            final BasicDBList orClauses = new BasicDBList() {{
                add(new MyNameString("devin"));
                add(new MyDescString("body"));
            }};

            final List<App> results = coll.find(new BasicDBObject("$or", orClauses)).toArray();
            for (App a : results) {
                System.out.println(a);
            }
        } finally {
            mongo.close();
        }
    }
}
