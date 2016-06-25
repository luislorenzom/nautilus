package es.udc.fic.tic.nautilus.connection;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.ChannelCreator;
import net.tomp2p.connection.DefaultConnectionConfiguration;
import net.tomp2p.connection.DiscoverNetworks;
import net.tomp2p.futures.FutureChannelCreator;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
import es.udc.fic.tic.nautilus.view.MainView;

@Service("server")
public class ServerImpl implements Server {
	
	@Autowired
	private ConnectionUtilities connectionUtilities;
	
	public ConfigHandler configHandler = new ConfigHandler();

	public void startServer() throws Exception {
		
		if (configHandler.getConfig().isServerAvailable()) {
			MainView.jTextArea2.append("Starting nautilus server...\n");
			Random rnd = new Random(43L);
			Bindings b = new Bindings().listenAny();
			Peer master = new PeerBuilder(new Number160(rnd)).ports(4000).bindings(b).start();		
			MainView.jTextArea2.append("Server started Listening to: " + DiscoverNetworks.discoverInterfaces(b)+"\n");
			MainView.jTextArea2.append("address visible to outside is " + master.peerAddress()+"\n");
			
			// Time now in milliseconds
			long t = new Date().getTime();
			
			/*while (true) {
				// check if the button cancel server has been pushed
				if (MainView.serverThread == 1) {
					Set<Thread> threads = Thread.getAllStackTraces().keySet();
					
					for (Thread thread : threads) {
						if (thread.getName().equals("Thread-0")) {
							thread.stop();
						}
						
						if (thread.getName().equals("pool-1-thread-1")) {
							thread.stop();
						}
						
						if (thread.getName().equals("NETTY-TOMP2P - boss - -3-1")) {
							thread.stop();
						}
						
						if (thread.getName().equals("NETTY-TOMP2P - worker-client/server - -1-1")) {
							thread.stop();
						}
						
						if (thread.getName().equals("Thread-0")) {
							thread.stop();
						}
					}
				}*/
				
				long now = new Date().getTime();
				if (now - t >= 600000) {
					// if elapses 10 minutes then check the expired files
					connectionUtilities.checkAndDeleteExpiredFile();
					// reset reference time
					t = new Date().getTime();
				}			
				for (PeerAddress pa : master.peerBean().peerMap().all()) {
					MainView.jTextArea2.append("PeerAddress: " + pa + "\n");
					FutureChannelCreator fcc = master.connectionBean().reservation().create(1, 1);
					fcc.awaitUninterruptibly();
	
					ChannelCreator cc = fcc.channelCreator();
	
					FutureResponse fr1 = master.pingRPC().pingTCP(pa, cc, new DefaultConnectionConfiguration());
					fr1.awaitUninterruptibly();
	
					if (fr1.isSuccess()) {
						MainView.jTextArea2.append("peer online T:" + pa + "\n");
					} else {
						MainView.jTextArea2.append("offline " + pa + "\n");
					}
	
					FutureResponse fr2 = master.pingRPC().pingUDP(pa, cc, new DefaultConnectionConfiguration());
					fr2.awaitUninterruptibly();
	
					cc.shutdown();
	
					if (fr2.isSuccess()) {
						MainView.jTextArea2.append("peer online U:" + pa + "\n");
					} else {
						System.out.println("offline " + pa);
						MainView.jTextArea2.append("\n");
					}
					
				}
				Thread.sleep(1500);
				master.objectDataReply(new ObjectDataReply() {
					
					@Override
					public Object reply(PeerAddress sender, Object request) throws Exception {
						MainView.jTextArea2.append("\n===== Incoming request\n");
						
						byte[] msg_bytes = (byte[]) request;
						NautilusMessage msg = byteArrayToObject(msg_bytes);
							
						switch (msg.getType()) {
						
						case 0:
							NautilusMessage response = connectionUtilities.processMessageTypeZero(msg);
							if (response == null) {
								return -1;
							}
							return response;
						
						case 1:
							return connectionUtilities.processMessageTypeOne(msg);
						
						case 2:
							return connectionUtilities.synchronizeFile(msg);
						
						case 3:
							/* get the string calendars and generate old style message */
							DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
							Calendar releaseDate = null;
							Calendar dateLimit = null;
							
							if (msg.getDateLimitString() != null) {
								dateLimit = Calendar.getInstance();
								/* Si no se pone este try-catch no hace 
								 * bien la conversion a calendar; REVISARLO */
								try{
									dateLimit.setTime(df.parse(msg.getReleaseDateString()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							if (msg.getReleaseDateString() != null) {
								releaseDate = Calendar.getInstance();
								try {
								releaseDate.setTime(df.parse(msg.getDateLimitString()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							NautilusMessage msg_old = new NautilusMessage(1, msg.getHash(), msg.getContent(), 
									msg.getDownloadLimit(), releaseDate, dateLimit);
							
							return connectionUtilities.processMessageTypeOne(msg_old);
							
						default:
							return -1;
						}
					}
				});
			} else {
				MainView.jTextArea2.append("Allow the server's functions in config panel\n");
		        MainView.jButton10.setEnabled(true);
		        MainView.jButton11.setEnabled(false);
		        MainView.jButton12.setEnabled(false);
			}
		}
	
	
	
	/*********************/
	/* Private functions */
	/*********************/
	
	/**
	 * This function convert the byte[] received into a object
	 * 
	 * @param byte[] the message bytes
	 * @return NautilusMessage the message object
	 * @throws Exception
	 */
	private NautilusMessage byteArrayToObject(byte[] bytes) throws Exception {
		ByteArrayInputStream bs= new ByteArrayInputStream(bytes);
		ObjectInputStream is = new ObjectInputStream(bs);
		NautilusMessage message = (NautilusMessage)is.readObject();
		is.close();
		return message;
	}
}
