/** Client Program */
import java.util.*;
import java.io.*;
import java.math.*;
import java.net.*;


class Client {


  // close connection upon exiting program
  // @return void
  private static void terminateConnection (DataOutputStream opts, BufferedReader kb_bufferedReader, 
    BufferedReader r_bufferedReader, Socket socket) {

    try {
      // close connection
      System.out.println("Terminating Socket connections ...");
      opts.close();
      r_bufferedReader.close();
      kb_bufferedReader.close();
      socket.close();
      System.out.println("Exiting Program ...");
    }
    catch (Exception e) {
      throw new RuntimeException("Error Terminating Socket Connections!");
    }

  }


  public static void main (String args[]) throws Exception {

    int PORT = 0;

    if (args.length < 1) {
      System.err.println("Usage : java Client <port-number>");
      System.exit(0);
    }

    PORT = Integer.parseInt(args[0]);

    /** 
     * Note that: the program will throw ConnectException 
     * if there is not server listening at given PORT number 
     */
    Socket socket = new Socket(InetAddress.getLocalHost(), PORT);
    System.out.println("Connected to Server at PORT:" + PORT);

    // send data to server
    DataOutputStream opts = new DataOutputStream(socket.getOutputStream());

    // receive incoming message from server
    BufferedReader r_bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    BufferedReader kb_bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    String s_message, r_message;


    System.out.printf("Enter Username : ");

    while (true) {
      
      s_message = kb_bufferedReader.readLine(); 
      
      // send message to server
      opts.writeBytes(s_message + "\n");

      if (s_message.equals(".exit()"))
        break;


      // receive message from server
      r_message = r_bufferedReader.readLine();
      System.out.println("Server : " + r_message);

      // if server terminates the Program.
      // if server Program is terminated, the client program will be terminated as well.
      if (r_message.equals(".exit()")) 
        break;

    }

    // terminate socket connections
    terminateConnection(opts, kb_bufferedReader, r_bufferedReader, socket);

  }
}
