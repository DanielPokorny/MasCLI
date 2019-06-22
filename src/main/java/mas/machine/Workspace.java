/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Pracovní prostor pro interpretery. Obsahuje komunikační linky a zajišťuje jejich správu.
 * @author daniel
 */
public class Workspace {
    /**
     * Jméno workspace.
     */
    private final String name_;
    private Path repositoryPath_;
    private ArrayList<FileHandler> fileHandlers_ = new ArrayList<>();

    /**
     * List s komunikačními linkami.
     */
    private final List<CommLine> commLines_ = new ArrayList<>();

    /**
     * Vrátí zadanou linku.
     * @param name  jméno linky
     * @return linka
     */
    public CommLine getCommline(String name) {
        CommLine returnValue = null;

        for(CommLine cl : commLines_) {
            if(cl.getName().equals(name)) {
                returnValue = cl;
            }
        }

        return returnValue;
    }

    /**
     * Vytvoří nový workspace.
     * @param name  jméno workspace
     * @throws IOException
     */
    public Workspace(String name) throws IOException {
        this.name_ = name;
    }

    /**
     * Zašle zprávu do zadané linky. Jméno linky je vyhodnocováno jako regulární výraz, tedy je možno zaslat jednu
     * zprávu do více linek naráz.
     * @param address   jméno linky
     * @param message   zpráva
     */
    public void sendMessage(String address, Object message) {
        for(CommLine cl : commLines_) {
            if(cl.getName().matches(address)){
                cl.sendMessage(message);
            }
        }
    }

    /**
     * Vyzvedne zprávu z linky.
     * @param address   jméno linky
     * @return  zpráva
     * @throws InterruptedException
     */
    public Object getMessage(String address) throws InterruptedException {
        Object returnValue = null;
        for(CommLine cl : commLines_) {
            if(cl.getName().matches(address)){
                returnValue = cl.getMessage();
            }
        }
        return returnValue;
    }

    /**
     * Přidá komunikační linku. Pokud linka již existuje, nedělá nic.
     * @param name  jméno linky
     * @throws IOException
     */
    public void addCommLine(String name) throws IOException{
        boolean newLine = true;
        for(CommLine cl : commLines_) {
            if(cl.equals(name)) {
                newLine = false;
            }
        }

        if(newLine) {
            commLines_.add(new CommLine(name));
        }
    }

    /**
     * Vrátí jméno workspace.
     * @return jméno workspace
     */
    public String getName() {
        return name_;
    }

    /**
     * Zruší komunikační linku bez ohledu na to, zda je prázdná.
     * @param name  jméno linky
     */
    public void closeCommLine(String name) {
        int index = 0;
        for(int i = 0; i < commLines_.size(); i++) {
            if(commLines_.get(i).getName().equals(name)) {
                index = i;
            }
        }
        commLines_.remove(index);
    }

    public void setRepository(Path path) {
        repositoryPath_ = path;
    }

    public FileHandler getFileHandler() {
        FileHandler returnValue = new FileHandler(repositoryPath_);
        fileHandlers_.add(returnValue);
        return returnValue;
    }
}
