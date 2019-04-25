package mas.machine.jworkers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import mas.machine.Worker;
import org.bson.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class MongoWriter extends Worker {
    private final MongoWriterConfig config;

    /**
     * Vytvoří Worker.
     * @param iniFile soubor *.json s konfigurací
     */
    public MongoWriter(File iniFile) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(iniFile));
        config = gson.fromJson(reader, MongoWriterConfig.class);

        this.setDaemon(true);
    }

    @Override
    public void run() {
        MongoClient client = MongoClients.create(config.output);
        MongoDatabase db = client.getDatabase(config.database);
        MongoCollection<Document> col = db.getCollection(config.collection);

        Gson gson = new Gson();

        while(true) {
            try {
                DataSet msg = (DataSet) getMessage(config.input);
                String out = gson.toJson(msg);
                Document doc = Document.parse(out);
                col.insertOne(doc);
            } catch (Exception e){
                System.out.println(e);
            }
        }
    }

}
