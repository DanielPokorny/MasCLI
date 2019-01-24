/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mas.machine.jworkers;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import mas.machine.Worker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Odesílá nové URL extrahované z RSS feedu do výstupní linky.
 * @author daniel
 * @version 2.0
 */
public class RSSReader extends Worker{
    RSSReaderConfig config_;

    /**
     * List obsahující nově přidané odkazy.
     */
    private List<String> newLinks_ = new ArrayList<>();

    /**
     * List obsahující již navštívené odkazy.
     */
    private List<String> oldLinks_ = new ArrayList<>();
    
    /**
     * Vytvoří nový RSSReader
     * @param iniFile cesta k souboru s konfigurací
     * @throws JAXBException 
     */
    public RSSReader(File iniFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(iniFile);
        Node n = doc.getDocumentElement();


/*        JAXBContext jaxbContext = JAXBContext.newInstance(RSSReaderConfig.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        config_ = (RSSReaderConfig) jaxbUnmarshaller.unmarshal(iniFile);*/
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
                } catch (IllegalArgumentException | FeedException ex) {
                    Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
                }

                if(newLinks_.size() > 0){
                    nextRead = false;
                }else{
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } while(nextRead);

            String outLink = newLinks_.get(0);
            newLinks_.remove(0);
            oldLinks_.add(outLink);

            sendMesage_(config_.output, outLink);
        }
    }

    /**
     * Extrahuje odkazy z textu.
     * @throws IOException něco je špatně.
     */
    private void readLinks() throws IOException, IllegalArgumentException, FeedException{
        URL rssURL = null;
        try {
            rssURL = new URL(config_.url);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        URLConnection conn = null;

        if(config_.proxy != null) {
            Authenticator.setDefault(authenticator);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config_.proxy.address, config_.proxy.port));

            try {
                    conn = rssURL.openConnection(proxy);
            } catch (IOException ex) {
                    Logger.getLogger(RSSReader.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            conn = rssURL.openConnection();
        }

        SyndFeedInput input = new SyndFeedInput();
        try {
            SyndFeed feed = input.build(new XmlReader(conn));

            for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
                String link = entry.getLink();
                if(link != null) {
                    if(isNewLink(link)){
                        newLinks_.add(link);
                    }
                }
            }
        } catch (FeedException e) {
            e.printStackTrace();
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
            return (new PasswordAuthentication(config_.proxy.username, config_.proxy.password.toCharArray()));
        }
    };

    /**
     * Otestuje odkaz, zda je nový.
     * @param link odkaz.
     * @return true, pokud je odkaz nový.
     */
    private boolean isNewLink(String link){
        boolean returnValue = true;
        for(String oldLink : oldLinks_){
            if(link.equals(oldLink)){
                returnValue = false;
            }
        }

        for(String newLink : newLinks_){
            if(link.equals(newLink)){
                returnValue = false;
            }
        }
        return returnValue;
    }
}
