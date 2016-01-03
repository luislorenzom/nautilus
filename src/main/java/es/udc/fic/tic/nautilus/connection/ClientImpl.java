package es.udc.fic.tic.nautilus.connection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.DiscoverNetworks;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.config.ConfigHandler;

@Service("client")
public class ClientImpl implements Client {
	
	@Autowired
	private ConnectionUtilities connectionUtilities;
	
	public ConfigHandler configHandler = new ConfigHandler();
	public NautilusKeysHandler keysHandler = new NautilusKeysHandler();
	
	
	@Override
	public void saveFileInNetwork(String filePath, int downloadLimit,
			Calendar dateLimit, Calendar dateRelease) throws Exception {
		
		String path = new File(filePath).getParent();
		List<NautilusMessage> msgs = connectionUtilities.prepareFileToSend(filePath, 
				downloadLimit, dateLimit, dateRelease);
		
		
		List<NautilusKey> nautilusKey = keysHandler.getKeys(new File(filePath).getName()+"_key.xml");
		int index = 0;
		
		for (NautilusMessage msg : msgs) {
			String host = connectionUtilities.getHostAndBackupFromConfig().get(0);
			int result = startClient(host, msg);
			
			if (result == 1) {
				// Save the host in the key file
				nautilusKey.get(index).setHost(host);
				
				// Delete the split file 
				if (path != null) {
					new File(path + "/"+ nautilusKey.get(index).getFileName()).delete();
				} else {
					new File(nautilusKey.get(index).getFileName()).delete();
				}
			} else {
				//TODO: comprobar si se guardo bien, y si no probar con mas host de la config
				String backup = connectionUtilities.getHostAndBackupFromConfig().get(1);	
				startClient(backup, msg);
			}
			index++;
		}
		keysHandler.generateKeys(nautilusKey);
	}

	@Override
	public void getFileFromKey(String keyPath) throws Exception {
		
		List<NautilusKey> keys = keysHandler.getKeys(keyPath);
		List<File> filesJoin = new ArrayList<File>();
		
		for (NautilusKey key : keys) {
			NautilusMessage msg = new NautilusMessage(0, key.getHash());
			int result = startClient(key.getHost(), msg);
			
			if (result == 1) {
				File file = new File(key.getHash()+".aes256");
				File newFile = new File(key.getFileName());
				file.renameTo(newFile);
				
				file.delete();
				filesJoin.add(newFile);
			} else {
				// Make the petition to the backup host
				if (key.getHostBackup() != null) {
					int secondResult = startClient(key.getHostBackup(), msg);
					
					if (secondResult == 1) {
						File file = new File(key.getHash()+".aes256");
						File newFile = new File(key.getFileName());
						file.renameTo(newFile);
						
						file.delete();
						filesJoin.add(newFile);
					} else {
						System.out.println("Can't recovery the file");
						System.exit(0);
					}
				} else {
					//TODO: pulir los mensajes de error, indicar si es porque se acabo el limite
					// o lo que queda para que se libere el fichero
					System.out.println("Can't recovery the file");
					System.exit(0);
				}
			}
		}
		// Decrypt and join the file's part
		connectionUtilities.restoreFile(filesJoin, keys);
	}
	
	/**********************/
	/* Private functions */
	/*********************/
	
	private int startClient(String ipAddress, NautilusMessage msgObject) throws Exception {
		Random rnd = new Random(42L);
		Bindings b = new Bindings().listenAny();
		Peer client = new PeerBuilder(new Number160(rnd)).ports(4001).bindings(b).start();
		System.out.println("Client started and Listening to: " + DiscoverNetworks.discoverInterfaces(b));
		System.out.println("address visible to outside is " + client.peerAddress());

		InetAddress address = Inet4Address.getByName(ipAddress);
		int masterPort = 4000;
		PeerAddress pa = new PeerAddress(Number160.ZERO, address, masterPort, masterPort, masterPort +1 );

		System.out.println("PeerAddress: " + pa);
		
		// Future Discover
		FutureDiscover futureDiscover = client.discover().expectManualForwarding().inetAddress(address).ports(masterPort).start();
		futureDiscover.awaitUninterruptibly();

		// Future Bootstrap - slave
		FutureBootstrap futureBootstrap = client.bootstrap().inetAddress(address).ports(masterPort).start();
		futureBootstrap.awaitUninterruptibly();

		Collection<PeerAddress> addressList = client.peerBean().peerMap().all();
		System.out.println("=====> "+addressList.size());

		if (futureDiscover.isSuccess()) {
			System.out.println("found that my outside address is " + futureDiscover.peerAddress());
		} else {
			System.out.println("failed " + futureDiscover.failedReason());
		}
		
		PeerAddress peerA = addressList.iterator().next();
		
		byte[] msg = objectToByteArray(msgObject);
		
		
		if (msgObject.getType() == 1) {
			// Send and logic to process msg type one
			FutureDirect future = client.sendDirect(peerA).object(msg).start();
			
			future.awaitUninterruptibly();
			
			if (future.isSuccess()) {
				
				System.out.println("=====> receiving message");
				int val = (int) future.object();
				if (val == 1) {
					// Success!!
					System.out.println("=====> File part correctly sent");
					client.shutdown();
					return val;
				} else {
					// Fail in the server (can't save for space, permits, doesn't find, etc)
					System.out.println("=====> has been some error in the server");
					client.shutdown();
					return val;
				}
				
			} else {
				System.out.println(future.failedReason());
			}
		}
		
		if (msgObject.getType() == 0) {
			// Send and logic to process msg type zero
			FutureDirect future = client.sendDirect(peerA).object(msg).start();
			
			future.awaitUninterruptibly();
			
			if (future.isSuccess()) {
				System.out.println("=====> receiving message");
				try{
					byte[] byteArray = (byte[]) future.object();
					FileOutputStream fos = new FileOutputStream(msgObject.getHash()+".aes256");
					fos.write(byteArray);
					fos.close();
					
					System.out.println("=====> File part recovered");
					client.shutdown();
					return 1;
				} catch (Exception e) {
					System.out.println("=====> has been some error in the server");
					client.shutdown();
					return -1;
				}
				
			} else {
				System.out.println(future.failedReason());
			}
		}
		
		client.shutdown();
		return -1;
	}
	
	
	/**
	 * This function return the file bytes
	 * 
	 * @param File the file object which we want convert into byte array
	 * @return byte[] from the file
	 */
	@SuppressWarnings("unused")
	private static byte[] readContentIntoByteArray(File file) {
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
	 * This function convert one object, in this case a Message, into byte array
	 * 
	 * @param NautilusMessage object
	 * @return byte[] the byte array of the message
	 * @throws Exception
	 */
	private static byte[] objectToByteArray(NautilusMessage object) throws Exception {
		ByteArrayOutputStream bs= new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream (bs);
		os.writeObject(object);
		os.close();
		return bs.toByteArray();
	}
}