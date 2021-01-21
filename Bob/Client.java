/** Client Program */
import java.util.*;
import java.io.*;
import java.math.*;
import java.net.*;


class Client {


  private static final String RECEIVE_TEXT_COLOR = "\u001B[36m"; // cyan
  private static final String DEFAULT_COLOR = "\u001B[0m"; // default color
  public static final String SEND_TEXT_COLOR = "\u001B[35m"; // purple
  private static final String ERROR_TEXT_COLOR = "\u001B[31m";

  public static void main (String args[]) throws Exception {

    int PORT = 0;

    if (args.length < 1) {
      System.err.println("Usage : java Client <port-number>");
      System.exit(0);
    }

    PORT = Integer.parseInt(args[0]);

    ClientSetUp setUp = new ClientSetUp(PORT);
    BufferedReader kb_bufferedReader = setUp.getBufferedReader();
    DataInputStream inputStream = setUp.getInputStream();
    
    setUp.performHandShake();

    System.out.print(SEND_TEXT_COLOR + "\n" + SEND_TEXT_COLOR);
    while (true) {
      String s_message = ""; 
      String r_message = "";
      
      while ( !kb_bufferedReader.ready() ) {

        while ( inputStream.available() > 0 ) {

          r_message = setUp.receiveMessage();

          if (r_message != null) {
            System.out.print(RECEIVE_TEXT_COLOR + "Host : " + r_message + RECEIVE_TEXT_COLOR);
            System.out.print(SEND_TEXT_COLOR + "\n\n" + SEND_TEXT_COLOR);
            
            if (r_message.equals("exit"))
              setUp.terminateConnection();
          }
          else {
            // message rejected as the hashA and hashB aren't same
            System.out.println(ERROR_TEXT_COLOR + "MESSAGE REJECTED" + ERROR_TEXT_COLOR);
            System.out.println(SEND_TEXT_COLOR);
          }
          
        }
      }

      if (kb_bufferedReader.ready()) {
        //System.out.println("Client : ");
        s_message = kb_bufferedReader.readLine();
        setUp.sendMessage(s_message);
      }
            

      if (s_message.equals("exit"))
        break;
    }  

    System.out.println(DEFAULT_COLOR);
    setUp.terminateConnection();

  }
}

