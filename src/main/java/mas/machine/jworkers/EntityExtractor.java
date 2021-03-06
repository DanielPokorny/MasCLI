package mas.machine.jworkers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mas.machine.Worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author daniel
 */
public class EntityExtractor extends Worker{  
    private class EntityHit {
        String text;
        String value;
        int start;
        int end;
    }
    
    private static ArrayList<ArrayList<EntityHit>> allHits;
    
    /**
     * Obsahuje konfiguraci workeru načtenou z *.xml.
     */
    private final EntityExtractorConfig config;

    /**
     * Vytvoří Worker.
     * @param iniFile soubor *.xml s konfigurací
     * @throws JAXBException
     */
    public EntityExtractor(File iniFile) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(iniFile));
        config = gson.fromJson(reader, EntityExtractorConfig.class);

        this.setDaemon(true);
    }
    
    /**
     * Vlastní běh Workeru.
     */
    @Override
    public void run() {
        List<Integer> dictionaryIDList = new ArrayList<>();
        for(EntityDictionary entityDictionary : config.entityDictionaries) {
            dictionaryIDList.add(entityDictionary.id);
        }
        Collections.sort(dictionaryIDList);
        
        while(true) {
            DataSet msg = null;
            
            try {
                msg = (DataSet) getMessage(config.input);
                String text = msg.getText();
                
                for(Entity entity : config.regexEntities) {
                    Pattern pattern;
                    pattern = Pattern.compile(entity.regex);
                    Matcher matcher = pattern.matcher(text);

                    while(matcher.find()) {
                        String entityText = matcher.group(0);
                        msg.extractedEntities.add(new ExtractedEntity(entity.category, entityText));
                    }
                }
                
                allHits = new ArrayList<>();
                for(Integer dictionaryID : dictionaryIDList) {
                    allHits.add(findEntity(dictionaryID, text));
                }
                
                for(EntityHit firstHit : allHits.get(0)) {
                    String nextHit = getNextHit(1, firstHit.end);
                    if(nextHit != null) {
                        String hit = firstHit.value + nextHit;
                        msg.extractedEntities.add(new ExtractedEntity(config.dictionariesCategory, hit));
                        System.out.println(hit);
                    }
                }
                
                if(msg.extractedEntities.size() > 0) {
                    sendMesage_(config.output, msg);
                    System.out.println(msg.getText());
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(EntityExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    EntityDictionary getEntityDictionary(int dictionaryID) {
        EntityDictionary returnValue = null;
        for (EntityDictionary entityDictionary : config.entityDictionaries) {
            if(entityDictionary.id == dictionaryID) {
                returnValue = entityDictionary;
            }
        }
        return returnValue;
    }
    
    ArrayList<EntityHit> findEntity(int entityDictionaryID, String text) {
        ArrayList<EntityHit> returnValue = new ArrayList<>();
        
        EntityDictionary entityDictionary = getEntityDictionary(entityDictionaryID);
        if(entityDictionary != null) {
            for(Entity entity : entityDictionary.entities) {
                Pattern pattern;
                pattern = Pattern.compile(entity.regex);
                Matcher matcher = pattern.matcher(text);
                while(matcher.find()) {
                    EntityHit entityHit = new EntityHit();
                    entityHit.text = matcher.group(0);
                    entityHit.value = entity.value;
                    entityHit.start = matcher.start();
                    entityHit.end = matcher.end();
                    returnValue.add(entityHit);
                }
            }
        }
        return returnValue;
    }
    
    private String getNextHit(int hitsID, int start) {
        String returnValue = null;
        if(hitsID < allHits.size()) {
            for(EntityHit entityHit : allHits.get(hitsID)) {
                if(entityHit.start == start) {
                    String nextText = getNextHit(hitsID + 1, entityHit.end);
                    if(nextText != null) {
                        returnValue = entityHit.value + nextText;
                    }
                }
            }
        } else {
            returnValue = "";
        }
        return returnValue;
    }

}
