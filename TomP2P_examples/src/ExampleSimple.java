import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import net.tomp2p.dht.PeerBuilderDHT;

/**
 * This simple example creates 10 nodes, bootstraps to the first and put and get data from those 10 nodes.
 * 
 * @author draft
 */
public final class ExampleSimple {
    private static final Random RND = new Random(42L);
    private static final int PEER_NR_1 = 30;
    private static final int PEER_NR_2 = 77;
    
    /**
     * Empty constructor.
     */
    private ExampleSimple() { }

    /**
     * @param args No arguments necessary
     * @throws Exception .
     */
    public static void main(final String[] args) throws Exception {
    	PeerDHT master = null;
        final int nrPeers = 100;
        final int port = 4001;
        final int waitingTime = 250;
        try {
        	//Creates a list of Peers (100)
        	PeerDHT[] peers = createAndAttachPeersDHT(nrPeers, port);
        	/* Bootstrapping is the process that a new peer who
        	intends to join a P2P network uses to discover contact
        	information for another peer in the existing network. 
        	Or just starting a new P2P network */
            bootstrap(peers);
            master = peers[0];
            Number160 nr = new Number160(RND);
            examplePutGet(peers, nr);
            exampleGetBlocking(peers, nr);
            exampleGetNonBlocking(peers, nr);
            Thread.sleep(waitingTime);
            exampleAddGet(peers);
        } finally {
            if (master != null) {
                master.shutdown();
            }
        }
    }
    
    public static void bootstrap( PeerDHT[] peers ) {
    	for(int i=0;i<peers.length;i++) {
    		for(int j=0;j<peers.length;j++) {
    			peers[i].peerBean().peerMap().peerFound(peers[j].peerAddress(), null, null, null);
    		}
    	}
    }
    
    public static PeerDHT[] createAndAttachPeersDHT( int nr, int port ) throws IOException {
        PeerDHT[] peers = new PeerDHT[nr];
        for ( int i = 0; i < nr; i++ ) {
            if ( i == 0 ) {
                peers[0] = new PeerBuilderDHT(new PeerBuilder( new Number160( RND ) ).ports( port ).start()).start();
            } else {
                peers[i] = new PeerBuilderDHT(new PeerBuilder( new Number160( RND ) ).masterPeer( peers[0].peer() ).start()).start();
            }
        }
        return peers;
    }

    /**
     * Basic example for storing and retrieving content.
     * 
     * @param peers The peers in this P2P network
     * @param nr The number where the data is stored
     * @throws IOException e.
     * @throws ClassNotFoundException .
     */
    private static void examplePutGet(final PeerDHT[] peers, final Number160 nr) 
            throws IOException, ClassNotFoundException {
    	/* In order to store data in TomP2P, the object needs to be wrapped with the Data class. 
    	 * The data class offers additional features, such as setting a TTL or signing the object. 
    	 * Then, put or add is called, which starts the routing process, finds the peers close to nr, 
    	 * where the data is stored. Since we “only” have a Peer object, we need to 
    	 * create a PeerDHT object first.
    	 */
        FuturePut futurePut = peers[10].put(nr).data(new Data("hallo")).start();
        //waits for a result
        futurePut.awaitUninterruptibly();
        System.out.println("peer " + PEER_NR_1 + " stored [key: " + nr + ", value: \"hallo\"]");
        FutureGet futureGet = peers[20].get(nr).start();
        /* Since TomP2P uses non-blocking communication, a future object is used to keep track of future results. 
         * Thus, a get().start(), put().start(), or add().start() returns immediately and the future object is 
         * used to get the results from those operations.
         */
        futureGet.awaitUninterruptibly();
        System.out.println("peer " + PEER_NR_2 + " got: \"" + futureGet.data().object() + "\" for the key " + nr);
        // the output should look like this:
        // peer 30 stored [key: 0xba419d350dfe8af7aee7bbe10c45c0284f083ce4, value: "hallo"]
        // peer 77 got: "hallo" for the key 0xba419d350dfe8af7aee7bbe10c45c0284f083ce4
    }

    private static void exampleAddGet( PeerDHT[] peers )
        throws IOException, ClassNotFoundException
    {
        Number160 nr = new Number160( RND );
        String toStore1 = "hallo1";
        String toStore2 = "hallo2";
        Data data1 = new Data( toStore1 );
        Data data2 = new Data( toStore2 );
        FuturePut futurePut = peers[30].add( nr ).data( data1 ).start();
        futurePut.awaitUninterruptibly();
        System.out.println( "added: " + toStore1 + " (" + futurePut.isSuccess() + ")" );
        futurePut = peers[50].add( nr ).data( data2 ).start();
        futurePut.awaitUninterruptibly();
        System.out.println( "added: " + toStore2 + " (" + futurePut.isSuccess() + ")" );
        FutureGet futureGet = peers[77].get( nr ).all().start();
        futureGet.awaitUninterruptibly();
        System.out.println( "size" + futureGet.dataMap().size() );
        Iterator<Data> iterator = futureGet.dataMap().values().iterator();
        System.out.println( "got: " + iterator.next().object() + " (" + futureGet.isSuccess() + ")" );
        System.out.println( "got: " + iterator.next().object() + " (" + futureGet.isSuccess() + ")" );
    }

    /**
     * Example of a blocking operation and what happens after.
     * @param peers The peers in this P2P network
     * @param nr The number where the data is stored
     * @throws ClassNotFoundException .
     * @throws IOException .
     */
    private static void exampleGetBlocking(final PeerDHT[] peers, final Number160 nr)
        throws ClassNotFoundException, IOException {
        FutureGet futureGet = peers[PEER_NR_2].get(nr).start();
        // blocking operation
        futureGet.awaitUninterruptibly();
        System.out.println("result blocking: " + futureGet.data().object());
        System.out.println("this may *not* happen before printing the result");
    }

    /**
     * Example of a non-blocking operation and what happens after. This is the preferred method
     * @param peers The peers in this P2P network
     * @param nr The number where the data is stored
     */
    private static void exampleGetNonBlocking(final PeerDHT[] peers, final Number160 nr) {
        FutureGet futureGet = peers[PEER_NR_2].get(nr).start();
        // non-blocking operation
        futureGet.addListener(new BaseFutureAdapter<FutureGet>() {
        	@Override
			public void operationComplete(FutureGet future) throws Exception {
        		System.out.println("result non-blocking: " + future.data().object());
            }
            
        });
        System.out.println("this may happen before printing the result");
        
        /* The .awaitUninterruptibly() vs .addListener
         * The first is by blocking and waiting for the result to arrive, 
         * which can be either await() or awaitUninterruptibly(). The second 
         * option is to add a listener, which gets called whenever a result is 
         * ready. It is preferred to use this second option and avoid blocking, 
         * because in the worst case, you might cause a deadlock if await() is 
         * called from a wrong (I/O) thread. If such a listener is used, then 
         * the listeners gets called in all cases. If no peer replies, the 
         * timeout handler triggers the listener.
         */
    }
}