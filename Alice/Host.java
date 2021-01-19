/** 
 * This is just a testing and playing around with Sha-1 Hash,
 * which is required for my CS Assignment
 * In real world, do not ever use SHA-1 as a hashing technique.
 * It is not secure anymore.
 */
import java.io.BufferedReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.*;


class Host {


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

    
    SetUp host_setup = new SetUp(hashed_pwd, P, G);
    PrintStream printStream = host_setup.getPrintStream();
    BufferedReader kb_bufferedReader = host_setup.getKeyBoardBufferedReader();
    BufferedReader r_bufferedReader = host_setup.getReceiveBufferedReader();
    
    

    while(true) {
      String r_message, s_message = ""; 

      // read/send message from/to the client
      while ( (r_message = r_bufferedReader.readLine()) != null) {
      
        System.out.println("Client : " + r_message);
        
        if ( (r_message.toLowerCase()).equals("bob") ) {
          // receive username
          // return P, G and G^a mod P values in RC4 Stream Cihper
          Random rand = new Random(System.currentTimeMillis());
          BigInteger X = utility.findModulo(P, G, rand.nextInt(99999));

          String cihper_string = new String("_cipher_," + P.toString() + "," + G.toString() + "," + X.toString());
          
          printStream.println(cihper_string + "\n");

          continue;
        }


        if ( r_message.equals(".exit()") )
          host_setup.terminateConnection();

        s_message = kb_bufferedReader.readLine();
        // send message to client
        printStream.println(s_message);

        if ( s_message.equals(".exit()") )
          host_setup.terminateConnection();
      }

      System.out.println("Outside Client Loop");
   
      if ( (s_message.trim()).equals(".exit()") )
        host_setup.terminateConnection();
    }
  }
}
