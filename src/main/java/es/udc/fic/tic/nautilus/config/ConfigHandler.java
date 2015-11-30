package es.udc.fic.tic.nautilus.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import es.udc.fic.tic.nautilus.util.ModelConstanst;

public class ConfigHandler {
	
	public NautilusConfig getConfig() {
		SAXBuilder builder = new SAXBuilder();
		File fileConfig = new File("config.xml");
		
		Boolean serverAvaliable = null;
		long limitSpace = 0;
		ModelConstanst.LANGUAGE language = null;
		List<ServerInfo> serverPreferences = new ArrayList<ServerInfo>();
		String homeFolder = null;
				
		try {
			// Generate Document
			Document configFile = (Document) builder.build(fileConfig);
			
			// Get the root element
			Element config = configFile.getRootElement();

			// Get the attributes
			serverAvaliable = Boolean.valueOf(config.getChildTextTrim("serverAvailable"));

			if (config.getChildTextTrim("limitSpace").equals("0")) {
				limitSpace = 0;
			} else {
				limitSpace = Long.parseLong(config.getChildTextTrim("limitSpace"));
			}
			
			if (config.getChildTextTrim("language").equals("EN")) {
				language = ModelConstanst.LANGUAGE.EN;
			} else if (config.getChildTextTrim("language").equals("ES")) {
				language = ModelConstanst.LANGUAGE.ES;
			} else {
				// Throws exception
			}
			

			Element servers = config.getChild("servers");
			@SuppressWarnings("rawtypes")
			List listServer = servers.getChildren();
			
			// Check if listServer has any server reference
			if (listServer.size() > 0) {
				for ( int i = 0; i < listServer.size(); i++ ) {
					Element serverElement = (Element)listServer.get(i);
					
					String name = serverElement.getChildText("name");
					String hash = serverElement.getChildText("hash");
					
					ServerInfo server = new ServerInfo(name, hash);
					serverPreferences.add(server);
				}
			}
			
			homeFolder = config.getChildTextTrim("StorageFolder");
			
		} catch ( IOException io ) {
	        System.out.println( io.getMessage() );
	    } catch ( JDOMException jdomex ) {
	        System.out.println( jdomex.getMessage() );
	    }
		
		NautilusConfig nautilusConfig = new NautilusConfig(serverAvaliable, 
				limitSpace, language, serverPreferences, homeFolder);
		
		return nautilusConfig;
	}
	
	public void restoreConfig() {
		// Delete old file
		File fileConfig = new File("config.xml");
		fileConfig.delete();
		// Generate a default config file
		NautilusConfig config = new NautilusConfig(false, 0, ModelConstanst.LANGUAGE.EN , null, getStorageFolder());
		writeXMLFile(config);
	}
	
	public void changeConfig(NautilusConfig config) {
		// Delete old file
		File fileConfig = new File("config.xml");
		fileConfig.delete();
		// Generate a new config file
		writeXMLFile(config);
	}
	
	public void initializeConfig() {
		// Default parameters
		NautilusConfig config = new NautilusConfig(false, 0, ModelConstanst.LANGUAGE.EN , null, getStorageFolder());
		// Create the file
		writeXMLFile(config);
	}
	
	/*********************/
	/* Private functions */
	/*********************/
	
	/**
	 * Function to get the storage folder
	 */
	private String getStorageFolder() {
		String homeFolder;
		if (System.getProperty("os.name").contains("win")) {
			// Windows system
			homeFolder = System.getProperty("user.home") + "\nautilus_storage";
		} else {
			// Unix system
			homeFolder = System.getProperty("user.home") + "/nautilus_storage";
		}
		return homeFolder;
	}
	
	/**
	 * Function to write the config file
	 */
	private void writeXMLFile(NautilusConfig configParemeters) {
		try {
			// Root element
			Element config = new Element("config");
			Document doc = new Document(config);
			doc.setRootElement(config);
			
			// Server available
			doc.getRootElement().addContent(new Element("serverAvailable").setText(
					String.valueOf(configParemeters.isServerAvailable())));
			
			// Limit Space
			if (configParemeters.isServerAvailable() && (configParemeters.getLimitSpace() != 0)) {
				doc.getRootElement().addContent(new Element("limitSpace").setText(
						String.valueOf(configParemeters.getLimitSpace())));
			} else {
				doc.getRootElement().addContent(new Element("limitSpace").setText("0"));;	
			}
			
			// Language
			doc.getRootElement().addContent(new Element("language").setText(configParemeters.getLanguage().toString()));
			
			// Servers preferences
			Element servers = new Element("servers");
			
			if (configParemeters.getServerPreferences() != null) {
				
				for (ServerInfo serverInfo : configParemeters.getServerPreferences()) {
					// Server Info
					Element server = new Element("server");
					server.addContent(new Element("name").setText(serverInfo.getName()));
					server.addContent(new Element("hash").setText(serverInfo.getHash()));
					
					servers.addContent(server);
				}
			}
			
			doc.getRootElement().addContent(servers);
			
			// Storage Folder
			doc.getRootElement().addContent(new Element("StorageFolder").setText(configParemeters.getStorageFolder()));
			
			// Save the dom in xml file
			XMLOutputter xmloutputter = new XMLOutputter();
			
			// Set pretty format to display nice
			xmloutputter.setFormat(Format.getPrettyFormat());
			xmloutputter.output(doc, new FileWriter("config.xml"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
