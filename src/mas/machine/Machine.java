/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author daniel
 */
public class Machine {
    private List<Worker> workers_ = new ArrayList<>();
    private List<Workspace> workspaces_ = new ArrayList<>();
    private String machineName_;

    public String getMachineName() {
        return machineName_;
    }

    public Object getMessage(String workspace, String commLine) throws InterruptedException {
        Object returnValue = null;

        returnValue = getCommLine_(workspace, commLine).getMessage();

        return returnValue;
    }

    public void sendMessage(String workspace, String commLine, String message) {
        getCommLine_(workspace, commLine).sendMessage(message);
    }

    private CommLine getCommLine_(String workspace, String name) {
        CommLine returnValue = null;

        for(Workspace ws : workspaces_) {
            if(ws.getName().equals(workspace)) {
                returnValue = ws.getCommline(name);
            }
        }

        return returnValue;
    }

    public void addBasicWorker(String name, String workspace) {
        workers_.add(new BasicWorker(name, getWorkspace(workspace)));
    }

    public void addJavaWorker(String type, String name, String workspace, File iniFile) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        StringBuilder stb = new StringBuilder();
        stb.append("mas.machine.jworkers.");
        stb.append(type);
        String typeLong = stb.toString();
        Class t = Class.forName(typeLong);
        Constructor<?> constructor = t.getConstructor(java.io.File.class);
        Worker jworker = (Worker) constructor.newInstance(iniFile);
        jworker.setWorkerName(name);
        jworker.setWorkspace(getWorkspace(workspace));
        workers_.add(jworker);
    }

    public void addWorkspace(String name) throws IOException {
        Workspace newWorkspace = new Workspace(name);
        workspaces_.add(newWorkspace);
    }

    public Workspace getWorkspace(String workspaceName) {
        Workspace returnValue = null;
        for(Workspace ws : workspaces_) {
            if(ws.getName().equals(workspaceName)) {
                returnValue = ws;
            }
        }
        return returnValue;
    }

    public void addLine(String interpreter, String line) {
        for(Worker worker : workers_) {
            if(worker.getWorkerName().equals(interpreter)) {
                BasicWorker bw = (BasicWorker) worker;
                bw.addLine(line);
            }
        }
    }

    public void runWorker(String workerName) {
        for(Worker worker : workers_) {
            if(worker.getWorkerName().equals(workerName)) {
                worker.start();
            }
        }
    }

    public Machine(String machineName) {
        machineName_ = machineName;
    }
}
