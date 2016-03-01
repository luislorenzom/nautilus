package es.udc.fic.tic.nautilus.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import es.udc.fic.tic.nautilus.connection.NautilusMessage;

public final class MessageSynchronizationBuffer {
	
	private static List<BufferElement> messageBuffer = new ArrayList<BufferElement>();

	/**
	 * This method serialize the list that have all the message to
	 * synchronize the different servers, which have the same data.
	 */
	public static void saveBufferInFile() {
		try {
			FileOutputStream fos = new FileOutputStream("MessageSynchronizationBuffer");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(messageBuffer);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method recovery the list from file.
	 */
	@SuppressWarnings("unchecked")
	public static void recoveryBufferFromFile() {
		try {
			FileInputStream fis = new FileInputStream("MessageSynchronizationBuffer");
			ObjectInputStream ois = new ObjectInputStream(fis);
			messageBuffer = (List<BufferElement>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check if exist any message pending to send in the buffer
	 * 
	 * @return boolean 
	 */
	public static boolean anyMessage() {
		return (messageBuffer.size() != 0);
	}
	
	/**
	 * This method add to list the one message has not been received
	 * 
	 * @param NautilusMessage
	 */
	public static void addMessage(NautilusMessage msg, String ipAddress) {
		messageBuffer.add(new BufferElement(msg, ipAddress));
	}
	
	/**
	 * This function recovery the oldest message saved
	 * 
	 * @return NautilusMessage
	 */
	public static BufferElement getMessage() {
		return messageBuffer.get(0);
	}
	
	/**
	 * This method delete the oldest message saved
	 */
	public static void deleteMessage() {
		messageBuffer.remove(0);
	}
	
	/**
	 * This function give you the number of messages 
	 * pending to synchronize
	 * 
	 * @return int Number of size
	 */
	public static int getSize() {
		return messageBuffer.size();
	}
}
