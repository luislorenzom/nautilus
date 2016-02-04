package es.udc.fic.tic.nautilus.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.client.ClientService;
import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.expcetion.HashGenerationException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.server.ServerService;
import es.udc.fic.tic.nautilus.util.ModelConstanst;
import es.udc.fic.tic.nautilus.util.ModelConstanst.ENCRYPT_ALG;

@Service("connectionUtilities")
public class ConnectionUtilitiesImpl implements ConnectionUtilities {
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private ClientService clientService;
	
	ConfigHandler configHandler = new ConfigHandler();
	NautilusKeysHandler keysHandler = new NautilusKeysHandler();
	
	@Override
	public byte[] processMessageTypeZero(NautilusMessage msg) {
		if (msg != null) {
			try {
				FileInfo fileInfo = serverService.returnFile(msg.getHash());
				File file = new File(fileInfo.getPath());
				//----
				// checked if the file is corrupted
				if (!(msg.getHash().equals(getHashFromFile(file, "SHA-256")))) {
					//the split file is corrupted
					return null;
				}
				//---
				return readContentIntoByteArray(file);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public int processMessageTypeOne(NautilusMessage msg) {
		/* Check if the message is not null and this node is a server */
		if ((msg != null) && (configHandler.getConfig().isServerAvailable())) {
			long byteSize = msg.getContent().length;
			/* Check if can save the file in the node */
			if (serverService.checkFileSize(byteSize)) {
				String filePath = configHandler.getConfig().getStorageFolder()+"/"+msg.getHash()+".aes256";
				try {
					FileOutputStream fos = new FileOutputStream(filePath);
					fos.write(msg.getContent());
					fos.close();
					
					FileInfo fileInfo = serverService.keepTheFile(filePath, msg.getDownloadLimit(), msg.getReleaseDate(), 
							msg.getDateLimit(), byteSize, msg.getHash());
					
					if (fileInfo != null) {
						return 1;
					} else {
						return -1;
					}
					
				} catch (Exception e){
					return -1;
				}
			}
			return -1;
		}
		return -1;
	}

	@Override
	public List<NautilusMessage> getMessagesFromKey(String hash) {
		return null;
	}

	@Override
	public List<NautilusMessage> prepareFileToSend(String filePath,
			int downloadLimit, Calendar dateLimit, Calendar releaseDate) {
		List<NautilusMessage> msgs = new ArrayList<NautilusMessage>();
		try {
			String pathForList = ".";
			/* Split the file */
			 clientService.fileSplit(filePath);
			 List<File> splitFiles = new ArrayList<File>();

			 if (new File(filePath).getParent() != null) {
				 pathForList = new File(filePath).getParent();
			 }
			 
			File[] files = new File(pathForList).listFiles();
			 /* Get the file's chunks */
			 for (File fileEntry : files) {
				 if (fileEntry.getName().contains(getNameAboutPath(filePath)) &&
					(fileEntry.getName()).substring(fileEntry.getName().length() - 1).matches("[0-9]+") &&
					! fileEntry.getPath().equals(filePath)) {
					 splitFiles.add(fileEntry);
				 }
			 }
			 
			 /* Prepare the list that before we turned to a XML key */
			 List<NautilusKey> keysList = new ArrayList<NautilusKey>();
			 
			 /* Now encrypt the files and generate a key */
			 for (File fileEntry : splitFiles) {
				 
				 // Encrypt
				 SecretKey key = clientService.encryptFile(fileEntry.getPath(), ENCRYPT_ALG.AES);
				 
				 String EncryptfileName = fileEntry.getName()+".aes256";
				 
				 // Delete the plain file
				 fileEntry.delete();
				 
				 File encryptFile = new File(fileEntry.getPath()+".aes256");
				 // Generate the file hash
				 String hash = getHashFromFile(encryptFile, "SHA-256");
				 
				 // We generate a key and adding to list, after when send the file will save the host
				 NautilusKey nKey = new NautilusKey(EncryptfileName, key, hash, null, null);
				 keysList.add(nKey);
				 
				 // Generate a message
				 NautilusMessage msg = new NautilusMessage(1, hash, readContentIntoByteArray(encryptFile), 
						 downloadLimit, dateLimit, releaseDate);
				 
				 // Add the new message to the messageList
				 msgs.add(msg);
			 }
			 
			 // Write the key into xml
			 keysHandler.generateKeys(keysList);
			 			 
		} catch (Exception e) {
			//return null;
			e.printStackTrace();
		}
		return msgs;
	}
	
	
	public List<String> getHostAndBackupFromConfig() {
		List<String> preferences = configHandler.getConfig().getServerPreferences();
		long seed = System.nanoTime();
		
		Collections.shuffle(preferences, new Random(seed));
		return preferences;
	}
	
	
	@Override
	public void restoreFile(List<File> files, List<NautilusKey> keys) throws Exception {
		try {
			int index = 0;
			List<File> deleteFiles = new ArrayList<File>();
			// Decrypt
			for (File file : files) {
				clientService.decrypt(keys.get(index).getKey(), file.getPath(), ModelConstanst.ENCRYPT_ALG.AES);
				index++;
				
				// delete encrypt file
				file.delete();
				
				int lenghtDeleteFiles = file.getName().length() - 7;
				deleteFiles.add(new File("dec_"+file.getName().substring(0, lenghtDeleteFiles)));
			}
			
			// Get the baseName for make the join operation
			String[] baseNameArray = keys.get(0).getFileName().split("\\.");
			String baseName = "";
			index = 0;
			for (String baseNameFrag : baseNameArray) {
				if (index == (baseNameArray.length - 2)) {
					break;
				}
				
				if (index == (baseNameArray.length - 3)) {
					baseName += baseNameFrag;
				} else {
					baseName += baseNameFrag + ".";
				}
				
				index++;
			}
			
			// Join
			clientService.fileJoin("dec_"+baseName);
			
			for (File fileToDelete : deleteFiles) {
				fileToDelete.delete();
			}
		} catch (Exception e) {
			//TODO: añadir otro catch para cuando la clave sea incorreta
			System.err.println("The file is corrupt");
			//e.printStackTrace();
		}
	}
	
	/*********************/
	/* private functions */
	/*********************/
	
	/**
	 * Function to obtain the hash of the file
	 * 
	 * @param File file
	 * @param String algorithm
	 * @return String The hash string from the file
	 * @throws HashGenerationException
	 */
	private String getHashFromFile(File file, String algorithm) throws HashGenerationException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			// algorithm can be "MD5", "SHA-1", "SHA-256"
	        MessageDigest digest = MessageDigest.getInstance(algorithm); 
	 
	        byte[] bytesBuffer = new byte[1024];
	        int bytesRead = -1;
	 
	        while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
	            digest.update(bytesBuffer, 0, bytesRead);
	        }
	 
	        byte[] hashedBytes = digest.digest();
	 
	        return convertByteArrayToHexString(hashedBytes);
	    } catch (NoSuchAlgorithmException | IOException ex) {
	        throw new HashGenerationException(
	                "Could not generate hash from file", ex);
	    }
	}
	
	/**
	 * Function to convert byte array to hexadecimal string
	 * 
	 * @param Byte array which want to convert
	 * @return String The string from byte array
	 */
	private static String convertByteArrayToHexString(byte[] arrayBytes) {
	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < arrayBytes.length; i++) {
	        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
	                .substring(1));
	    }
	    return stringBuffer.toString();
	}
	
	/**
	 * This function get the byteArray from a file
	 * 
	 * @param File file
	 * @return byte[] from file
	 */
	private byte[] readContentIntoByteArray(File file) {
	      FileInputStream fileInputStream = null;
	      byte[] bFile = new byte[(int) file.length()];
	      try {
	         //convert file into array of bytes
	         fileInputStream = new FileInputStream(file);
	         fileInputStream.read(bFile);
	         fileInputStream.close();
	      }
	      catch (Exception e) {
	         e.printStackTrace();
	      }
	      return bFile;
	}
	
	/**
	 * Get the file name from the path
	 * 
	 * @param String path file
	 * @return String name of the file
	 */
	private String getNameAboutPath(String path) {
		String[] tmp = path.split("/");
		return tmp[tmp.length - 1];
	}
	
	/**
	 * Get the extesion from the file name
	 * 
	 * @param String fileName
	 * @return String extension
	 */
	@SuppressWarnings("unused")
	private String getExtensionFromFileName(String fileName) {
		String[] chunks = fileName.split(".");
		String extension = "";
		for (String chunk : chunks) {
			extension = chunk;
		}
		return extension;
	}
}
