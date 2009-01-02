

import java.util.concurrent.*;
import java.nio.channels.*;
import java.util.logging.*;
import java.beans.*;
import java.util.*;
import java.nio.*;
import java.net.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;



/**
 * <p>A robust class for establishing a TCP server and manipulating
 * its listening port.
 * The {@link Event}s and property change events make
 * it an appropriate tool in a threaded, GUI application.
 * It is almost identical in design to the UdpServer and TcpServer classes that
 * should have accompanied this class when you downloaded it.</p>
 * 
 * <p>To start a TCP server, create a new NioServer and call start():</p>
 * 
 * <pre> NioServer server = new NioServer();
 * server.start();</pre>
 * 
 * <p>Of course it won't be much help unless you register as a listener
 * so you'll know when data has come in:</p>
 * 
 * <pre> server.addNioServerListener( new NioServer.Adapter(){
 *     public void nioServerDataReceived( NioServer.Event evt ){
 *         ByteBuffer buff = evt.getBuffer();
 *         ...
 *     }   // end data received
 * });</pre>
 * 
 * <p>The server runs on one thread, and all events are fired on that thread.
 * Consider offloading heavy processing to another thread. Be aware that
 * you can register multiple listeners to respond to incoming data
 * so be mindful of more than one listener being around to makes calls
 * on the new Socket.</p>
 * 
 * <p>The public methods are all synchronized on <tt>this</tt>, and great
 * care has been taken to avoid deadlocks and race conditions. That being said,
 * there may still be bugs (please contact the author if you find any), and
 * you certainly still have the power to introduce these problems yourself.</p>
 * 
 * <p>It's often handy to have your own class extend this one rather than
 * making an instance field to hold a NioServer where you'd have to
 * pass along all the setPort(...) methods and so forth.</p>
 * 
 * <p>The supporting {@link Event}, {@link Listener}, and {@link Adapter}
 * classes are static inner classes in this file so that you have only one
 * file to copy to your project. You're welcome.</p>
 * 
 * <p>This code is released into the Public Domain.
 * Since this is Public Domain, you don't need to worry about
 * licensing, and you can simply copy this NioServer.java file
 * to your own package and use it as you like. Enjoy.
 * Please consider leaving the following statement here in this code:</p>
 * 
 * <p><em>This <tt>NioServer</tt> class was copied to this project from its source as
 * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
 *
 * @author Robert Harder
 * @author rharder@users.sourceforge.net
 * @version 0.1
 * @see NioServer
 * @see Adapter
 * @see Event
 * @see Listener
 */
public class NioServer {
    
    private final static Logger LOGGER = Logger.getLogger(NioServer.class.getName());
    

    /** The buffer size property. */
    public final static String BUFFER_SIZE_PROP = "bufferSize";
    private final static int BUFFER_SIZE_DEFAULT = 4096;
    private int bufferSize = BUFFER_SIZE_DEFAULT;
    
    
    /**
     * <p>One of four possible states for the server to be in:</p>
     * 
     * <ul>
     *  <li>STARTING</li>
     *  <li>STARTED</li>
     *  <li>STOPPING</li>
     *  <li>STOPPED</li>
     * </ul>
     */
    public static enum State { STARTING, STARTED, STOPPING, STOPPED };
    private State currentState = State.STOPPED;
    public final static String STATE_PROP = "state";
    
    
    private final Collection<Listener> listeners = new LinkedList<Listener>();                // Event listeners
    private final NioServer.Event event = new NioServer.Event(this);                                              // Shared event
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);        // Properties
    
    private final NioServer This = this;                                             // To aid in synchronizing
    private ThreadFactory threadFactory;                                                // Optional thread factory
    private Thread ioThread;                                                            // Performs IO
    private Selector selector;                                                  // Brokers all the connections

    
    private final Map<SocketAddress,SelectionKey> tcpBindings = new HashMap<SocketAddress,SelectionKey>();// Requested TCP bindings, e.g., "listen on port 80"
    private final Map<SocketAddress,SelectionKey> udpBindings = new HashMap<SocketAddress,SelectionKey>();// Requested UDP bindings

    private final Map<SocketAddress,String> multicastGroups = new HashMap<SocketAddress,String>();

    private final Set<SocketAddress> pendingTcpAdds = new HashSet<SocketAddress>(); // TCP bindings to add to selector on next cycle
    private final Set<SocketAddress> pendingUdpAdds = new HashSet<SocketAddress>(); // UDP bindings to add to selector on next cycle

    private final Set<SocketAddress> pendingTcpRemoves = new HashSet<SocketAddress>(); // TCP bindings to remove from selector on next cycle
    private final Set<SocketAddress> pendingUdpRemoves = new HashSet<SocketAddress>(); // UDP bindings to remove from selector on next cycle


/* ********  C O N S T R U C T O R S  ******** */
    
    
    /**
     * Constructs a new NioServer that will listen on the default port 8000
     * (but not until {@link #start} is called).
     * The I/O thread will not be in daemon mode.
     */
    public NioServer(){
    }
    
    /**
     * Constructs a new NioServer that will listen on the given port
     * (but not until {@link #start} is called). The provided
     * ThreadFactory will be used when starting and running the server.
     * @param factory the ThreadFactory to use when starting the server
     */
    public NioServer( ThreadFactory factory ){
        this.threadFactory = factory;
    }
    
    
    
    
/* ********  R U N N I N G  ******** */
    
    
    /**
     * Attempts to start the server listening and returns immediately.
     * Listen for start events to know if the server was
     * successfully started.
     * 
     * @see Listener
     */
    public synchronized void start(){
        if( this.currentState == State.STOPPED ){           // Only if we're stopped now
            assert ioThread == null : ioThread;             // Shouldn't have a thread

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    runServer();                            // This runs for a long time
                    ioThread = null;          
                    setState( State.STOPPED );              // Clear thread
                }   // end run
            };  // end runnable
            
            if( this.threadFactory != null ){               // User-specified threads
                this.ioThread = this.threadFactory.newThread(run);
                
            } else {                                        // Our own threads
                this.ioThread = new Thread( run, this.getClass().getName() );   // Named
            }

            setState( State.STARTING );                     // Update state
            this.ioThread.start();                          // Start thread
        }   // end if: currently stopped
    }   // end start
    
    
    /**
     * Attempts to stop the server, if the server is in
     * the STARTED state, and returns immediately.
     * Be sure to listen for stop events to know if the server was
     * successfully stopped.
     * 
     * @see Listener
     */
    public synchronized void stop(){
        if( this.currentState == State.STARTED || this.currentState == State.STARTING ){   // Only if already STARTED
            setState( State.STOPPING );             // Mark as STOPPING
            if( this.selector != null ){           //
                try{
                    this.selector.close();
                } catch( IOException exc ){
                    LOGGER.log( 
                      Level.SEVERE,
                      "An error occurred while closing the server. " +
                      "This may have left the server in an undefined state.",
                      exc );
                }
            }   // end if: not null
        }   // end if: already STARTED
    }   // end stop
    
    
    
    
    /**
     * Returns the current state of the server, one of
     * STOPPED, STARTING, or STARTED.
     * @return state of the server
     */
    public synchronized State getState(){
        return this.currentState;
    }
    
    
    /**
     * Sets the state and fires an event. This method
     * does not change what the server is doing, only
     * what is reflected by the currentState variable.
     * @param state the new state of the server
     */
    protected synchronized void setState( State state ){
        State oldVal = this.currentState;
        this.currentState = state;
        firePropertyChange(STATE_PROP, oldVal, state);
    }
    
    
    /**
     * Resets the server, if it is running, otherwise does nothing.
     * This is accomplished by registering as a listener, stopping
     * the server, detecting the stop, unregistering, and starting
     * the server again. It's a useful design pattern, and you may
     * want to look at the source code for this method to check it out.
     */
    public synchronized void reset(){
        switch( this.currentState ){
            case STARTED:
                this.addPropertyChangeListener(STATE_PROP, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        State newState = (State)evt.getNewValue();
                        if( newState == State.STOPPED ){
                            NioServer server = (NioServer)evt.getSource();
                            server.removePropertyChangeListener(STATE_PROP,this);
                            server.start();
                        }   // end if: stopped
                    }   // end prop change
                });
                stop();
                break;
        }   // end switch
    }
    
    
    /**
     * This method starts up and listens indefinitely
     * for TCP packets. On entering this method,
     * the state is assumed to be STARTING. Upon exiting
     * this method, the state will be STOPPING.
     */
    protected void runServer(){
        try{
            this.selector = Selector.open();
            ByteBuffer buff = ByteBuffer.allocateDirect(this.bufferSize);

            // Add all the requested TCP and UDP bindings to the "pending" lists
            synchronized( this ){
                this.pendingTcpAdds.clear();
                this.pendingTcpAdds.addAll(this.tcpBindings.keySet());
                this.pendingUdpAdds.clear();
                this.pendingUdpAdds.addAll(this.udpBindings.keySet());
            }
            
            setState( State.STARTED );                                          // Mark as started
            while( this.selector.isOpen() ){

                // Check to see if the server is supposed to be stopping
                synchronized( this ){
                    if( this.currentState == State.STOPPING ){
                        LOGGER.finer( "Stopping server by request." );
                        this.selector.close();
                        continue;
                    }   // end if: stopping
                }   // end sync


                // Add and remove pending bindings
                synchronized( this ){

                    // Pending TCP Adds
                    for( SocketAddress addr : this.pendingTcpAdds ){            // For each add
                        ServerSocketChannel sc = ServerSocketChannel.open();    // Open a channel
                        sc.socket().bind(addr);                                 // Bind as requested
                        sc.configureBlocking(false);                            // Make non-blocking
                        SelectionKey acceptKey = sc.register(                   // Register with master Selector
                          this.selector, SelectionKey.OP_ACCEPT );              // We want to "accept" connections
                        this.tcpBindings.put(addr, acceptKey);                  // Save the key
                    }   // end for: each address
                    this.pendingTcpAdds.clear();                                // Remove list of pending adds

                    // Pending UDP Adds
                    for( SocketAddress addr : this.pendingUdpAdds ){            // Same comments as for TCP
                        DatagramChannel dc = DatagramChannel.open();
                        dc.socket().bind(addr);
                        dc.configureBlocking(false);
                        SelectionKey acceptKey = dc.register(
                          this.selector, SelectionKey.OP_READ );
                        this.udpBindings.put(addr, acceptKey);

                        
                        // Found a weird hack to support multicast -- at least for now.
                        String group = this.multicastGroups.get(addr);
                        if( group != null && addr instanceof InetSocketAddress ){
                            int port = ((InetSocketAddress)addr).getPort();
                            InetSocketAddress groupAddr = new InetSocketAddress(group,port);
                            if( groupAddr.getAddress().isMulticastAddress() ){

                                // http://www.mernst.org/blog/archives/12-01-2006_12-31-2006.html
                                // UGLY UGLY HACK: multicast support for NIO
                                // create a temporary instanceof PlainDatagramSocket, set its fd and configure it
                                @SuppressWarnings("unchecked")
                                Constructor<? extends DatagramSocketImpl> c =
                                  (Constructor<? extends DatagramSocketImpl>)
                                    Class.forName("java.net.PlainDatagramSocketImpl").getDeclaredConstructor();
                                c.setAccessible(true);
                                DatagramSocketImpl socketImpl = c.newInstance();

                                Field channelFd = Class.forName("sun.nio.ch.DatagramChannelImpl").getDeclaredField("fd");
                                channelFd.setAccessible(true);

                                Field socketFd = DatagramSocketImpl.class.getDeclaredField("fd");
                                socketFd.setAccessible(true);
                                socketFd.set(socketImpl, channelFd.get(dc));

                                try {
                                  Method m = DatagramSocketImpl.class.getDeclaredMethod("joinGroup", SocketAddress.class, NetworkInterface.class);
                                  //Method m = DatagramSocketImpl.class.getDeclaredMethod("joinGroup", InetAddress.class );
                                  m.setAccessible(true);
                                  m.invoke(socketImpl, groupAddr, null );
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                finally {
                                  // important, otherwise the fake socket's finalizer will nuke the fd
                                  socketFd.set(socketImpl, null);
                                }

                            }   // end if: multicast
                            else{
                                LOGGER.warning("Could not join non-multicast group: " + group);
                            }
                        }   // end if: got group




                    }   // end for: each address
                    this.pendingUdpAdds.clear();

                    // Pending TCP Removes
                    for( SocketAddress addr : this.pendingTcpRemoves ){         // For each pending remove
                        SelectionKey key = this.tcpBindings.get(addr);          // Get the registered key
                        if( key != null ){                                      // Might be null if someone gave us bogus address
                            key.channel().close();                              // Close the channel
                            key.cancel();                                       // And cancel the key (redundant?)
                            this.tcpBindings.remove(addr);                      // Remove from master list
                        }   // end if: key != null
                    }   // end for: each remove
                    this.pendingTcpRemoves.clear();                             // Remove from list of pending removes

                    // Pending UDP Removes
                    for( SocketAddress addr : this.pendingUdpRemoves ){         // Same comments as for TCP
                        SelectionKey key = this.udpBindings.get(addr);
                        if( key != null ){
                            key.channel().close();
                            key.cancel();
                            this.udpBindings.remove(addr);
                        }   // end if: key != null
                    }   // end for: each remove
                    this.pendingUdpRemoves.clear();
                }   // end sync: this

                //
                // The meat of the matter begins now
                //

                ////////  B L O C K S   H E R E
                if( this.selector.select() <= 0 ){                              // Block until notified
                    LOGGER.finer("selector.select() <= 0");                     // Possible false start
                    Thread.sleep(100);                                          // Let's not run away from ourselves
                }///////  B L O C K S   H E R E

                
                // Possibly resize buffer if a change was requested since last cycle
                if( this.bufferSize != buff.capacity() ){                       // Mismatch size means someone asked for something new
                    assert this.bufferSize >= 0 : this.bufferSize;              // We check for this in setBufferSize(..)
                    buff = ByteBuffer.allocateDirect(this.bufferSize);          // Resize and use direct for OS efficiencies
                }

                Set<SelectionKey> keys = this.selector.selectedKeys();          // These keys need attention
                if( LOGGER.isLoggable(Level.FINEST ) ){                         // Only report this at finest grained logging level
                    LOGGER.finest("Keys: " + keys );                            // Which keys are being examined this round
                }

                Iterator<SelectionKey> iter = keys.iterator();                  // Iterate over keys -- cannot use "for" loop since we remove keys
                while( iter.hasNext() ){                                        // Each key
                    SelectionKey key = iter.next();                             // The key
                    iter.remove();                                              // Remove from list

                    // Accept connections
                    // This should only be from the TCP bindings
                    if(  key.isAcceptable()  ){                                 // New, incoming connection?
                        handleAccept( key );                                    // Handle accepting connections
                    }

                    // Data to read
                    // This could be an ongoing TCP connection
                    // or a new (is there any other kind) UDP datagram
                    else if( key.isReadable() ){                                // Existing connection has data (or is closing)
                        handleRead( key, buff );                                // Handle data
                    }   // end if: readable

                }   // end while: keys

            }   // end while: selector is open
            
        } catch( Exception exc ){
            synchronized( this ){
                if( this.currentState == State.STOPPING ){  // User asked to stop
                    try{
                        this.selector.close();
                        LOGGER.info( "Server closed normally." );
                    } catch( IOException exc2 ){
                        LOGGER.log( 
                          Level.SEVERE,
                          "An error occurred while closing the server. " +
                          "This may have left the server in an undefined state.",
                          exc2 );
                    }   // end catch IOException
                } else {
                    LOGGER.log( Level.WARNING, "Server closed unexpectedly: " + exc.getMessage(), exc );
                }   // end else
            }   // end sync
        } finally {
            setState( State.STOPPING );
            if( this.selector != null ){
                try{
                    this.selector.close();
                    LOGGER.info( "Server closed normally." );
                } catch( IOException exc2 ){
                    LOGGER.log( 
                      Level.SEVERE,
                      "An error occurred while closing the server. " +
                      "This may have left the server in an undefined state.",
                      exc2 );
                }   // end catch IOException
            }   // end if: not null
            this.selector = null;
        }
    }


    /**
     * Handles accepting new connections.
     * @param sel The selector with which we'll register
     * @param key The OP_ACCEPT key
     * @throws java.io.IOException if an error occurs
     */
    private void handleAccept( SelectionKey key ) throws IOException{
        assert key.isAcceptable() : key.readyOps();                             // We know it should be acceptable
        assert selector.isOpen();                                               // Not sure this matters. Meh.

        SelectableChannel sc = key.channel();                                   // Channel for th key
        assert sc instanceof ServerSocketChannel : sc;                          // Only our TCP connections have OP_ACCEPT
//        if( sc instanceof ServerSocketChannel ){

        ServerSocketChannel ch = (ServerSocketChannel)key.channel();            // Server channel
        SocketChannel incoming = null;                                          // Reusable for all pending connections
        while( (incoming = ch.accept()) != null ){                              // Iterate over all pending connections
            incoming.configureBlocking(false);                                  // Non-blocking IO
            SelectionKey incomingReadKey = incoming.register(                   // Register new connection
              this.selector,                                                    // With the Selector
              SelectionKey.OP_READ);//,                                             // Want to READ data
              //(ByteBuffer)ByteBuffer.allocateDirect(128).flip() );              // Will store leftover data between read ops

            fireNewConnection(incomingReadKey);

            if( LOGGER.isLoggable(Level.FINEST) ){
                LOGGER.finest("  " + incoming + ", key: " + incomingReadKey );
            }
        }   // end while: each incoming connection

    }

    /**
     * Handles reading incoming data and then firing events.
     * @param key The key associated with the reading
     * @param buff the ByteBuffer to hold the data
     * @throws java.io.IOException if an error occurs
     */
    private void handleRead(SelectionKey key, ByteBuffer buff ) throws IOException {

        SelectableChannel sc = key.channel();
        buff.clear();                                                           // Clear input buffer

        // TCP
        if( sc instanceof SocketChannel ){

            SocketChannel client = (SocketChannel) key.channel();               // Source socket
            ByteBuffer leftover = (ByteBuffer)key.attachment();                 // Leftover bytes are in attachment

            if( leftover != null && leftover.remaining() > 0 ){                 // Have a leftover buffer
                buff.put(leftover);                                             // Preload leftovers
            }   // end if: have leftover buffer

            // Read into the buffer here
            if( client.read(buff) == -1 ){                                      // End of stream?
                key.cancel();                                                   // Cancel the key
                client.close();                                                 // And cancel the client
                fireConnectionClosed(key);                                      // Fire event for connection closed
                if( LOGGER.isLoggable(Level.FINER) ){
                    LOGGER.finer("Connection closed: " + key );
                }
                
            } else {
                buff.flip();                                                    // Flip the buffer to prepare to read
                key.attach( buff );                                             // Put the channel's data in the attachment
                fireTcpDataReceived(key);                                       // Fire event for new data

                if( buff.remaining() > 0 ){                                     // Did the user leave data for next time?
                    if( leftover == null ||                                     // Leftover buffer not yet created?
                        buff.remaining() > leftover.capacity() ){               // Or is too small?
                        leftover = ByteBuffer.allocateDirect(buff.remaining()); // Create/resize
                    }   // end if: need to resize
                    leftover.clear();                                           // Clear old leftovers
                    leftover.put(buff).flip();                                  // Save new leftovers
                }   // end if: has remaining bytes
                key.attach(leftover);                                           // Save leftovers for next time
            }   // end else: read
        }   // end if: SocketChannel

        // Datagram
        else if( sc instanceof DatagramChannel ){
            DatagramChannel dc = (DatagramChannel)sc;                           // Cast to datagram channel
            SocketAddress remote = null;
            while( (remote = dc.receive(buff)) != null ){                       // Loop over all pending datagrams
                buff.flip();                                                    // Flip after reading in
                key.attach( buff );                                             // Attach buffer to key
                fireUdpDataReceived(key,remote);                                // Fire event
                key.attach(null);                                               // Clear attachment
            }   // end while: each pending datagram
        }   // end else: UDP

    }   // end handleRead



    



/* ********  B U F F E R   S I Z E  ******** */

    /**
     * Returns the size of the ByteBuffer used to read
     * from the connections. This refers to the buffer
     * that will be passed along with {@link Event}
     * objects as data is received and so forth.
     * @return The size of the ByteBuffer
     */
    public synchronized int getBufferSize(){
        return this.bufferSize;
    }

    /**
     * Sets the size of the ByteBuffer used to read
     * from the connections. This refers to the buffer
     * that will be passed along with {@link Event}
     * objects as data is received and so forth.
     * @param size The size of the ByteBuffer
     * @throws IllegalArgumentException if size is not positive
     */
    public synchronized void setBufferSize( int size ){
        if( size <= 0 ){
            throw new IllegalArgumentException( "New buffer size must be positive: " + size );
        }   // end if: size outside range

        int oldVal = this.bufferSize;
        this.bufferSize = size;
        this.selector.wakeup();

        firePropertyChange( BUFFER_SIZE_PROP, oldVal, size  );
    }

/* ********  B I N D I N G S  ******** */



    /**
     * Adds a TCP binding to the server. Effectively this is how you
     * set which ports and on which interfaces you want the server
     * to listen. In the simplest case, you might do the following
     * to listen generically on port 80:
     * <code>addTcpBinding( new InetAddress(80) );</code>.
     * The server can listen on multiple ports at once.
     * @param addr The address on which to listen
     */
    public synchronized void addTcpBinding( SocketAddress addr ){
        this.tcpBindings.put(addr,null);
        this.pendingTcpAdds.add(addr);
        this.pendingTcpRemoves.remove(addr);
        
        if( this.selector != null ){
            this.selector.wakeup();
        }
    }



    /**
     * Adds a UDP binding to the server. Effectively this is how you
     * set which ports and on which interfaces you want the server
     * to listen. In the simplest case, you might do the following
     * to listen generically on port 6997:
     * <code>addUdpBinding( new InetAddress(6997) );</code>.
     * The server can listen on multiple ports at once.
     * @param addr The address on which to listen
     */
    public synchronized void addUdpBinding( SocketAddress addr ){
        addUdpBinding(addr,null);
    }


    /**
     * <p><em>Experimental Hack</em> - Adds a UDP binding to the server
     * and joins the given multicast group (if <code>group<code>
     * is not null and is a valid multicast group).
     * In the simplest case, you might do the following
     * to listen on port 16000 and multicast group 239.0.0.1:
     * <code>addUdpBinding( new InetAddress(16000), "239.0.0.1" );</code>.
     * The server can listen on multiple ports at once.</p>
     *
     * <p>As of Java 6, the <code>java.nio</code> "New IO" packages
     * don't support multicast groups ("annoyed grunt"), however I
     * found a clever hack at this gentleman's website
     * (<a href="http://www.mernst.org/blog/archives/12-01-2006_12-31-2006.html">http://www.mernst.org/blog/archives/12-01-2006_12-31-2006.html</a>)
     * that makes multicast work -- for now.</p>
     * 
     * @param addr The address on which to listen
     * @param group The multicast group to join
     */
    public synchronized void addUdpBinding( SocketAddress addr, String group ){
        this.udpBindings.put(addr,null);
        this.pendingUdpAdds.add(addr);
        this.pendingUdpRemoves.remove(addr);
        if( group != null ){
            this.multicastGroups.put(addr,group);
        }
        if( this.selector != null ){
            this.selector.wakeup();
        }
    }




    /**
     * Removes a TCP binding. Effectively stops the server from
     * listening to this or that port.
     * @param addr The address to stop listening to
     */
    public synchronized void removeTcpBinding( SocketAddress addr ){
        this.pendingTcpAdds.remove(addr);   // In case it's also pending an add
        this.pendingTcpRemoves.add(addr);
        if( this.selector != null ){
            this.selector.wakeup();
        }
    }


    /**
     * Removes a UDP binding. Effectively stops the server from
     * listening to this or that port.
     * @param addr The address to stop listening to
     */
    public synchronized void removeUdpBinding( SocketAddress addr ){
        this.multicastGroups.remove(addr);
        this.pendingUdpAdds.remove(addr);   // In case it's also pending an add
        this.pendingUdpRemoves.add(addr);
        if( this.selector != null ){
            this.selector.wakeup();
        }
    }

    
    
    
/* ********  E V E N T S  ******** */
    
    

    /** Adds a {@link Listener}.
     * @param l the listener
     */
    public synchronized void addNioServerListener(NioServer.Listener l) {
        listeners.add(l);
    }

    
    /** Removes a {@link Listener}.
     * @param l the listener
     */
    public synchronized void removeNioServerListener(NioServer.Listener l) {
        listeners.remove(l);
    }
    
    
    /** 
     * Fire when data is received.
     * @param key the SelectionKey associated with the data
     */
    protected synchronized void fireTcpDataReceived(SelectionKey key) {
        fireUdpDataReceived(key,null);
    }


    /**
     * Fire when data is received.
     * @param key the SelectionKey associated with the data
     * @param remote the source address of the datagram or null if not available
     */
    protected synchronized void fireUdpDataReceived(SelectionKey key, SocketAddress remote) {
        
        final NioServer.Listener[] ll = listeners.toArray(new NioServer.Listener[ listeners.size() ] );
        this.event.setKey(key);
        this.event.setRemoteAddress(remote);
        
        // Make a Runnable object to execute the calls to listeners.
        // In the event we don't have an Executor, this results in
        // an unnecessary object instantiation, but it also makes
        // the code more maintainable.
        for( NioServer.Listener l : ll ){
            try{
                l.nioServerDataReceived(event);
            } catch( Exception exc ){
                LOGGER.warning("NioServer.Listener " + l + " threw an exception: " + exc.getMessage() );
            }   // end catch
        }   // end for: each listener
     }  // end fireNioServerPacketReceived


    
    /**
     * Fire when a connection is closed remotely.
     * @param key The key for the closed connection.
     */
    protected synchronized void fireConnectionClosed(SelectionKey key) {

        final NioServer.Listener[] ll = listeners.toArray(new NioServer.Listener[ listeners.size() ] );
        this.event.setKey(key);

        // Make a Runnable object to execute the calls to listeners.
        // In the event we don't have an Executor, this results in
        // an unnecessary object instantiation, but it also makes
        // the code more maintainable.
        for( NioServer.Listener l : ll ){
            try{
                l.nioServerConnectionClosed(event);
            } catch( Exception exc ){
                LOGGER.warning("NioServer.Listener " + l + " threw an exception: " + exc.getMessage() );
            }   // end catch
        }   // end for: each listener
     }  // end fireNioServerPacketReceived




    /** Fire when a new connection is established.
     * @param key the SelectionKey associated with the connection
     */
    protected synchronized void fireNewConnection(SelectionKey key) {

        final NioServer.Listener[] ll = listeners.toArray(new NioServer.Listener[ listeners.size() ] );
        this.event.setKey(key);

        // Make a Runnable object to execute the calls to listeners.
        // In the event we don't have an Executor, this results in
        // an unnecessary object instantiation, but it also makes
        // the code more maintainable.
        for( NioServer.Listener l : ll ){
            try{
                l.nioServerNewConnectionReceived(event);
            } catch( Exception exc ){
                LOGGER.warning("NioServer.Listener " + l + " threw an exception: " + exc.getMessage() );
            }   // end catch
        }   // end for: each listener
     }  // end fireNioServerPacketReceived


    
    
    
/* ********  P R O P E R T Y   C H A N G E  ******** */
    
    
    /**
     * Fires property chagne events for all current values
     * setting the old value to null and new value to the current.
     */
    public synchronized void fireProperties(){
        firePropertyChange( STATE_PROP, null, getState()  );
        firePropertyChange( BUFFER_SIZE_PROP, null, getBufferSize()  );
    }
    
    
    /**
     * Fire a property change event on the current thread.
     * 
     * @param prop      name of property
     * @param oldVal    old value
     * @param newVal    new value
     */
    protected synchronized void firePropertyChange( final String prop, final Object oldVal, final Object newVal ){
        try{
            propSupport.firePropertyChange(prop,oldVal,newVal);
        } catch( Exception exc ){
            LOGGER.log(Level.WARNING,
                    "A property change listener threw an exception: " + exc.getMessage()
                    ,exc);
        }   // end catch
    }   // end fire
    
    
    
    /**
     * Add a property listener.
     * @param listener the listener
     */
    public synchronized void addPropertyChangeListener( PropertyChangeListener listener ){
        propSupport.addPropertyChangeListener(listener);
    }

    
    /**
     * Add a property listener for the named property.
     * @param property the property name
     * @param listener the listener
     */
    public synchronized void addPropertyChangeListener( String property, PropertyChangeListener listener ){
        propSupport.addPropertyChangeListener(property,listener);
    }
    
    
    /**
     * Remove a property listener.
     * @param listener the listener
     */
    public synchronized void removePropertyChangeListener( PropertyChangeListener listener ){
        propSupport.removePropertyChangeListener(listener);
    }

    
    /**
     * Remove a property listener for the named property.
     * @param property the property name
     * @param listener the listener
     */
    public synchronized void removePropertyChangeListener( String property, PropertyChangeListener listener ){
        propSupport.removePropertyChangeListener(property,listener);
    }
    
    
    
    
    
/* ********  L O G G I N G  ******** */


    
    /**
     * Static method to set the logging level using Java's
     * <tt>java.util.logging</tt> package. Example:
     * <code>NioServer.setLoggingLevel(Level.OFF);</code>.
     * 
     * @param level the new logging level
     */
    public static void setLoggingLevel( Level level ){
        LOGGER.setLevel(level);
    }
    
    /**
     * Static method returning the logging level using Java's
     * <tt>java.util.logging</tt> package.
     * @return the logging level
     */
    public static Level getLoggingLevel(){
        return LOGGER.getLevel();
    }
    
    
    
    
    
/* ********                                                          ******** */
/* ********                                                          ******** */    
/* ********   S T A T I C   I N N E R   C L A S S   L I S T E N E R  ******** */
/* ********                                                          ******** */
/* ********                                                          ******** */    
    
    
    
    
    
    /**
     * An interface for listening to events from a {@link NioServer}.
     * A single {@link Event} is shared for all invocations
     * of these methods.
     * 
     * <p>This code is released into the Public Domain.
     * Since this is Public Domain, you don't need to worry about
     * licensing, and you can simply copy this NioServer.java file
     * to your own package and use it as you like. Enjoy.
     * Please consider leaving the following statement here in this code:</p>
     * 
     * <p><em>This <tt>NioServer</tt> class was copied to this project from its source as
     * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
     *
     * @author Robert Harder
     * @author rharder@users.sourceforge.net
     * @version 0.1
     * @see NioServer
     * @see Adapter
     * @see Event
     */
    public static interface Listener extends java.util.EventListener {

        /**
         * <p>Called when a new connection is received. The SelectionKey associated
         * with the event (an <code>OP_READ</code> key), is the key that will be
         * used with the data received event. In this way, you can seed a
         * <code>Map</code> or other data structure and associate this very
         * key with the connection. You will only get new connection
         * events for TCP connections (not UDP).</p>
         *
         * <p>If your protocol requires the server to respond to a client
         * upon connection, this sample code demonstrates such an arrangement:</p>
         *
         * <pre>
         *   public void nioServerNewConnectionReceived(NioServer.Event evt) {
         *       SocketChannel ch = (SocketChannel) evt.getKey().channel();
         *       try {
         *           ch.write(ByteBuffer.wrap("Greetings\r\n".getBytes()));
         *       } catch (IOException ex) {
         *           ex.printStackTrace(); // Please don't do printStackTrace in production code
         *       }
         *   }
         * </pre>
         * @param evt the shared event
         */
        public abstract void nioServerNewConnectionReceived( NioServer.Event evt );


        /**
         * <p>Called when data is received. Retrieve the associated ByteBuffer
         * with <code>evt.getBuffer()</code>. This is the source ByteBuffer
         * used by the server directly to receive the data. It is a 
         * "direct" ByteBuffer (created with <code>ByteBuffer.allocateDirect(..)</code>).
         * Read from it as much as
         * you can. Any data that remains on or after the value
         * of <code>position()</code> will be saved for the next
         * time an event is fired. In this way, you can defer
         * processing incomplete data until everything arrives.
         * <em>Be careful that you don't leave the buffer full</em>
         * or you won't be able to receive anything next time around
         * (unless you call {@link #setBufferSize} to resize buffer).</p>
         *
         * <p>You will get these events for both TCP and UDP incoming data.
         * Consider using the {@link Event#isTcp} and {@link Event#isUdp}
         * convenience methods for distinguishing between them.</p>
         *
         * <p>Example: You are receiving lines of text. The ByteBuffer
         * returned here contains one and a half lines of text.
         * When you realize this, you process the first line as you
         * like, but you leave this buffer's position at the beginning
         * of the second line. In this way, The beginning of the second
         * line will be the start of the buffer the next time around.</p>
         *
         * @param evt the shared event
         */
        public abstract void nioServerDataReceived( NioServer.Event evt );

        /**
         * Called when a connection is closed remotely. If you close the connection
         * somewhere in your own code, this event probably won't be fired.
         * @param evt the shared event
         */
        public abstract void nioServerConnectionClosed( NioServer.Event evt );


    }   // end inner static class Listener

    
    



/* ********                                                        ******** */
/* ********                                                        ******** */    
/* ********   S T A T I C   I N N E R   C L A S S   A D A P T E R  ******** */
/* ********                                                        ******** */
/* ********                                                        ******** */    
    



    /**
     * A helper class that implements all methods of the
     * {@link NioServer.Listener} interface with empty methods.
     * 
     * <p>This code is released into the Public Domain.
     * Since this is Public Domain, you don't need to worry about
     * licensing, and you can simply copy this NioServer.java file
     * to your own package and use it as you like. Enjoy.
     * Please consider leaving the following statement here in this code:</p>
     * 
     * <p><em>This <tt>NioServer</tt> class was copied to this project from its source as
     * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
     *
     * @author Robert Harder
     * @author rharder@users.sourceforge.net
     * @version 0.1
     * @see NioServer
     * @see Listener
     * @see Event
     */
    public class Adapter implements Listener {

        /**
         * Empty method.
         * @see Listener
         * @param evt the shared event
         */
        public void nioServerDataReceived(NioServer.Event evt) {}


        /**
         * Empty method.
         * @see Listener
         * @param evt the shared event
         */
        public void nioServerNewConnectionReceived(NioServer.Event evt) {}

        /**
         * Empty method.
         * @see Listener
         * @param evt the shared event
         */
        public void nioServerConnectionClosed(NioServer.Event evt) {}

    }   // end static inner class Adapter

    
/* ********                                                    ******** */
/* ********                                                    ******** */    
/* ********   S T A T I C   I N N E R   C L A S S   E V E N T  ******** */
/* ********                                                    ******** */
/* ********                                                    ******** */    
    
    

    /**
     * An event representing activity by a {@link NioServer}.
     * 
     * <p>This code is released into the Public Domain.
     * Since this is Public Domain, you don't need to worry about
     * licensing, and you can simply copy this NioServer.java file
     * to your own package and use it as you like. Enjoy.
     * Please consider leaving the following statement here in this code:</p>
     * 
     * <p><em>This <tt>NioServer</tt> class was copied to this project from its source as
     * found at <a href="http://iharder.net" target="_blank">iHarder.net</a>.</em></p>
     *
     * @author Robert Harder
     * @author rharder@users.sourceforge.net
     * @version 0.1
     * @see NioServer
     * @see Adapter
     * @see Listener
     */
    public static class Event extends java.util.EventObject {

        private final static long serialVersionUID = 1;

        /**
         * The key associated with this (reusable) event.
         * Use setKey(..) to change the key between firings.
         */
        private SelectionKey key;

        /**
         * The source address for incoming UDP datagrams.
         * The {@link #getRemoteSocketAddress} method
         * will return this value if data is from UDP.
         */
        private SocketAddress remoteAddress;

        /**
         * Creates a Event based on the given {@link NioServer}.
         * @param src the source of the event
         */
        public Event( NioServer src ){
            super(src);
        }

        /**
         * Returns the source of the event, a {@link NioServer}.
         * Shorthand for <tt>(NioServer)getSource()</tt>.
         * @return the server
         */
        public NioServer getNioServer(){
            return (NioServer)getSource();
        }

        /**
         * Shorthand for <tt>getNioServer().getState()</tt>.
         * @return the state of the server
         * @see NioServer.State
         */
        public NioServer.State getState(){
            return getNioServer().getState();
        }


        /**
         * Returns the SelectionKey associated with this event.
         *
         * @return the SelectionKey
         */
        public SelectionKey getKey(){
            return this.key;
        }

        /**
         * Sets the key for the event before a firing.
         * @param key the SelectionKey
         */
        protected void setKey(SelectionKey key){
            this.key = key;
        }

        /**
         * Sets the source address for an incoming UDP datagram.
         * The {@link #getRemoteSocketAddress} method
         * will return this value if data is from UDP.
         * @param addr the source address
         */
        protected void setRemoteAddress(SocketAddress addr){
            this.remoteAddress = addr;
        }


        /**
         * <p>Returns the {@link java.nio.ByteBuffer} that contains
         * the data for this connection. Read from it as much as
         * you can. Any data that remains on or after the value
         * of <code>position()</code> will be saved for the next
         * time an event is fired. In this way, you can defer
         * processing incomplete data until everything arrives.</p>
         *
         * <p>Example: You are receiving lines of text. The ByteBuffer
         * returned here contains one and a half lines of text.
         * When you realize this, you process the first line as you
         * like, but you leave this buffer's position at the beginning
         * of the second line. In this way, The beginning of the second
         * line will be the start of the buffer the next time around.</p>
         * @return buffer with the data
         */
        public ByteBuffer getBuffer(){
            return (ByteBuffer)this.key.attachment();
        }


        /**
         * <p>Returns the local address/port to which this connection
         * is bound. That is, if you are listening on port 80, then
         * this might return something like an InetSocketAddress
         * (probably) that indicated /127.0.0.1:80.</p>
         * <p>This is
         * essentially a convenience method for returning the same-name
         * methods from the key's channel after checking the type
         * of channel (SocketChannel or DatagramChannel).</p>
         *
         * @return local address that server is bound to for this connection
         */
        public SocketAddress getLocalSocketAddress(){
            SocketAddress addr = null;
            if( this.key != null ){
                SelectableChannel sc = this.key.channel();
                if( sc instanceof SocketChannel ){
                    addr = ((SocketChannel)sc).socket().getLocalSocketAddress();
                } else if( sc instanceof DatagramChannel ){
                    addr = ((DatagramChannel)sc).socket().getLocalSocketAddress();
                }
            }
            return addr;
        }


        /**
         * <p>Returns the address of the endpoint this socket is
         * connected to, or null if it is unconnected. </p>
         * <p>This is
         * essentially a convenience method for returning the same-name
         * methods from the key's channel after checking the type
         * of channel (SocketChannel or DatagramChannel).</p>
         *
         * <p>For UDP datagrams, use the {@link #getSourceAddress}
         * method to get the source of the datagram.</p>
         *
         * @return remote address from which connection came
         */
        public SocketAddress getRemoteSocketAddress(){
            SocketAddress addr = null;
            if( this.key != null ){
                SelectableChannel sc = this.key.channel();
                if( sc instanceof SocketChannel ){
                    addr = ((SocketChannel)sc).socket().getRemoteSocketAddress();
                } else if( sc instanceof DatagramChannel ){
                    addr = this.remoteAddress;
                }
            }
            return addr;
        }


        /**
         * Convenience method for checking
         * <code>getKey().channel() instanceof SocketChannel</code>.
         * @return true if a TCP connection
         */
        public boolean isTcp(){
            return this.key.channel() instanceof SocketChannel;
        }


        /**
         * Convenience method for checking
         * <code>getKey().channel() instanceof DatagramChannel</code>.
         * @return true if a UDP connection
         */
        public boolean isUdp(){
            return this.key.channel() instanceof DatagramChannel;
        }


    }   // end static inner class Event

    

}   // end class NioServer
