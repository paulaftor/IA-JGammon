package jgam.test.net;
import jgam.game.BoardSnapshot;
import jgam.net.JGammonConnection;
import junit.framework.TestCase;

/**
 * <p>Title: JGam - Java Backgammon</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Mattias Ulbrich
 * @version 1.0
 */
public class TestJGammonConnection extends TestCase {
    private JGammonConnection server = null;
    private JGammonConnection client = null;

    protected void setUp() throws Exception {
        super.setUp();
        testJGammonConnection();
    }

    protected void tearDown() throws Exception {
        if(server != null) {
            server.close("Closed server");
            server = null;
        }
        if(client != null) {
            client.close("Closed client");
            client = null;
        }
        super.tearDown();
    }

    public void testJGammonConnection() throws  Exception {
        tearDown();
        new Thread() {
            public void run() {
                try {
                    server = new JGammonConnection(12345, "Server", BoardSnapshot.INITIAL_SETUP, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(1000);
        client = new JGammonConnection("localhost", 12345, "Client");
    }

    public void testGetName() {
        String s = client.getRemoteName();
        String c = server.getRemoteName();
        assertEquals("Server", s);
        assertEquals("Client", c);
    }
}
