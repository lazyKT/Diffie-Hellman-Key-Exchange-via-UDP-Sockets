import java.io.*;
import java.math.*;
import java.net.*;


public class SetUp {
    
    private static int PORT = 4455;
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintStream printStream;
    private BufferedReader kb_bufferedReader, r_bufferedReader;
    private BigInteger P, G;
    private String hashed_pwd;


    public SetUp(String hashed_pwd, BigInteger P, BigInteger G) throws Exception {

        this.hashed_pwd = hashed_pwd;
        this.G = G;
        this.P = P;

        System.out.println("Creating New Socket Connection ...");
        this.serverSocket = new ServerSocket(4455);
        System.out.println("Waiting for the client to connect ... ");
        this.socket = serverSocket.accept();
        System.out.printf("Socket Connection Established. Listening at PORT:%d ...\n", PORT);

        this.printStream = new PrintStream(this.socket.getOutputStream());
        this.kb_bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        // receive buffer string from client
        this.r_bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }


    public String getHashed_Pwd() { return this.hashed_pwd; }

    public BigInteger getP() { return this.P; }

    public BigInteger getG() { return this.G; }

    public PrintStream getPrintStream() { return this.printStream; }

    public BufferedReader getKeyBoardBufferedReader() { return this.kb_bufferedReader; }

    public BufferedReader getReceiveBufferedReader() { return this.r_bufferedReader; }


    // perform handshake to establish secure channel
    public void performHandShake() {
        
    }


    public void terminateConnection () throws Exception {
        this.printStream.close();
        this.r_bufferedReader.close();
        this.kb_bufferedReader.close();
        this.socket.close();
        this.serverSocket.close();
    }

}