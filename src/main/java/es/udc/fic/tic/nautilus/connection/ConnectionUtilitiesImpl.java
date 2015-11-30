package es.udc.fic.tic.nautilus.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.client.ClientService;
import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.expcetion.HashGenerationException;
import es.udc.fic.tic.nautilus.model.FileInfo;
import es.udc.fic.tic.nautilus.server.ServerService;

@Service("connectionUtilities")
public class ConnectionUtilitiesImpl implements ConnectionUtilities {
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private ClientService clientService;

	ConfigHandler configHandler = new ConfigHandler();
	
	@Override
	public byte[] processMessageTypeZero(NautilusMessage msg) {
		if (msg != null) {
			try {
				FileInfo fileInfo = serverService.returnFile(msg.getHash());
				File file = new File(fileInfo.getPath());
				return readContentIntoByteArray(file);
			} catch(Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public int processMessageTypeOne(NautilusMessage msg) {
		/* Check if the message is not null and this node is a server */
		if ((msg != null) && (configHandler.getConfig().isServerAvailable())) {
			int byteSize = msg.getContent().length;
			/* Check if can save the file in the node */
			if (CheckFileSize(byteSize)) {
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
	public NautilusMessage packagingMessageTypeZero(String hash) {
		// TODO comprobar que el hash no es nulo y marchearlo con 
		// una expresion regular del SHA-256
		return null;
	}

	@Override
	public NautilusMessage packagingMessageTypeOne(String filePath,
			int downloadLimit, Calendar dateLimit, Calendar releaseDate) {
		// TODO haciendo uso del servicio del cliente encriptar el fichero y comprobar
		// los campos para crear el mensaje y devolverlo
		return null;
	}
	
	/* TODO hacer una funcion que a partir de un fichero haga un split de este e invoque
	 a la funcion de empaquetar el mensaje de tipo uno por cada split que haga el fichero
	 en un bucle */
	
	
	/* private functions */
	
	/**
	 * Function to obtain the hash of the file
	 * 
	 * @param File file
	 * @param String algorithm
	 * @return The hash string from the file
	 * @throws HashGenerationException
	 */
	@SuppressWarnings("unused")
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
	 * @return The string from byte array
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
	 * This function evaluate if the file can be save in the peer in
	 * relation with configuration file
	 * 
	 * @param float fileSize
	 * @return boolean that represents if the peer can save the file or not
	 */
	private boolean CheckFileSize(int fileSize) {
		long limit = configHandler.getConfig().getLimitSpace();
		long folderSize = folderSize(new File(configHandler.getConfig().getStorageFolder()));
		
		if (folderSize + fileSize > limit) {
			return false;
		}
		return true;
	}
	
	/**
	 * This function calculate the size of directory
	 */
	private long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
	
	/* this function get the byteArray from a file */
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
	
	/* TODO hacer un funcion que en base a la lista de preferencias de servidor buscar 2
	 * uno para subir el fichero y el otro a modo de mirror */
	
	/* TODO hacer un funcion para generar el archivo key, que contenga las claves de los
	 * archivos y los peers donde se encuentran estos archivos, por ahora hacerlo como 
	 * una clase serializada que sea un lista de tuplas (Server,frase de paso) */
}
