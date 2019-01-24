/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine;

/**
 *
 * @author daniel
 */
public class Worker extends Thread{
    private String name_;
    private Workspace workspace_;

    /**
     * Nastaví jméno Workeru.
     * @param name jméno Workeru
     */
    public void setWorkerName(String name) {
        name_ = name;
    }

    /**
     * Vrátí jméno workeru.
     * @return jméno
     */
    public String getWorkerName() {
        return name_;
    }

    /**
     * Nastaví workspace, se kterým Worker pracuje.
     * @param workspace jméno workspace
     */
    public void setWorkspace(Workspace workspace) {
        workspace_ = workspace;
    }

    /**
     * Odešle zprávu do commlinky s danou adresou.
     * @param address adresa
     * @param message zpráva
     */
    protected void sendMesage_(String address, Object message) {
        workspace_.sendMessage(address, message);
    }

    /**
     * Vrátí zprávu ze zadané adresy commlinky.
     * @param address adresa
     * @return zpráva
     * @throws InterruptedException
     */
    protected Object getMessage_(String address) throws InterruptedException {
        return workspace_.getMessage(address);
    }
}
