import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;


public class ClientSetUp {

    private final String secret = "ThisIsSecret123";
    private BigInteger P, G, X, Y, sharedKeyB;
    private Socket socket;
    private BufferedReader kb_bufferedReader;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Cipher cipher;
    private SecretKey secretKey;

    public ClientSetUp(int Port) throws Exception {

        System.out.println("\nTrying to connect to the Server at PORT : " + Port + "...\\\n");
        this.socket = new Socket(InetAddress.getLocalHost(), Port);
        System.out.println("Connected to the Host at PORT:" + Port);

        this.cipher = Cipher.getInstance("ARCFOUR");
    }


    public void init() throws Exception{
        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
        this.kb_bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        this.inputStream = new DataInputStream(this.socket.getInputStream());

        generatSecretKey(sha1Hash());
    }


    public BufferedReader getBufferedReader() { return this.kb_bufferedReader; }
    public DataInputStream getInputStream() { return this.inputStream; }

    public void performHandShake() throws Exception {

        System.out.printf("\nUsername : ");
        String s_string = (this.kb_bufferedReader).readLine();
        (this.outputStream).writeBytes(s_string + "\n");

        // recive encrypted text from Host
        System.out.println("\nPerforming First HandShake ...\\");
        
        String decrypted_PGX = this.receiveAndDecryptMessage();

        // Extract P, G and X from decrypted message
        String[] dataSetPGX = decrypted_PGX.split(",");
        if (dataSetPGX.length < 3)
            throw new RuntimeException("Failed to get data during First HandShake .!");
        this.P = new BigInteger(dataSetPGX[0]);
        this.G = new BigInteger(dataSetPGX[1]);
        this.X = new BigInteger(dataSetPGX[2]);

        // generating b
        Random random = new Random(System.currentTimeMillis());
        int b = random.nextInt(999);
        this.Y = this.findModulo(this.G, b);
        this.sharedKeyB = this.findModulo(X, b); // generate shared key
        
        // send Y to Host
        String strY = (this.Y).toString();
        this.sendEncryptedMessage(strY);
        System.out.println("First HandShake Success!\n");

        // generate new Secret Key using Shared Key
        generatSecretKey(this.sharedKeyB.toString());
        
        System.out.println("Performing Second HandShake ... |");
        long nounceA = Long.parseLong(this.receiveAndDecryptMessage());

        // generate nounceB
        long nounceB = System.nanoTime();
        // send nounceA + 1 and nounce B
        String secondHandShakeStr = String.format("%s,%s", String.valueOf(nounceA + 1), nounceB);
        this.sendEncryptedMessage(secondHandShakeStr);

        String secondHandShakeResult = this.receiveAndDecryptMessage();
        if (secondHandShakeResult.equals("Login Failed")) {
            System.out.println("\nHost : " + secondHandShakeResult);
            this.terminateConnection();
            return;
        }
        // System.out.println("Nounce B : " + nounceB);
        // System.out.println("Second HandShake Result " + secondHandShakeResult);
        System.out.println("Second HandShake Success!\n");
        System.out.println("Performing Final HandShake ... /");
        if (Long.parseLong(secondHandShakeResult) != (nounceB + 1)) {
            (this.outputStream).writeInt(403);
            System.out.println("\nCannot Establish Secure Channel with Host \n");
            this.terminateConnection();
        }

        System.out.println("Final HandShake Success!\n");
        (this.outputStream).writeInt(200); // response to server or Host
        System.out.println("Secure Channel Established ! :D\n");
    }


    public String receiveAndDecryptMessage() throws Exception {
        int message_len = (this.inputStream).readInt();
        byte[] encrypted_message = new byte[message_len];
        (this.inputStream).readFully(encrypted_message, 0, encrypted_message.length);
        
        //System.out.println("Encrypted Message : " + encrypted_message);
        return this.decryptMessage(encrypted_message);
    }


    public void sendEncryptedMessage (String message) throws Exception {

        (this.outputStream).writeInt(message.length());

        byte[] encrypted_message = this.encryptMessage(message);

        (this.outputStream).write(encrypted_message);
    } 


    private String sha1Hash() {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] secret_bytes = messageDigest.digest(this.secret.getBytes());
            BigInteger signum_val = new BigInteger(1, secret_bytes);

            String hashed_secret = signum_val.toString();

            // padd "0"s to get 64-bit length
            while ( hashed_secret.length() < 64 )
                hashed_secret = "0" + hashed_secret;

            return hashed_secret;
        }   
        catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private void generatSecretKey(String secret) throws Exception {

        // if secret doesn't long 64-bit, padd with zero at the beginning
        while (secret.length() < 64)
            secret = "0" + secret;

        byte[] hashed_bytes = Base64.getDecoder().decode(secret.getBytes());

        this.secretKey = new SecretKeySpec( hashed_bytes, 0, hashed_bytes.length, "ARCFOUR" );
    }


    private byte[] encryptMessage(String message) throws Exception {
        
        this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
        byte[] message_byte = message.getBytes();

        return cipher.doFinal(message_byte);
    }


    private String decryptMessage(byte[] encrypted_message) throws Exception {

        this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
        byte[] decrypted_bytes = cipher.doFinal(encrypted_message);

        return new String(decrypted_bytes);
    }


    /** 
     * find mod value
     * @return BigInteger
     */
    private BigInteger findModulo(BigInteger x, int m) {
        
        BigInteger ex = x.pow(m);

        return ex.mod(this.P);
    }


    public void terminateConnection() {
        try {
            // close connection
            System.out.println("Terminating Socket connections ...");
            (this.kb_bufferedReader).close();
            (this.inputStream).close();
            (this.outputStream).close();
            (this.socket).close();
            System.out.println("Exiting Program ...");
            System.exit(1);
        }
        catch (Exception e) {
            throw new RuntimeException("Error closing socket connection!");
        }
    }
}