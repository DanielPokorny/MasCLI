/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Komunikační linka. Lze do ní ukládat zprávy typu String a vyzvedávat je. Jedná se v principu o zásobník typu FIFO.
 * @author daniel
 */
public class CommLine {
    /**
     * Jméno linky.
     */
    private String name_ = "";

    /**
     * Vlastní linka - fronta LinkedBlockingQueue.
     */
    private final LinkedBlockingQueue<Object> queue_;

    /**
     * Vytvoří linku se jménem name.
     * @param name  jméno linky
     * @throws IOException
     */
    public CommLine(String name) throws IOException {
        this.queue_ = new LinkedBlockingQueue<>();
        this.name_ = name;
    }

    /**
     * Vrátí jméno linky.
     * @return jméno linky
     */
    public String getName() {
        return name_;
    }

    /**
     * Načte zprávu z linky. Pokud je linka prázdná, čeká, až se nějaká objeví.
     * @return  zpráva
     * @throws InterruptedException
     */
    public Object getMessage() throws InterruptedException {
        return queue_.take();
    }

    /**
     * Odešle zprávu do linky.
     * @param message zpráva
     */
    public void sendMessage(Object message) {
        queue_.add(message);
    }

    public LinkedBlockingQueue<Object> getQueue() {
        return queue_;
    }
}
