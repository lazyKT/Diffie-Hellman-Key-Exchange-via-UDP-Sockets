/** 
 * This is just a testing and playing around with Sha-1 Hash,
 * which is required for my CS Assignment
 * In real world, do not ever use SHA-1 as a hashing technique.
 * It is not secure anymore.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.*;
import java.net.*;


class Host {

  // Port Number
  private static int PORT = 4455;


  // clean up before exiing the program
  // closing streams and terminating sockets
  private static void cleanUp (PrintStream printStream, BufferedReader kb_bufferedReader, BufferedReader r_bufferedReader, Socket socket, ServerSocket serversocket) {

    try {
      // close all the streams and socket
      System.out.println("Terminating Socket Connections ...");
      r_bufferedReader.close();
      kb_bufferedReader.close();
      printStream.close();
      socket.close();
      serversocket.close();

      System.out.println("Exiting Program ...");
      // Exit the program
      System.exit(0);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public static void main(String[] args) throws Exception {
    
    // instantiate Custom Utility class
    Utility utility = new Utility();

    // set up
    System.out.println("Setting up new server ...");
    utility.hostSetUp();

    List<String> secret_values = new ArrayList<String>();
    System.out.println("Reading Configuration ...");
    secret_values = utility.readFromFile();

    if (secret_values.size() < 3)
      throw new RuntimeException("Error Reading Config From File!!");

    String hashed_pwd = secret_values.get(0);
    BigInteger P = new BigInteger(secret_values.get(1));
    BigInteger G = new BigInteger(secret_values.get(2));

    System.out.println("Creating New Socket Connection ...");
    ServerSocket serversocket = new ServerSocket(PORT);
    Socket socket = serversocket.accept();
    System.out.printf("Socket Connection Established. Listening at PORT:%d ...\n", PORT);

    // read buffer string from client
    PrintStream printStream = new PrintStream(socket.getOutputStream());

    // read from keyborad
    BufferedReader kb_bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    
    // receive buffer string from client
    BufferedReader r_bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    while(true) {
      String r_message, s_message = "";

      // read/send message from/to the client
      while ( (r_message = r_bufferedReader.readLine()) != null) {
        System.out.println("Client : " + r_message);

        s_message = kb_bufferedReader.readLine();
        if (s_message == null)
          System.out.println("Receiving NULL Message. Breaking the loop ...");
        // send message to client
        printStream.println("Server : " + s_message);

        if ((s_message.trim()).equals(".exit()"))
          cleanUp(printStream, kb_bufferedReader, r_bufferedReader, socket, serversocket);
      }
    
    }
  }
}