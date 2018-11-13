package p2p;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.tomp2p.connection.RSASignatureFactory;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageDisk;

public class PeerToPeer {
	private static PeerToPeer instance = null;
	private PeerDHT master;
	private final int PORT = 4001;
	private final int NUM_OF_PEERS = 3;
	private PeerDHT[] peers;
	private final int GUID_STEP = 10000;
	private final int DISK_GUID = 2421;
	
	private PeerToPeer() {
		//Creates a list of Peers (3)
        try {
			peers = createAndAttachPeersDHT(NUM_OF_PEERS, PORT);
			
	        /* Bootstrapping is the process that a new peer who
	        intends to join a P2P network uses to discover contact
	        information for another peer in the existing network. 
	        Or just starting a new P2P network */
	        bootstrap(peers);
	        master = peers[0];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	/**
	 * Singleton class instantiation. Returns single singleton object.
	 * 
	 * @return Returns Singleton PeerToPeer instance.
	 */
	public static PeerToPeer getInstance() {
		if (instance == null ) 
			instance = new PeerToPeer();
		return instance;
	}
	
	/**
	 * Maps each peer to every other peer. 
	 * 
	 * Taken from TomP2P example.
	 * 
	 * @param peers Returns the peers, now interconnected. 
	 */
    private void bootstrap( PeerDHT[] peers ) {
    	//loop through all peers
    	for(int i=0;i<peers.length;i++) {
    		//for each peer, loop through all other peers to build each peer map
    		for(int j=0;j<peers.length;j++) {
    			peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
    		}
    	}
    }
    
    /**
     * Creates peers and attaches them together.
     * 
     * Taken from TomP2P example. (And then edited to store to disk rather than RAM.
     * 
     * @param nr Number of peers to create.
     * @param port Port number for the peers to listen on.
     * @return Returns array of peers.
     * @throws IOException Throws IO exception if can't access disk.
     */
    private PeerDHT[] createAndAttachPeersDHT( int nr, int port ) throws IOException {
        PeerDHT[] peers = new PeerDHT[nr];
        
        //create path for peer to peer files
        Path path = Paths.get("p2p/tomp2p");
        
        File file = path.toFile();
        
        //create disk object to store on disk
        StorageDisk disk = new StorageDisk(new Number160(DISK_GUID), file,  new RSASignatureFactory());
        
        for ( int i = 0; i < nr; i++ ) {
            if ( i == 0 ) {
                peers[0] = new PeerBuilderDHT(new PeerBuilder( new Number160( (i + 1) * GUID_STEP ) ).ports( port ).start()).storage(disk).start();
            } else {
                peers[i] = new PeerBuilderDHT(new PeerBuilder( new Number160( (i + 1) * GUID_STEP) ).masterPeer( peers[0].peer() ).start()).storage(disk).start();
            }      
        }
        
        return peers;
    }
    
    /**
     * Puts data to a peer using the given guid.
     * 
     * @param data Data to be placed on peer
     * @param guid GUID of peer.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void Put(Serializable data, int guid) 
            throws IOException, ClassNotFoundException {
    	/* In order to store data in TomP2P, the object needs to be wrapped with the Data class. 
    	 * The data class offers additional features, such as setting a TTL or signing the object. 
    	 * Then, put or add is called, which starts the routing process, finds the peers close to nr, 
    	 * where the data is stored. Since we â€œonlyâ€� have a Peer object, we need to 
    	 * create a PeerDHT object first.
    	 */
    	
    	FuturePut futurePut = master.put(new Number160(guid)).data(new Data(data)).start();
    	//waits for a result
        futurePut.awaitUninterruptibly();

    }
    
    /**
     * Returns an object that was stored on a peer.
     * 
     * @param guid GUID of object that was stored.
     * @return Returns object that was stored using that GUID.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public Object Get(int guid) throws ClassNotFoundException, IOException {
    	FutureGet futureGet = master.get(new Number160(guid)).start();
        /* Since TomP2P uses non-blocking communication, a future object is used to keep track of future results. 
         * Thus, a get().start(), put().start(), or add().start() returns immediately and the future object is 
         * used to get the results from those operations.
         */
    	futureGet.awaitUninterruptibly();
    	return futureGet.data().object();
    }
    
    /**
     * Closes the tomp2p instance.
     */
    public void close() {
    	if (master != null) {
    		master.shutdown();
        }
    }

}
