package mas.cli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mas.factory.Factory;
import mas.machine.Machine;

/**
 * Ovládání systému z terminálu.
 * Created by daniel on 4.10.16.
 * verze 1.0.0
 */

public class MasCLI {
    private static Factory factory = new Factory();
    private static int speed = 1000;

    private static final TreeMap<String, Machine> machines_ = new TreeMap<>();
    private static final TreeMap<String, Listener> listeners_ = new TreeMap<>();

    /**
     * Metoda pro testování funkčnosti zbytku.
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException {
        if(args.length > 0) {
            for(String s : args) {
                System.out.println("Processing script " + s);
                processScript(s);
            }
        }

        boolean working = true;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(working) {
            String command = br.readLine();
            if(command.toUpperCase().equals("QUIT")) {
                working = false;
                List<Listener> listenersList = new ArrayList<>(listeners_.values());
                for(Listener listener : listenersList) {
                    listener.interrupt();
                }
            } else {
                System.out.println(processLine(command));
//                f.processCommand(command);
//                System.out.println(f.controlQueue.take().toString());
            }
        }
    }

    public static void processScript(String path) throws FileNotFoundException, IOException, InterruptedException, ClassNotFoundException, NoSuchMethodException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        while(br.ready()) {
            String line = br.readLine();
            System.out.println(line);
            System.out.println(processLine(line));
            Thread.sleep(speed);
        }
    }

    public static String processLine(String line) throws InterruptedException, IOException, ClassNotFoundException, NoSuchMethodException {
        String returnValue = "";
        String upLine = line.toUpperCase();
        if(upLine.startsWith("LISTENER START")) {
            String parameter = line.substring(15);
            String m = parameter.substring(0, parameter.indexOf(":"));
            parameter = parameter.substring(parameter.indexOf(":") + 1);
            String w = parameter.substring(0, parameter.indexOf(":"));
            String cl = parameter.substring(parameter.indexOf(":") + 1);
            listeners_.put(line.substring(15), new Listener(factory, m, w, cl));
            listeners_.get(line.substring(15)).start();
            returnValue = "Listener " + parameter + " started.";
        } else {
            try {
                returnValue = factory.processCommand(line);
            } catch (InstantiationException ex) {
                Logger.getLogger(MasCLI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(MasCLI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(MasCLI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(MasCLI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnValue;
    }
}
