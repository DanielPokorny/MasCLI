/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mas.machine.Worker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileReader;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Načítá ze zadané commlinky jednotlivé URL a zapisuje do vystupní commlinky načtený obsah.
 * @author daniel
 */
public class URLReader extends Worker{
    /**
     * Obsahuje konfiguraci workeru načtenou z *.xml.
     */
    private URLReaderConfig config;

    /**
     * Vytvoří Worker.
     * @param iniFile soubor *.xml s konfigurací
     */
    public URLReader(File iniFile) throws Exception {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(iniFile));
        config = gson.fromJson(reader, URLReaderConfig.class);
        this.setDaemon(true);
    }

    /**
     * Vlastní běh Workeru.
     */
    @Override
    public void run() {
        System.out.println("URL reader hallo");
        while(true) {
            URL url;
            try {
                url = new URL((String) getMessage(config.input));
                Document doc;
                if(config.proxy == null) {
                    doc = Jsoup.connect((String) getMessage(config.input)).get();
                } else {
                    Authenticator.setDefault(authenticator);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.proxy.address, config.proxy.port));
                    HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
                    doc = Jsoup.connect((String) getMessage(config.input)).proxy(proxy).get();
                }

                String outputLine = "";

                if(config.element == null) {
                    outputLine = doc.text();
                } else {
                    Element element = doc.select(config.element).first();
                    if(element != null) {
                        outputLine = element.text();
                    }
                }
                
                DataSet message = new DataSet();
                message.setUrl(url.toString());
                message.setText(outputLine);

                sendMesage_(config.output, message);

            } catch (Exception ex) {
                Logger.getLogger(URLReader.class.getName()).log(Level.SEVERE, null, ex);
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
}
