/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

/**
 *
 * @author daniel
 */
public class ExtractedEntity {
    private String category;
    private String text;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ExtractedEntity(String category, String text) {
        this.category = category;
        this.text = text;
    }    
}
