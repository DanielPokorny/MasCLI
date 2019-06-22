/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.cli;

import mas.factory.Factory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author daniel
 */
public class Listener extends Thread{
    private Factory factory;
    private String machine;
    private String workspace;
    private String commLine;
    private LinkedBlockingQueue<Object> queue = null;

    public Listener(Factory factory, String machine, String workspace, String commLine) throws InterruptedException {
        this.factory = factory;
        this.machine = machine;
        this.workspace = workspace;
        this.commLine = commLine;
    }

    @Override
    public void run() {
        while(queue == null) {
            try {
                queue = factory.getCommLine(machine, workspace, commLine);
            } catch (Exception ex) {
            }

            if(queue == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }
        }

        boolean live = true;
        while(live) {
            try {
                Object message = null;
                try {
                    message = queue.poll(10, TimeUnit.SECONDS);
                } catch (NullPointerException ex) {
                    System.out.println("null");
                }

                if(message == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex1) {
                    }
                } else {
                    System.out.println(machine + ":" + workspace + ":" + commLine + " " + message.toString());
                }
            } catch (InterruptedException ex) {
                live = false;
            }
        }
        System.out.println("Listener " + machine + ":" + workspace + ":" + commLine + " destroyed.");
    }
}
