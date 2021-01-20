# UDP-Sockets-with-Diffie-Hellman-Key-Exchange
Implementing and Establishing the secure channel between two parties using Diffie-Hellman Key Exchange

- Alice stands for Host(Server)
- Bob stands for Client

#Usage :

- Run server and client in seperate terminal windows.

-to run server

  $ cd Alice

  $ javac Host.java SetUp.java;java Host

-to run client

  $cd Bob

  $ javac Client.java;java Client <port-number>

# Note

Please run the server first.*

When the client is run while there is no serve listening at the given <port-number>, the program will throw ConnectException.*
