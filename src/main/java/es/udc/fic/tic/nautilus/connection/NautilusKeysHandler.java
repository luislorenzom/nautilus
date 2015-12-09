package es.udc.fic.tic.nautilus.connection;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import net.tomp2p.peers.Number160;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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
					String aesKeyText = keyElement.getChildText("AESKey");
					String hostText = keyElement.getChildText("host");
					String hostBackupText = keyElement.getChildText("hostBackup");
					
					Number160 host = new Number160(hostText);
					Number160 hostBackup = new Number160(hostBackupText);
					SecretKey key = stringToSecretKey(aesKeyText);
					
					NautilusKey nkey = new NautilusKey(fileName, key, host, hostBackup);
					
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
					
					if (keyItem.getKey() != null) {
						key.addContent(new Element("AESKey").setText(secretKeyToString(keyItem.getKey())));
					}
					
					if (keyItem.getHost() != null) {
						key.addContent(new Element("host").setText(keyItem.getHost().toString()));
					}
					
					if (keyItem.getHostBackup() != null) {
						key.addContent(new Element("hostBackup").setText(keyItem.getHostBackup().toString()));
					}
					keys.addContent(key);
				}
			}
			
			doc.getRootElement().addContent(keys);
			
			// Save the dom in xml file
			XMLOutputter xmloutputter = new XMLOutputter();
			
			// Set pretty format to display nice
			xmloutputter.setFormat(Format.getPrettyFormat());
			xmloutputter.output(doc, new FileWriter("key.xml"));
			
		} catch (Exception e) {
			System.err.println("Can't generate a XML key");
		}
	}
	
	/*********************/
	/* Private functions */
	/*********************/
	
	private String secretKeyToString (SecretKey secretKey) {
		byte[] bytes = secretKey.getEncoded();
		String stringKey = Arrays.toString(bytes);
		return stringKey;
	}
	
	private SecretKey stringToSecretKey (String stringKey) {
		String[] byteValues = stringKey.substring(1, stringKey.length() - 1).split(",");
		byte[] bytes = new byte[byteValues.length];
		
		for (int i=0, len=bytes.length; i<len; i++) {
			bytes[i] = Byte.parseByte(byteValues[i].trim());
		}
		
		// convert to string
		/* String str = new String(bytes); */
		
		SecretKey key = new SecretKeySpec(bytes, 0, 32, "AES");
		
		return key;
	}
}
