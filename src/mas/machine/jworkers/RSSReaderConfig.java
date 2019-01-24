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
public class RSSReaderConfig {
    @XmlElement(name = "proxy")
    public NetProxy proxy;

    /**
     * Adresa rss streamu.
     */
    public String url;

    public String output;
}
