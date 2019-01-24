/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import mas.machine.Worker;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
     * @throws JAXBException
     */
    public URLReader(File iniFile) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(URLReaderConfig.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        config = (URLReaderConfig) jaxbUnmarshaller.unmarshal(iniFile);
        this.setDaemon(true);
    }

    /**
     * Vlastní běh Workeru.
     */
    @Override
    public void run() {
        while(true) {
            URL url;
            try {
                url = new URL((String) getMessage_(config.input));
                Document doc;
                if(config.proxy == null) {
                    HttpURLConnection uc = (HttpURLConnection)url.openConnection();
                    uc.connect();

                    String line = null;
                    StringBuffer tmp = new StringBuffer();
                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    while ((line = in.readLine()) != null) {
                        tmp.append(line);
                    }

                    doc = Jsoup.parse(String.valueOf(tmp));
                    uc.disconnect();

/*                    doc = Jsoup
                        .connect(url)
                        .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                        .header("Content-Language", "en-US")
                        .get();*/
                } else {
                    Authenticator.setDefault(authenticator);
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.proxy.address, config.proxy.port));
                    HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
                    uc.connect();

                    String line = null;
                    StringBuffer tmp = new StringBuffer();
                    BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    while ((line = in.readLine()) != null) {
                        tmp.append(line);
                    }

                    doc = Jsoup.parse(String.valueOf(tmp));
                    uc.disconnect();

                    /*doc = Jsoup
                        .connect(url)
                        . proxy(proxy) // sets a HTTP proxy
                        .userAgent("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                        .header("Content-Language", "en-US")
                        .get();*/

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
