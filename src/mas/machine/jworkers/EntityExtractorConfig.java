/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import java.util.List;

/**
 *
 * @author daniel
 */
public class EntityExtractorConfig {
    
    public String input;
    public String output;
    
    public List<Entity> regexEntities;

    public List<EntityDictionary> entityDictionaries;
    
    public String dictionariesCategory;
}
