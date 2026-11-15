package socktalk.client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Dependency-free tests: run this class's main method with JDK 21.
 * Uses only ephemeral loopback sockets; no SockTalk server is required.
 */
public final class ConnectionManagerTest {
    private static final int TIMEOUT_MILLIS = 2000;

    public static void main(String[] args) throws Exception {
        constructionDoesNotOpenAConnection();
        connectionDoesNotRequireAnObjectStreamHandshake();
        readsLineDelimitedTextAndEndOfStream();
        writesPlainTextWithoutSerializationHeaders();
        rejectsPortsOutsideTheTcpRange();
        System.out.println("ConnectionManagerTest: 5 tests passed");
    }

    private static void constructionDoesNotOpenAConnection() throws Exception {
        ConnectionManager manager = new ConnectionManager("127.0.0.1", 54321);
        check(field(manager, "socket", Socket.class) == null, "Construction must not open a socket");
        check(field(manager, "in", BufferedReader.class) == null, "Input must be initialized on connect");
        check(field(manager, "out", PrintWriter.class) == null, "Output must be initialized on connect");
    }

    private static void connectionDoesNotRequireAnObjectStreamHandshake() throws Exception {
        withConnection((manager, peer) -> {
            Socket client = field(manager, "socket", Socket.class);
            check(client.isConnected(), "Client must connect to the configured endpoint");
            check(client.getPort() == peer.getLocalPort(), "Client must use the configured port");
            check(field(manager, "in", BufferedReader.class) != null, "Text input must be initialized");
            check(field(manager, "out", PrintWriter.class) != null, "Text output must be initialized");
            // The peer has sent nothing: ObjectInputStream initialization would block here.
        });
    }

    private static void readsLineDelimitedTextAndEndOfStream() throws Exception {
        withConnection((manager, peer) -> {
            PrintWriter serverOutput = new PrintWriter(new OutputStreamWriter(peer.getOutputStream()));
            serverOutput.print("LOGIN_OK|test1|개발\n\nNOTICE|first\r\nNOTICE|last");
            serverOutput.flush();
            peer.shutdownOutput();

            BufferedReader input = field(manager, "in", BufferedReader.class);
            equal("LOGIN_OK|test1|개발", input.readLine(), "Unicode protocol text must survive decoding");
            equal("", input.readLine(), "An empty line must remain distinct from EOF");
            equal("NOTICE|first", input.readLine(), "CRLF must delimit a single line");
            equal("NOTICE|last", input.readLine(), "EOF must preserve a final unterminated line");
            equal(null, input.readLine(), "A graceful peer shutdown must produce EOF");
        });
    }

    private static void writesPlainTextWithoutSerializationHeaders() throws Exception {
        withConnection((manager, peer) -> {
            // send() is currently a stub. Exercise only the text stream initialized by connectToServer().
            PrintWriter output = field(manager, "out", PrintWriter.class);
            output.print("LOGIN|test1|비밀번호\n\nLOGOUT\n");
            output.flush();
            check(!output.checkError(), "Writing to the connected peer must succeed");

            BufferedReader serverInput = new BufferedReader(new InputStreamReader(peer.getInputStream()));
            equal("LOGIN|test1|비밀번호", serverInput.readLine(), "Output must be plain protocol text");
            equal("", serverInput.readLine(), "Empty output lines must be preserved");
            equal("LOGOUT", serverInput.readLine(), "Successive messages must remain separate lines");
        });
    }

    private static void rejectsPortsOutsideTheTcpRange() throws Exception {
        for (int port : new int[] {-1, 65536}) {
            ConnectionManager manager = new ConnectionManager("127.0.0.1", port);
            try {
                try {
                    manager.connectToServer();
                    throw new AssertionError("Expected IllegalArgumentException for port " + port);
                } catch (IllegalArgumentException expected) {
                    check(field(manager, "in", BufferedReader.class) == null,
                            "An invalid endpoint must not initialize input");
                    check(field(manager, "out", PrintWriter.class) == null,
                            "An invalid endpoint must not initialize output");
                }
            } finally {
                closeSocket(manager);
            }
        }
    }

    private static void withConnection(ConnectionCheck test) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ConnectionManager manager = null;
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            server.setSoTimeout(TIMEOUT_MILLIS);
            manager = new ConnectionManager("127.0.0.1", server.getLocalPort());
            ConnectionManager client = manager;
            Future<Void> connecting = executor.submit(() -> {
                client.connectToServer();
                return null;
            });
            try (Socket peer = server.accept()) {
                peer.setSoTimeout(TIMEOUT_MILLIS);
                connecting.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                field(manager, "socket", Socket.class).setSoTimeout(TIMEOUT_MILLIS);
                test.check(manager, peer);
            }
        } finally {
            try {
                if (manager != null) {
                    // disconnect() is a stub, so the fixture owns cleanup directly.
                    closeSocket(manager);
                }
            } finally {
                executor.shutdownNow();
                check(executor.awaitTermination(5, TimeUnit.SECONDS), "Connection worker must terminate");
            }
        }
    }

    private static void closeSocket(ConnectionManager manager) throws ReflectiveOperationException, IOException {
        Socket socket = field(manager, "socket", Socket.class);
        if (socket != null) {
            socket.close();
        }
    }

    // There is no public receive API yet. Keep reflection isolated until one is implemented.
    private static <T> T field(ConnectionManager manager, String name, Class<T> type)
            throws ReflectiveOperationException {
        Field field = ConnectionManager.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(manager));
    }

    private static void equal(String expected, String actual, String message) {
        check(java.util.Objects.equals(expected, actual), message + ": expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface ConnectionCheck {
        void check(ConnectionManager manager, Socket peer) throws Exception;
    }
}
