/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author daniel
 */
@XmlRootElement(name = "settings")
@XmlSeeAlso({NetProxy.class})
public class URLReaderConfig {
    /**
     * definice proxy serveru
     * @see NetProxy
     */
    @XmlElement(name = "proxy")
    public NetProxy proxy;

    /**
     * Commlink, ze kterého čte adresy.
     */
    public String input;

    /**
     * Commlink, do kterého zapisuje načtená data.
     */
    public String output;

    /**
     * Název elementu, který má být ze stránky extrahován. Není-li zadán, je extrahován kompletní text.
     */
    public String element;
}
