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
    setUp.init();
    setUp.performHandShake();

    BufferedReader kb_bufferedReader = setUp.getBufferedReader();
    String r_message, s_message;

    while (true) {
      s_message = kb_bufferedReader.readLine();
      setUp.sendEncryptedMessage(s_message);

      if (s_message.equals(".exit()"))
        break;

      r_message = setUp.receiveAndDecryptMessage();
      System.out.println("Host : " + r_message);

      if (r_message.equals(".exit()"))
        break;
    }

    setUp.terminateConnection();

  }
}
