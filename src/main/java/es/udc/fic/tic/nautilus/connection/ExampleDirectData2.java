package es.udc.fic.tic.nautilus.connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import net.tomp2p.dht.FutureSend;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.p2p.builder.BootstrapBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

public class ExampleDirectData2 {
	static int p1Counter = 0;
	static int p2Counter = 0;
	
	@Autowired
	private ConnectionUtilities connectionUtilities;

	public static void main(String[] args) throws Exception {
		final Number160 idP1 = Number160.createHash("p1");
		final Number160 idP2 = Number160.createHash("p2");
		
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(idP1).ports(1234).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(idP2).ports(1235).start()).start();
		BootstrapBuilder b = p2.peer().bootstrap();
		b.bootstrapTo(Arrays.asList(new PeerAddress(idP1, "localhost", 1234, 1234)));
		b.start().awaitUninterruptibly();

		p1.peer().objectDataReply(new ObjectDataReply() {
			@Override
			public Object reply(PeerAddress sender, Object request) throws Exception {
				if (sender.peerId().equals(idP2)) {
					byte[] val = (byte[]) request;
					
					NautilusMessage msg = byteArrayToObject(val);
					
					//System.out.println(msg.getHash());
					switch(msg.getType()) {
					
					case 0:
						/* Lógica que llame a la funcion que busque
						 * un file por su hash y lo devuelva */
						return 0;
					
					case 1:
						/* Lógica para almacenar un fichero en
						 * el sistema */
						System.out.println(msg.getHash());
						return 1;
					}

				}

				return null;
			}
		});

		// p2 -> p1 -- Works
		p2SendNext(p2, idP1);
	}

	static void p2SendNext(final PeerDHT p, final Number160 idP1) throws Exception {
		
		NautilusMessage msg = new NautilusMessage(1, "12345hola");
		
		p.send(idP1).object(objectToByteArray(msg)).requestP2PConfiguration(new 
				RequestP2PConfiguration(1, 5, 0)).start().addListener(new BaseFutureListener<FutureSend>() {

			        @Override
			        public void operationComplete(FutureSend future) throws Exception {

				        Object[] values = future.rawDirectData2().values().toArray();
				        
				        System.out.println(values[0]);
				        System.exit(0);
				        if (values.length == 1)
					        System.err.println(String.format("P2 received: %d", values[0]));
				        else if (values.length != 1) {
					        throw new Exception("Invalid length");
				        }


				        //p2SendNext(p, idP1);
			        }

			        @Override
			        public void exceptionCaught(Throwable t) throws Exception {
				        System.err.println(t.toString());

			        }
		        });
	}
	
	/* Private functions */
	
	private static byte[] objectToByteArray(NautilusMessage object) throws Exception {
		ByteArrayOutputStream bs= new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream (bs);
		os.writeObject(object);
		os.close();
		return bs.toByteArray();
	}
	
	private static NautilusMessage byteArrayToObject(byte[] bytes) throws Exception {
		ByteArrayInputStream bs= new ByteArrayInputStream(bytes);
		ObjectInputStream is = new ObjectInputStream(bs);
		NautilusMessage message = (NautilusMessage)is.readObject();
		is.close();
		return message;
	}
}