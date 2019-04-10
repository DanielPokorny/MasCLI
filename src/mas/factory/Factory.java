/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.factory;

import mas.machine.Machine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author daniel
 */
public class Factory {
    private Path currentRepository = null;
    private String currentWorkspace = "";
    private String currentMachine = "";
    private final TreeMap<String, Machine> machines = new TreeMap<>();

    /**
     * Metoda, zajišťující řízení systému pomocí příkazů.
     *
     * |Příkaz     |Modifikátor|Parametr    |Popis                                  |
     * |-----------|----------:|------------|---------------------------------------|
     * | REPOSITORY|SET        |cesta       |nastaví cestu k repozitáři             |
     * | WORKSPACE |CREATE     |název       |vytvoří workspace                      |
     * |           |SET        |název       |nastaví aktuální workspace             |
     * | MACHINE   |CREATE     |název       |vytvoří nový stroj                     |
     * |           |SET        |název       |nastaví aktuální stroj                 |
     * | BWORKER   |LOAD       |název       |nahraje worker z repository            |
     * |           |RUN        |název       |spustí nahraný worker                  |
     * | JWORKER   |LOAD       |typ název   |nahraje worker daného typu z repository|
     * |           |RUN        |název       |spustí nahraný worker                  |
     * | COMMLINE  |CREATE     |název       |vytvoří commline                       |
     * | PRINT     |           |commline    |kam se má poslat text                  |
     * |           |           |text        |vlastní text                           |
     *
     * Příklady
     * --------
     * ~~~~~~~~~~~
     * REPOSITORY SET /home/myrepo/
     * WORKSPACE CREATE workspace
     * WORKSPACE SET workspace
     * MACHINE CREATE mašina
     * MACHINE SET mašina
     * BWORKER LOAD tester
     * BWORKER RUN tester
     * COMMLINE CREATE lajna
     * PRINT lajna, "ahoj"
     * ~~~~~~~~~~~
     *
     * @param command řetězec obsahující příkaz, který se má zpracovat
     * @throws IOException
     */
    public String processCommand(String command) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String returnValue = "";
        int spacePosition = command.indexOf(" ");
        String parameter = "";
        if(spacePosition > -1) {
            parameter = command.substring(spacePosition + 1).trim();
            command = command.substring(0, spacePosition).toUpperCase();
        }

        switch(command) {
            case "REPOSITORY":
                if(parameter.toUpperCase().startsWith("SET")) {
                    parameter = parameter.substring(3).trim();
                    setCurrentRepository(parameter);
                    if(Files.exists(currentRepository)) {
                        machines.get(currentMachine).getWorkspace(currentWorkspace).setRepository(currentRepository);
                        returnValue = "Current repository is: " + currentRepository.toString();
                    } else {
                        returnValue = "Invalid repository.";
                    }
                }
                break;

            case "WORKSPACE":
                if(parameter.toUpperCase().startsWith("CREATE")) {
                    parameter = parameter.substring(6).trim();
                    machines.get(currentMachine).addWorkspace(parameter);
                    currentWorkspace = parameter;
                    returnValue = "Workspace \"" + parameter + "\" created. Current workspace is: " + currentMachine + ":" + parameter;
                }

                if(parameter.toUpperCase().startsWith("SET")) {
                    parameter = parameter.substring(3).trim();
                    if(machines.get(currentMachine).getWorkspace(parameter) != null) {
                        currentWorkspace = parameter;
                        returnValue = "Current workspace is: " + parameter;
                    } else {
                        returnValue = "Workspace \"" + parameter + "\" does not exist. Current workspace is: " + currentWorkspace;
                    }
		}
                break;

            case "MACHINE":
                if(parameter.toUpperCase().startsWith("CREATE")) {
                    parameter = parameter.substring(6).trim();
                    Machine machine = new Machine(parameter);
                    machines.put(machine.getMachineName(), machine);
                    currentMachine = machine.getMachineName();
                    returnValue = "Machine \"" + machine.getMachineName()+ "\" created. Current machine is: " + machine.getMachineName();
                }
                break;

            case "BWORKER":
                if(parameter.toUpperCase().startsWith("LOAD")) {
                    parameter = parameter.substring(4).trim();
                    File workerFile = new File(currentRepository.toString() + File.separator + "Workers"
                            + File.separator + "Basic" + File.separator + parameter);
                    BufferedReader reader = new BufferedReader(new FileReader(workerFile));
                    machines.get(currentMachine).addBasicWorker(parameter, currentWorkspace);
                    String response = "";
                    while(reader.ready()) {
                        String line = reader.readLine();
                        machines.get(currentMachine).addLine(parameter, line);
                        response += line + "\r\n";
                    }
                    returnValue = response;
                }

                if(parameter.toUpperCase().startsWith("RUN")) {
                    parameter = parameter.substring(3).trim();
                    machines.get(currentMachine).runWorker(parameter);
                    returnValue = "Basic worker " + parameter + " is running.";
                }
                break;

            case "JWORKER":
                if(parameter.toUpperCase().startsWith("LOAD")) {
                    parameter = parameter.substring(4).trim();
                    String type = parameter.substring(0, parameter.indexOf(" "));
                    String name = parameter.substring(parameter.indexOf(" ") + 1);
                    File iniFile = new File(currentRepository.toString() + File.separator + "Workers"
                            + File.separator + "Java" + File.separator + name + ".json");
                    machines.get(currentMachine).addJavaWorker(type, name, currentWorkspace, iniFile);
                    String response = "Java worker " + name + " loaded.";
                    returnValue = response;
                }

                if(parameter.toUpperCase().startsWith("RUN")) {
                    parameter = parameter.substring(3).trim();
                    machines.get(currentMachine).runWorker(parameter);
                    returnValue = "Java worker " + parameter + " is running.";
                }
                break;

            case "COMMLINE":
                if(parameter.toUpperCase().startsWith("CREATE")) {
                    parameter = parameter.substring(6).trim();
                    machines.get(currentMachine).getWorkspace(currentWorkspace).addCommLine(parameter);
                    returnValue = "Commline \"" + parameter + "\" created.";
                }
                break;

            case "PRINT":
                {
                    String cl = parameter.substring(0, parameter.indexOf(",")).trim();
                    String message = parameter.substring(parameter.indexOf(",") + 1);
                    machines.get(currentMachine).sendMessage(currentWorkspace, cl, message);
                    returnValue = "Message " + message + " sent to " + cl + ".";
                }
                break;
        }
        return returnValue;
    }

    public void setCurrentRepository(String repositoryPath) {
        currentRepository = Paths.get(repositoryPath);
    }

    public LinkedBlockingQueue<Object> getCommLine(String machine, String workspace, String name) {
        LinkedBlockingQueue<Object> returnValue = null;
        try {
            returnValue = machines.get(machine).getWorkspace(workspace).getCommline(name).getQueue();
        } catch (Exception ex) {

        }
        return returnValue;
    }
}
