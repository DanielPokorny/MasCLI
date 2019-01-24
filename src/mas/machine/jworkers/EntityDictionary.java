/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 *
 * @author daniel
 */
class EntityDictionary {
    public int id;
    
    @XmlElementWrapper(name="entities")
    @XmlElement(name="entity")
    public List<Entity> entities;
}
