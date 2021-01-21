import java.io.*;
import java.util.*;
import java.math.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;
import java.nio.file.*;
import java.nio.charset.*;
import javax.crypto.spec.*;  


public class SetUp {
    
    private static int PORT = 4455;
    private final String CLIENT_TEXT_COLOR = "\u001B[36m"; // cyan
    private final String DEFAULT_COLOR = "\u001B[0m"; // default color
    private final String secret = "ThisIsSecret123";
    private ServerSocket serverSocket;
    private Socket socket;
    private DataOutputStream outputStream;
    private BufferedReader kb_bufferedReader, r_bufferedReader;
    private DataInputStream inputStream;
    private BigInteger P, G, X, Y, sharedKeyA;
    private String hashed_pwd;
    private Cipher cipher;
    private SecretKey secretKey;


    public SetUp() throws Exception {

        this.showText("\nCreating New Server ... /", "info");
        Random random = new Random(System.currentTimeMillis());
        
        this.showText("Loading Configuration ... |", "info");
        this.P = BigInteger.probablePrime(128, random);
        this.G = BigInteger.probablePrime(128, random);
        this.hashed_pwd = sha1Hash(this.secret);

        this.saveToFile(Arrays.asList(this.hashed_pwd, (this.P).toString(), (this.G).toString()));

    }

    // // run in background thread
    // public void run () {
    //     try {

    //         while (true) {
    //             String receiveMessage = this.receiveAndDecryptMessage();
    //             this.showText(new String("Client : " + receiveMessage), "message");

    //             if (receiveMessage.equals("exit"))
    //                 break;
    //         }

    //     }   
    //     catch (Exception e) {
    //         throw new RuntimeException(e);
    //     }
    // }


    /**
     * Read Configurations from file
     */
    public void readConfigs () {
        this.showText("Reading Configurations ....\\", "info");
        try {
            Path file = Paths.get("secret.txt");
            List<String> lines = Files.readAllLines(file);

            this.hashed_pwd = lines.get(0);
            this.P = new BigInteger(lines.get(1));
            this.G = new BigInteger(lines.get(2));
        
        }
            catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }


    /**
     * Initialise socket connection
     * 
     * @return void
     */
    public void initConnection() {
        
        try {
            this.showText("\nCreating New Socket Connection ... \\\n", "info");
            this.serverSocket = new ServerSocket(4455);
            System.out.println("Waiting for the client to connect ... |\n");
            this.socket = serverSocket.accept();
            this.showText(String.format("Socket Connection Established. Listening at PORT:%d\n", PORT), "info");

            this.outputStream = new DataOutputStream(this.socket.getOutputStream());
            this.kb_bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            this.inputStream = new DataInputStream(this.socket.getInputStream());
            // receive and read buffer string from client
            this.r_bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            this.cipher = Cipher.getInstance("ARCFOUR");
            // generate secretkey for first handshake session
            generateSecretKey(this.hashed_pwd);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public BufferedReader getKeyBoardBufferedReader() { return this.kb_bufferedReader; }
    public DataInputStream getInputStream() { return this.inputStream; }


    // perform handshake to establish secure channel
    public void performHandShake() throws Exception {
        
        String r_message = this.r_bufferedReader.readLine();

        if (r_message != null && (r_message.toLowerCase()).equals("bob") ) {
            // perform handshake
            System.out.println("Client Username : " + r_message);
            System.out.println("\nPerforming first handshake .... /\n");

            Random rand = new Random(System.currentTimeMillis());
            int a = rand.nextInt(999);
            this.X = this.findModulo(this.G, a);
            String firstHandShakeStr = String.format("%s,%s,%s",  (this.P).toString(), (this.G).toString(), X.toString());
            
            // send P, G, X 
            this.sendEncryptedMessage(firstHandShakeStr);
            
            // receive Y
            String decrypted_Y = this.receiveAndDecryptMessage();
            this.Y = new BigInteger(decrypted_Y);
            System.out.println("First HandShake Success!\n");

            this.sharedKeyA = this.findModulo(Y, a);

            // generate secret key using Shared Key
            generateSecretKey((this.sharedKeyA).toString());

            long nounceA = System.nanoTime();
            System.out.println("Performing Second HandShake ... |\n");

            // send nounceA
            sendEncryptedMessage(String.valueOf(nounceA));
            
            String nounces[] = this.receiveAndDecryptMessage().split(",");
            if (nounces.length < 2)
                throw new RuntimeException("Error receiving NounceA + 1");
            
            long noucneA_plusOne = Long.parseLong(nounces[0]);
            long nounceB = Long.parseLong(nounces[1]);
            // System.out.println(x);
            if ( (nounceA + 1) != noucneA_plusOne ) {
                // login failed and terminate connection
                sendEncryptedMessage("Login Failed");
                System.out.println("\nClient Login Failed!");
                System.out.println("\nPress Enter to Exit!\n");
                (this.kb_bufferedReader).readLine();
                this.terminateConnection();
            }
            System.out.println("Second HandShake Success!\n");
            System.out.println("Performing Final HandShake ... /\n");
            // send nounceB
            sendEncryptedMessage(String.valueOf(nounceB + 1));
            
            int finalHandShakeResult = (this.inputStream).readInt();

            if (finalHandShakeResult == 200) {
                System.out.println("Final HandShake Success!\n");
                System.out.println("Secure Channel Established! :)\nYou can start to send message over secure channel.\nTry to type \"Hello Client\"\n");
            }
        }

    }


    /**
     * Hash Password using SHA-1 algorithm: Length - 64bits
     * DO NOT USE SHA-1 IN REAL WORLD CASE.
     * @param pwd
     * @return String
     */
    private String sha1Hash (String pwd) {

        String hashed_pwd = "";

        try {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        // convert String to bytes arry
        byte[] pwd_bytes = md.digest(pwd.getBytes());

        // convert byte array to signum value
        BigInteger signum_val = new BigInteger(1, pwd_bytes);

        hashed_pwd = signum_val.toString();

        // fill "0"s to get 64-bit length
        while ( hashed_pwd.length() < 64 )
            hashed_pwd = "0" + hashed_pwd;

        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return hashed_pwd;
    }

    // save password, P and G values to File
    private void saveToFile (List<String> lines) {

        try {
        Path filepath = Paths.get("secret.txt");
        Files.write(filepath, lines, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
        throw new RuntimeException(e);
        }
    }


    /** 
     * find mod value
     * @return BigInteger
     */
    private BigInteger findModulo(BigInteger x, int m) {
        
        BigInteger ex = x.pow(m);

        return ex.mod(this.P);
    }


    public String receiveAndDecryptMessage() throws Exception {

        int message_len = (this.inputStream).readInt();
        byte[] encrypted_message = new byte[message_len];
        (this.inputStream).readFully(encrypted_message, 0, encrypted_message.length);

        return this.decrypt(encrypted_message);
    }


    public void sendEncryptedMessage(String message) throws Exception {

        byte[] encrypted_message = encrypt(message);
        //System.out.println("Encrypted Message : " + encrypted_message);

        (this.outputStream).writeInt(message.length());
        (this.outputStream).write(encrypted_message);
    }


    /**
     * Generate Secret Key
     * @return SecretKey
     */
    private void generateSecretKey(String secret) throws Exception {
        // if secret doesn't long 64-bit, padd with zero at the beginning
        // System.out.println("Secert Length : " + secret.length());
        while (secret.length() < 64)
            secret = "0" + secret;
        
        byte[] secret_bytes = Base64.getDecoder().decode(secret);
        // System.out.println("Secret key length " + secret_bytes.length);
        this.secretKey = new SecretKeySpec(secret_bytes, 0, secret_bytes.length, "ARCFOUR");
    }


    /**
     * Encrypt Message using RC-4
     * @param message
     * @param key
     * @param cipher
     * @return byte[]
     * @throws Exception
     */
    private byte[] encrypt(String message) throws Exception {

        this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
        byte[] message_bytes = message.getBytes();
        
        return cipher.doFinal(message_bytes);
    }


    /**
     * Decrypt Message
     * @return String
     */
    private String decrypt(byte[] encrypted_bytes) throws Exception {

        this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
        byte[] decrypted_bytes = cipher.doFinal(encrypted_bytes);

        return new String(decrypted_bytes);
    }


    // text decoration
    private void showText(String message, String type) {
        if (type.equals("info")) 
            System.out.println(this.DEFAULT_COLOR + message + this.DEFAULT_COLOR);
        else 
        System.out.println(this.CLIENT_TEXT_COLOR + message + this.CLIENT_TEXT_COLOR);
    }


    public void terminateConnection () throws Exception {
        System.out.println("\nTerminating Socket connections ...");
        this.outputStream.close();
        this.r_bufferedReader.close();
        this.kb_bufferedReader.close();
        this.inputStream.close();
        this.socket.close();
        this.serverSocket.close();
        System.out.println("Exiting Program ...");
        System.exit(0);
    }

}
