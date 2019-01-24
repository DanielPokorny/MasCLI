/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author daniel
 */
@XmlRootElement(name = "settings")
public class EntityExtractorConfig {
    
    public String input;
    public String output;
    
    @XmlElementWrapper(name="entities")
    @XmlElement(name="entity")
    public List<Entity> regexEntities;    

    @XmlElementWrapper(name="dictionaries")
    @XmlElement(name="dictionary")
    public List<EntityDictionary> entityDictionaries;
    
    public String dictionariesCategory;
}
