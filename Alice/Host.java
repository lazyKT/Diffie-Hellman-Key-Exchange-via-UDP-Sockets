/** 
 * This is just a testing and playing around with Sha-1 Hash,
 * which is required for my CS Assignment
 * In real world, do not ever use SHA-1 as a hashing technique.
 * It is not secure anymore.
 */
import java.io.BufferedReader;
import java.io.DataInputStream;


class Host {

  private static final String RECEIVE_TEXT_COLOR = "\u001B[36m"; // cyan
  public static final String SEND_TEXT_COLOR = "\u001B[35m"; // purple
  private static final String DEFAULT_COLOR = "\u001B[0m"; // default color
  private static final String ERROR_TEXT_COLOR = "\u001B[31m"; // red

  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      System.out.println("Usage: java Host <port-number>");
      System.exit(1);
    }

    int PORT  = Integer.parseInt(args[0]);

    
    SetUp setUp = new SetUp(PORT);
    setUp.readConfigs();
    setUp.initConnection();
    setUp.performHandShake();
    // start background thread for receiving messages from client
    //setUp.start();

    BufferedReader kb_bufferedReader = setUp.getKeyBoardBufferedReader();
    DataInputStream inputStream = setUp.getInputStream();
    System.out.print(SEND_TEXT_COLOR + "\n" + SEND_TEXT_COLOR);
    while (true) {
      String s_message = ""; 
      String r_message = "";
      
      while ( !kb_bufferedReader.ready() ) {

        while ( inputStream.available() > 0 ) {
          // receive message
          r_message = setUp.receiveMessage();
          if (r_message != null) {

            System.out.print(RECEIVE_TEXT_COLOR + "Client : "+ r_message + RECEIVE_TEXT_COLOR);
            System.out.print(SEND_TEXT_COLOR+"\n\n"+SEND_TEXT_COLOR);

            if (r_message.equals("exit"))
              setUp.terminateConnection();

          }
          else {
            // reject message
            System.out.println(ERROR_TEXT_COLOR + "MESSAGE REJECTED" + ERROR_TEXT_COLOR);
            System.out.println(SEND_TEXT_COLOR);
          }
        }
      }

      if (kb_bufferedReader.ready()) {
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
