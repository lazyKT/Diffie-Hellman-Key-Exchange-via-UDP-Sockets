/** 
 * This is just a testing and playing around with Sha-1 Hash,
 * which is required for my CS Assignment
 * In real world, do not ever use SHA-1 as a hashing technique.
 * It is not secure anymore.
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.PrintStream;


class Host {


  public static void main(String[] args) throws Exception {
    
    
    SetUp setUp = new SetUp();
    setUp.readConfigs();
    setUp.initConnection();
    setUp.performHandShake();
    // start background thread for receiving messages from client
    //setUp.start();

    BufferedReader kb_bufferedReader = setUp.getKeyBoardBufferedReader();
    DataInputStream inputStream = setUp.getInputStream();
    
    // String s_message;

    // while(setUp.isAlive()){

    //   while ( !kb_bufferedReader.ready() ) {
        
    //     Thread.sleep(500);

    //     // when background job is finished, aka the client terminates the connection
    //     if (!setUp.isAlive())
    //       setUp.terminateConnection();
    //   }

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
          System.out.println("Client : " + r_message);

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
