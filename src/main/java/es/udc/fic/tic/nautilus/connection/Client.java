package es.udc.fic.tic.nautilus.connection;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Collection;
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


public class Client {
	
	public static void main (String[] args) throws Exception {
		startClient("192.168.1.36");
	}
	
	public static void startClient(String ipAddress) throws Exception {
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
		System.out.println("****** "+addressList.size());

		if (futureDiscover.isSuccess()) {
			System.out.println("found that my outside address is " + futureDiscover.peerAddress());
		} else {
			System.out.println("failed " + futureDiscover.failedReason());
		}
		
		PeerAddress peerA = addressList.iterator().next();
		
		FutureDirect future = client.sendDirect(peerA).object(2).start();
		future.awaitUninterruptibly();
		
		if (future.isSuccess()) {
			System.out.println("works!");
			int val = (int) future.object();
			System.out.println(val);
		} else {
			System.out.println(future.failedReason());
		}
		
		
		client.shutdown();
	}
	
}