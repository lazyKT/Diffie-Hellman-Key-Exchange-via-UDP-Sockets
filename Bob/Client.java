/** Client Program */
import java.util.*;
import java.io.*;
import java.math.*;
import java.net.*;


class Client {


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
    // setUp.setDaemon(true); // daemon thread
    // setUp.start();
    
    // String s_message;

    // while(setUp.isAlive()) {
      
    //   // when Host is exited the program
    //   while (!kb_bufferedReader.ready()) {
    //     Thread.sleep(500);

    //     // if background job is finished, aka host terminate the connection
    //     if (!setUp.isAlive()) 
    //       setUp.terminateConnection();
          
    //   }
        
    //   // send message
    //   s_message = kb_bufferedReader.readLine();
    //   setUp.sendEncryptedMessage(s_message);

    //   if (s_message.equals("exit"))
    //     break;

    // }
    

    while (true) {
      String s_message = ""; 
      String r_message = "";

      while ( !kb_bufferedReader.ready() ) {

        while ( inputStream.available() > 0 ) {

          r_message = setUp.receiveAndDecryptMessage();
          System.out.println("Host : " + r_message);

          if (r_message.equals("exit"))
            setUp.terminateConnection();
        }
      }

      if (kb_bufferedReader.ready()) {
        s_message = kb_bufferedReader.readLine();
        setUp.sendEncryptedMessage(s_message);
      }
            

      if (s_message.equals("exit"))
        break;
    }  
    
    setUp.terminateConnection();

  }
}

