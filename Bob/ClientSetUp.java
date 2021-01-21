import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;

/** Clinet Socket Connection Setup */

public class ClientSetUp {

    private final String RECEIVE_TEXT_COLOR = "\u001B[36m"; // cyan
    private final String DEFAULT_COLOR = "\u001B[0m"; // default color
    private final String ERROR_TEXT_COLOR = "\u001B[31m";
    private BigInteger P, G, X, Y, sharedKeyB;
    private Socket socket;
    private BufferedReader kb_bufferedReader; // read from keyboard input
    private DataInputStream inputStream; // read from socket
    private DataOutputStream outputStream; // write to socket
    private Cipher cipher;
    private SecretKey secretKey;

    public ClientSetUp(int Port) throws Exception {

        System.out.println("\nTrying to connect to the Server at PORT : " + Port + "...\\\n");
        this.socket = new Socket("127.0.0.1",Port);
        System.out.println("Connected to the Host at PORT:" + Port);

        this.cipher = Cipher.getInstance("ARCFOUR");
        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
        this.kb_bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        this.inputStream = new DataInputStream(this.socket.getInputStream());

        // generatSecretKey(sha1Hash(secret));
    }


    public BufferedReader getBufferedReader() { return this.kb_bufferedReader; }
    public DataInputStream getInputStream() { return this.inputStream; }

    public void performHandShake() throws Exception {

        System.out.printf("\nUsername : ");
        String s_string = (this.kb_bufferedReader).readLine();
        (this.outputStream).writeBytes(s_string + "\n");

        // recive encrypted text from Host
        System.out.println("\nAuthenticating...\nPerforming First HandShake ...");
        
        int message_len = (this.inputStream).readInt();
        byte[] encrypted_message = new byte[message_len];
        (this.inputStream).readFully(encrypted_message, 0, encrypted_message.length);

        // first handshake: Password Authentication
        this.performFirstHandShake(encrypted_message);

        // generating b
        Random random = new Random(System.currentTimeMillis());
        int b = random.nextInt(999);
        this.Y = this.findModulo(this.G, b);
        this.sharedKeyB = this.findModulo(X, b); // generate shared key
        
        // send Y to Host
        String strY = (this.Y).toString();
        this.sendEncryptedMessage(strY);

        // generate new Secret Key using Shared Key
        generatSecretKey(this.sharedKeyB.toString());
        
        System.out.println("Performing Second HandShake ...");
        long nounceA = Long.parseLong(this.receiveAndDecryptMessage());

        // generate nounceB
        long nounceB = System.nanoTime();
        // send nounceA + 1 and nounce B
        String secondHandShakeStr = String.format("%s,%s", String.valueOf(nounceA + 1), nounceB);
        this.sendEncryptedMessage(secondHandShakeStr);

        String secondHandShakeResult = this.receiveAndDecryptMessage();
        if (secondHandShakeResult.equals("Login Failed")) {
            this.outputStream.writeInt(403);
            System.out.println("\nHost : " + secondHandShakeResult);
            this.terminateConnection();
            return;
        }
        // System.out.println("Nounce B : " + nounceB);
        // System.out.println("Second HandShake Result " + secondHandShakeResult);
        System.out.println("Second HandShake Success!");
        System.out.println("Performing Final HandShake ... ");
        if (Long.parseLong(secondHandShakeResult) != (nounceB + 1)) {
            System.out.println("\nCannot Establish Secure Channel with Host \n");
            this.terminateConnection();
        }

        System.out.println("Final HandShake Success!\n");
        this.outputStream.writeInt(200); // response to server or Host
        System.out.println("Secure Channel Established ! :D\n\nYou can start to send message over secure channel.\nTry to type \"Hello Server\"\n");
    }


    private void performFirstHandShake(byte[] encrypted_message) throws Exception {

        String[] datasetPGX = new String[3];
        int retryCount = 0;
        
        do {
            retryCount ++;
            if (retryCount != 1) 
                System.out.println(ERROR_TEXT_COLOR + "Wrong Password Entered ..." + ERROR_TEXT_COLOR);

            if (retryCount > 3) {
                System.out.println("Maximum Retry Count Exceeded.");
                (this.outputStream).writeInt(403);
                this.terminateConnection();
            }
    
            String password = this.readPassword();
            this.generatSecretKey(sha1Hash(password));
            datasetPGX = this.decryptMessage(encrypted_message).split(",");
        }
        while(datasetPGX.length < 3);

        this.P = new BigInteger(datasetPGX[0]);
        this.G = new BigInteger(datasetPGX[1]);
        this.X = new BigInteger(datasetPGX[2]);
        this.outputStream.writeInt(200);
        System.out.println(this.DEFAULT_COLOR + "\nFirst HandShake Successful!" + this.DEFAULT_COLOR);
    }


    // read password from console
    private String readPassword() {
        Console console = System.console();
        System.out.println(this.DEFAULT_COLOR);
        if (console == null ) {
            System.out.println("Cannot get console instance ... ");
            this.terminateConnection();
        }

        char[] pwdArray = console.readPassword("Password : ");
        return new String(pwdArray);
    }


    // send message after successful handshake
    public void sendMessage(String message) throws Exception{

        String toHash = new String(this.sharedKeyB.toString() + message + this.sharedKeyB.toString());
        String hash = sha1Hash(toHash);

        // System.out.println("hash(H) : " + hash);
        String cipherText = String.format("%s@KMK@%s", message, hash);

        sendEncryptedMessage(cipherText);

    }

    // receive message after successful handshake
    public String receiveMessage() throws Exception {

        String cipherMessage = receiveAndDecryptMessage();

        String r_message = cipherMessage.split("@KMK@")[0];
        String hashA = cipherMessage.split("@KMK@")[1];

        String toHash = new String((this.sharedKeyB).toString()+r_message+(this.sharedKeyB).toString());
        String hashB = sha1Hash(toHash);

        if (hashB.equals(hashA))
            return r_message;
        
        return null;
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


    private String sha1Hash(String secret) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] secret_bytes = messageDigest.digest(secret.getBytes());
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

    
    // text decoration
    private void showText(String message, String type) {
        if (type.equals("info")) 
            System.out.println(this.DEFAULT_COLOR + message + this.DEFAULT_COLOR);
        else 
        System.out.println(this.RECEIVE_TEXT_COLOR + message + this.RECEIVE_TEXT_COLOR);
    }


    public void terminateConnection() {
        try {
            // close connection
            System.out.println("\nTerminating Socket connections ...");
            (this.kb_bufferedReader).close();
            (this.inputStream).close();
            (this.outputStream).close();
            (this.socket).close();
            System.out.println("Exiting Program ...");
            System.exit(0);
        }
        catch (Exception e) {
            throw new RuntimeException("Error closing socket connection!");
        }
    }
}