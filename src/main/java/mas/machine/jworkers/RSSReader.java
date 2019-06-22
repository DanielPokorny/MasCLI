/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import mas.machine.Worker;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Odesílá nové URL extrahované z RSS feedu do výstupní linky.
 * @author daniel
 * @version 2.0
 */
public class RSSReader extends Worker{
    RSSReaderConfig config;

    /**
     * List obsahující nově přidané odkazy.
     */
    private List<String> newLinks = new ArrayList<>();

    /**
     * List obsahující již navštívené odkazy.
     */
    private List<String> oldLinks = new ArrayList<>();
    
    /**
     * Vytvoří nový RSSReader
     * @param iniFile cesta k souboru s konfiguraci
     */
    public RSSReader(File iniFile) throws Exception {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(iniFile));
        config = gson.fromJson(reader, RSSReaderConfig.class);

        this.setDaemon(true);
    }

    @Override
    public void run() {
        while(true) {
            boolean nextRead = true;
            do{
                try {
                    readLinks();
                } catch (IOException ex) {
                    System.out.println(ex.getLocalizedMessage());
                }

                if(newLinks.size() > 0){
                    nextRead = false;
                }else{
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } while(nextRead);

            String outLink = newLinks.get(0);
            newLinks.remove(0);
            oldLinks.add(outLink);

            sendMesage_(config.output, outLink);
        }
    }

    /**
     * Extrahuje odkazy z textu.
     * @throws IOException něco je špatně.
     */
    private void readLinks() throws IOException, IllegalArgumentException {
        URL rssURL = null;
        try {
            rssURL = new URL(config.url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        URLConnection conn = null;

        if(config.proxy != null) {
            Authenticator.setDefault(authenticator);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.proxy.address, config.proxy.port));

            try {
                    conn = rssURL.openConnection(proxy);
            } catch (IOException ex) {
                    Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            conn = rssURL.openConnection();
        }


        StringBuilder content = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            content.append(line + "\n");
        }
        bufferedReader.close();

        String input = content.toString();

        Pattern pattern = Pattern.compile("<link>.+<\\/link>");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String link = matcher.group(0);
            link = link.substring(6).substring(0, link.lastIndexOf("<") - 6);
            if(link != null) {
                if(isNewLink(link)){
                    newLinks.add(link);
                }
            }
        }
    }

    /**
     * Autentizátor pro RSSProxy.
     */
    Authenticator authenticator = new Authenticator() {
        /**
         * Autentizace pro RSSProxy.
         * @return autentizace pro RSSProxy.
         */
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(config.proxy.username, config.proxy.password.toCharArray()));
        }
    };

    /**
     * Otestuje odkaz, zda je nový.
     * @param link odkaz.
     * @return true, pokud je odkaz nový.
     */
    private boolean isNewLink(String link){
        boolean returnValue = true;
        for(String oldLink : oldLinks){
            if(link.equals(oldLink)){
                returnValue = false;
            }
        }

        for(String newLink : newLinks){
            if(link.equals(newLink)){
                returnValue = false;
            }
        }
        return returnValue;
    }
}
