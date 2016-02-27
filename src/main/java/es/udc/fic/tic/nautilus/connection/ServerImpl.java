package es.udc.fic.tic.nautilus.connection;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.udc.fic.tic.nautilus.config.ConfigHandler;
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

@Service("server")
public class ServerImpl implements Server {
	
	@Autowired
	private ConnectionUtilities connectionUtilities;
	
	public ConfigHandler configHandler = new ConfigHandler();
	
	public void startServer() throws Exception {
		Random rnd = new Random(43L);
		Bindings b = new Bindings().listenAny();
		Peer master = new PeerBuilder(new Number160(rnd)).ports(4000).bindings(b).start();
		System.out.println("Server started Listening to: " + DiscoverNetworks.discoverInterfaces(b));
		System.out.println("address visible to outside is " + master.peerAddress());
		while (true) {
			for (PeerAddress pa : master.peerBean().peerMap().all()) {
				System.out.println("PeerAddress: " + pa);
				FutureChannelCreator fcc = master.connectionBean().reservation().create(1, 1);
				fcc.awaitUninterruptibly();

				ChannelCreator cc = fcc.channelCreator();

				FutureResponse fr1 = master.pingRPC().pingTCP(pa, cc, new DefaultConnectionConfiguration());
				fr1.awaitUninterruptibly();

				if (fr1.isSuccess()) {
					System.out.println("peer online T:" + pa);
				} else {
					System.out.println("offline " + pa);
				}

				FutureResponse fr2 = master.pingRPC().pingUDP(pa, cc, new DefaultConnectionConfiguration());
				fr2.awaitUninterruptibly();

				cc.shutdown();

				if (fr2.isSuccess()) {
					System.out.println("peer online U:" + pa);
				} else {
					System.out.println("offline " + pa);
				}
				
			}
			Thread.sleep(1500);
			master.objectDataReply(new ObjectDataReply() {
				
				@Override
				public Object reply(PeerAddress sender, Object request) throws Exception {
					System.out.println("\n=========> Incoming request");
					
					byte[] msg_bytes = (byte[]) request;
					NautilusMessage msg = byteArrayToObject(msg_bytes);
					
					switch (msg.getType()) {
					
					case 0:
						byte[] file = connectionUtilities.processMessageTypeZero(msg);
						if (file == null) {
							return -1;
						}
						return file;
					
					case 1:
						return connectionUtilities.processMessageTypeOne(msg);

					default:
						return -1;
					}
				}
			});
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
