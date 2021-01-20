/** 
 * This is just a testing and playing around with Sha-1 Hash,
 * which is required for my CS Assignment
 * In real world, do not ever use SHA-1 as a hashing technique.
 * It is not secure anymore.
 */
import java.io.BufferedReader;
import java.io.PrintStream;


class Host {


  public static void main(String[] args) throws Exception {
    
    
    SetUp setUp = new SetUp();
    setUp.readConfigs();
    setUp.initConnection();
    setUp.performHandShake();

    BufferedReader kb_bufferedReader = setUp.getKeyBoardBufferedReader();
    
    String r_message, s_message;

    while(true) {
      r_message = setUp.receiveAndDecryptMessage();
      System.out.println("Client : " + r_message);

      if (r_message.equals(".exit()"))
        break;

      s_message = kb_bufferedReader.readLine();
      setUp.sendEncryptedMessage(s_message);
    }

    setUp.terminateConnection();
    
  }
}
