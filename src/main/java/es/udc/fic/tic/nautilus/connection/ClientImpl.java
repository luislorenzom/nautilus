package es.udc.fic.tic.nautilus.connection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.tomp2p.connection.Bindings;
//import net.tomp2p.connection.DiscoverNetworks;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.util.RSAManager;

@Service("client")
public class ClientImpl implements Client {
	
	@Autowired
	private ConnectionUtilities connectionUtilities;
	
	private NautilusKeysHandler keysHandler = new NautilusKeysHandler();
	private RSAManager manager = new RSAManager();
	
	@Override
	public void saveFileInNetwork(String filePath, int downloadLimit,
			Calendar dateLimit, Calendar dateRelease, String pKeyPath) throws Exception {
		
		// Initialize and generate public key only if doesn't exist 
		PublicKey pkey = null;
		if (pKeyPath != null) {
			try {
				pkey = manager.getPublicKeyFromFile(pKeyPath);
			} catch (Exception e) {
				System.err.println("Can't recovery the public key, check the argument");
				System.exit(0);
			}
		}
		
		String path = new File(filePath).getParent();
		List<NautilusMessage> msgs = connectionUtilities.prepareFileToSend(filePath, 
				downloadLimit, dateLimit, dateRelease, pkey);
		
		
		List<NautilusKey> nautilusKey = keysHandler.getKeys(new File(filePath).getName()+"_key.xml");
		int index = 0;
		
		for (NautilusMessage msg : msgs) {
			boolean clean = true;
			List<String> serverPreferences = connectionUtilities.getHostAndBackupFromConfig();
			for (String host : serverPreferences) {
				//String host = serverPreferences.get(0);
				System.out.println("Save the file");
				
				// Make three intends
				int result = 0;
				for (int j = 0; j < 3; j++) {
					result = startClient(host, msg);
					
					if (result == 1) {
						break;
					}
					//TODO: añadir un wait en cada uno de los 
					//intentos para dar un margen de tiempo?
				}
				
				if (result == 1) {
					clean = false;
					System.out.println("ok!");
					// Save the host in the key file
					nautilusKey.get(index).setHost(host);
					
					// Delete the split file 
					if (path != null) {
						new File(path + "/"+ nautilusKey.get(index).getFileName()).delete();
					} else {
						new File(nautilusKey.get(index).getFileName()).delete();
					}
					
					// Check if exist one mirror (another server). if exist then copy the file
					if (serverPreferences.size() > 1) {
						int tmpIndex = 0;
						for (String hostBackup : serverPreferences) {
							if (hostBackup != host) {
								System.out.println("Mirroring the file");
								
								// Make three intends
								int resultMirroring = 0;
								for (int j = 0; j < 3; j++) {
									resultMirroring = startClient(hostBackup, msg);
									
									if (resultMirroring == 1) {
										break;
									}
								}
								
								if (resultMirroring == 1) {
									// Success, now save the hostBackup in the keyFile
									System.out.println("ok!");
									nautilusKey.get(index).setHostBackup(hostBackup);
									break;
								}
								
								tmpIndex++;
								
								if (tmpIndex == serverPreferences.size()) {
									break;
								}
								
							}
						}
					}
					break;
				} 
			}
			
			if (clean) {
				// if can't sent an file split then delete the 
				// key and the encrypt files
				System.out.println("Can't sent one file split. Cleaning the tmp files");
				cleanAllTmpFile(filePath);
				System.exit(0);
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
			
			// Make three intends
			int result = 0;
			for (int j = 0; j < 3; j++) {
				result = startClient(key.getHost(), msg);
				
				if (result == 1) {
					break;
				}
			}
			
			if (result == 1) {
				File file = new File(key.getHash()+".aes256");
				File newFile = new File(key.getFileName());
				file.renameTo(newFile);
				
				file.delete();
				filesJoin.add(newFile);
			} else {
				// Make the petition to the backup host
				if (key.getHostBackup() != null) {
					
					// Make three intends
					int secondResult = 0;
					for (int j = 0; j < 3; j++) {
						secondResult = startClient(key.getHostBackup(), msg);
						
						if (secondResult == 1) {
							break;
						}
					}
					
					if (secondResult == 1) {
						File file = new File(key.getHash()+".aes256");
						File newFile = new File(key.getFileName());
						file.renameTo(newFile);
						
						file.delete();
						filesJoin.add(newFile);
					} else {
						System.out.println("Can't recovery the file");
						deleteFilesToJoin(filesJoin);
						System.exit(0);
					}
				} else {
					System.out.println("Can't recovery the file");
					deleteFilesToJoin(filesJoin);
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
		try {
			System.out.println("Sending... "+msgObject.getHash()+"--->"+ipAddress);
			Random rnd = new Random(42L);
			Bindings b = new Bindings().listenAny();
			Peer client = new PeerBuilder(new Number160(rnd)).ports(4001).bindings(b).start();
			//System.out.println("Client started and Listening to: " + DiscoverNetworks.discoverInterfaces(b));
			//System.out.println("address visible to outside is " + client.peerAddress());
	
			InetAddress address = Inet4Address.getByName(ipAddress);
			int masterPort = 4000;
			//PeerAddress pa = new PeerAddress(Number160.ZERO, address, masterPort, masterPort, masterPort +1 );
	
			//System.out.println("PeerAddress: " + pa);
			
			// Future Discover
			FutureDiscover futureDiscover = client.discover().expectManualForwarding().inetAddress(address).ports(masterPort).start();
			futureDiscover.awaitUninterruptibly();
	
			// Future Bootstrap - slave
			FutureBootstrap futureBootstrap = client.bootstrap().inetAddress(address).ports(masterPort).start();
			futureBootstrap.awaitUninterruptibly();
	
			Collection<PeerAddress> addressList = client.peerBean().peerMap().all();
			System.out.println("=====> "+addressList.size());
			
			// if can not connect with the server
			if (addressList.size() == 0) {
				client.shutdown();
				return -1;
			}
			
			// patch for the fake ip
			if (!(addressList.iterator().next().inetAddress().getHostAddress().equals(ipAddress))) {
				//System.out.println("here!!");
				client.shutdown();
				return -1;
			}
			
			
			if (futureDiscover.isSuccess()) {
				System.out.println("found that my outside address is " + futureDiscover.peerAddress());
			} else {
				// Send the file without problem!
				System.out.println("failed 1 " + futureDiscover.failedReason());
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
						System.out.println("=====> Reason: " + printError(val));
						return -1;
					}
					
				} else {
					System.out.println("failed 2 " + future.failedReason());
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
					System.out.println("failed 3 " + future.failedReason());
				}
			}
			
			client.shutdown();
			return -1;
		} catch (Exception e) {
			System.out.println("Can't find the host");
			return -1;
		}
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
	
	/**
	 * This procedure delete the key file and the encrypt files which can't be delete
	 * 
	 * @param String filePath
	 */
	private void cleanAllTmpFile(String filePath) {
		List<NautilusKey> nautilusKey = keysHandler.getKeys(new File(filePath).getName()+"_key.xml");
		
		// Delete the keyFile
		String fileKey = (new File(filePath).getName()) + "_key.xml";
		new File(fileKey).delete();
		
		// Delete the encrypt files
		String parentPath = new File(filePath).getParent();
		
		for (NautilusKey key : nautilusKey) {
			new File(parentPath + "/" +key.getFileName()).delete();
		}
	}
	
	/**
	 * This function clean all the tmp files if you can't recovery all of the splits
	 * 
	 * @param ArrayList<File> filesToJoin
	 */
	private void deleteFilesToJoin(List<File> filesJoin) {
		for (File file : filesJoin) {
			file.delete();
		}
	}
	
	/**
	 * This function get the server error message and print the error reason
	 * 
	 * @param int val
	 * @return String failure reason
	 */
	private String printError(int val) {
		switch (val) {
		case -1:
			return "Has been happened some error in the save process\n";
		
		case -2:
			return "The server is full\n";
		
		case -3:
			return "The server isn't avaliable in the file configuration\n";

		default:
			return "Internal Error\n";
		}
		
	}
}