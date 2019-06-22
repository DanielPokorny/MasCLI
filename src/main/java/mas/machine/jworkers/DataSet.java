/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import java.util.ArrayList;
import java.util.List;

/**
 * Slouží k předávání dat mezi JWorery.
 * @author daniel
 */
public class DataSet {
    private String url;
    private String text;
    
    public List<ExtractedEntity> extractedEntities = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
