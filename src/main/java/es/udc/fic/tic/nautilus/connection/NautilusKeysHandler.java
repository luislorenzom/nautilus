package es.udc.fic.tic.nautilus.connection;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;

public class NautilusKeysHandler {
	
	/**
	 * Convert the XML key to List of NautilusKey
	 * 
	 * @param String keyPath
	 * @return List of NautilusKey
	 */
	public List<NautilusKey> getKeys(String keyPath) {
		SAXBuilder builder = new SAXBuilder();
		File keyFile = new File(keyPath);
		List<NautilusKey> keysList = new ArrayList<NautilusKey>();
		
		try {
			// Generate Document
			Document file = (Document) builder.build(keyFile);
			
			// Get the root element
			Element nautilusKey = file.getRootElement();
			
			// Get the keys element
			Element keys = nautilusKey.getChild("keys");
			
			// Check the keys
			@SuppressWarnings("rawtypes")
			List keyListXml = keys.getChildren();
			
			if (keyListXml.size() > 0) {
				for ( int i = 0; i < keyListXml.size(); i++ ) {
					Element keyElement = (Element)keyListXml.get(i);
					
					String fileName = keyElement.getChildText("fileName");
					
					String key = null;
					ENCRYPT_ALG algorithm;
					if (keyElement.getChildText("AESKey") == null) {
						key = keyElement.getChildText("RSAKey");
						algorithm = ENCRYPT_ALG.RSA;
					} else {
						key = keyElement.getChildText("AESKey");
						algorithm = ENCRYPT_ALG.AES;
					}
					
					String hash = keyElement.getChildText("hash");
					String host = keyElement.getChildText("host");
					String hostBackup = keyElement.getChildText("hostBackup");
					
					NautilusKey nkey = new NautilusKey(fileName, key, algorithm, hash, host, hostBackup);
					
					keysList.add(nkey);
				}
			}
		} catch (Exception e) {
			System.err.print("Can't recovery the keys from the XML file");
		}
		
		return keysList;
	}
	
	/**
	 * Generate a XML which has all of NautilusKey
	 * 
	 * @param List of NautilusKey
	 */
	public void generateKeys(List<NautilusKey> keysList) {
		try {
			// Root Element
			Element nautilusKey = new Element("nautilusKey");
			Document doc = new Document(nautilusKey);
			doc.setRootElement(nautilusKey);
			
			// Keys Element
			Element keys = new Element("keys");
			
			if (keysList.size() > 0) {
				for (NautilusKey keyItem : keysList) {
					Element key = new Element("key");
					
					if (keyItem.getFileName() != null) {
						key.addContent(new Element("fileName").setText(keyItem.getFileName()));
					}
					
					if ((keyItem.getKey() != null) && (keyItem.getEncryptAlg() == ENCRYPT_ALG.AES)) {
						key.addContent(new Element("AESKey").setText(keyItem.getKey()));
					}
					
					if ((keyItem.getKey() != null) && (keyItem.getEncryptAlg() == ENCRYPT_ALG.RSA)) {
						key.addContent(new Element("RSAKey").setText(keyItem.getKey()));
					}
					
					if (keyItem.getHash() != null) {
						key.addContent(new Element("hash").setText(keyItem.getHash()));
					}
					
					if (keyItem.getHost() != null) {
						key.addContent(new Element("host").setText(keyItem.getHost()));
					}
					
					if (keyItem.getHostBackup() != null) {
						key.addContent(new Element("hostBackup").setText(keyItem.getHostBackup()));
					}
					keys.addContent(key);
				}
			}
			
			doc.getRootElement().addContent(keys);
			
			// Save the dom in xml file
			XMLOutputter xmloutputter = new XMLOutputter();
			
			// Generate a name for the key
			int finalLenght = keysList.get(0).getFileName().length() - 9;
			String keyName = keysList.get(0).getFileName().substring(0, finalLenght);
			
			// Set pretty format to display nice
			xmloutputter.setFormat(Format.getPrettyFormat());
			xmloutputter.output(doc, new FileWriter(keyName+"_key.xml"));
			
		} catch (Exception e) {
			System.err.println("Can't generate a XML key");
		}
	}
}
