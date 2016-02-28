package es.udc.fic.tic.nautilus.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import es.udc.fic.tic.nautilus.connection.NautilusMessage;

public class MessageSynchronizationBufferTest {
	
	@After
	public void cleanTestFiles() {
		new File("MessageSynchronizationBuffer").delete();
	}
	
	@Test
	public void saveAndRecoveryBufferTest() {
		/* Check if exist any message */
		assertFalse(MessageSynchronizationBuffer.anyMessage());
		
		/* Add one message and check is exist any message */
		NautilusMessage msg1 = new NautilusMessage(1, "asdasdasdasdas");
		MessageSynchronizationBuffer.addMessage(msg1);
		assertTrue(MessageSynchronizationBuffer.anyMessage());
		
		/* Save the buffer in a file and recovery */
		MessageSynchronizationBuffer.saveBufferInFile();
		assertTrue(new File("MessageSynchronizationBuffer").exists());
		
		/* Delete message in the buffer and recovery from file */
		MessageSynchronizationBuffer.deleteMessage();
		assertFalse(MessageSynchronizationBuffer.anyMessage());
		MessageSynchronizationBuffer.recoveryBufferFromFile();
		assertTrue(MessageSynchronizationBuffer.anyMessage());
		
		/* Persist the change and check */
		MessageSynchronizationBuffer.deleteMessage();
		MessageSynchronizationBuffer.saveBufferInFile();
		NautilusMessage msg2 = new NautilusMessage(1, "ldsdfsdfsdfs");
		MessageSynchronizationBuffer.addMessage(msg2);
		assertTrue(MessageSynchronizationBuffer.anyMessage());
		MessageSynchronizationBuffer.recoveryBufferFromFile();
		assertFalse(MessageSynchronizationBuffer.anyMessage());
	}
}
