/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javachatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/**
 *
 * @author jamie
 */
public class JavaChatServer {
    
    static int numClients = 0;
    
       /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;

    /**
     * The set of all names of clients in the chat room.  Maintained
     * so that we can check that new clients are not registering name
     * already in use.
     */
    private static HashSet<String> names = new HashSet<String>();

    /**
     * The set of all the print writers for all the clients.  This
     * set is kept so we can easily broadcast messages.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    
   // ServerSocket listener;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
      //   ServerSocket listener=;
         
      
      
       System.out.println("The chat server is running.");

       // create a listen socket and loop forever accepting new clients
        try {
           ServerSocket listener = new ServerSocket(PORT);

    
            while (true) {
                new Handler( listener.accept()).start();
                numClients++;
            } 
            
            // listener.close();
 
            } catch(Exception e) {
         System.out.println("IOException occurred");
        }   
        
    }
    
    
     /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
     private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }


        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
		    // make sure onlyone thread acts at a time to the names hash set
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                
                //out.println("NAMEACCEPTED "+numClients+" ");

		// send nameaccepted to the client and number of client
		// ( 1 indicates first client and forces client to choose dinnerType)
		out.println("NAMEACCEPTED "+(writers.size()+1)+" ");
                
                // Send SYNC Message to clients to force sync
		for (PrintWriter writer : writers) {
                
		    System.out.println("Sync writers "+(writers.size()+1));
		    writer.println("SYNC: "+name);
		    writer.flush();
		    //     break;
		}
                
                writers.add(out);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }

                    if ( input.startsWith("$"))
                    {
			// dinnertype
                        for (PrintWriter writer : writers) {
                            writer.println("TYPE: " + input);
                        }
                    } else if ( input.startsWith("+"))
                    {
			// button set
                        for (PrintWriter writer : writers) {
                            writer.println("STUFF: " + input);
                        }
                    } else 
                    if ( input.startsWith("-"))
                    {
			// button reset
                        for (PrintWriter writer : writers) {
                            writer.println("STUFF: " + input);
                        }
                    }                    
                    else
                     if ( input.startsWith("^"))
                    {
			// size set/rest
                        for (PrintWriter writer : writers) {
                            writer.println("SIZE: " + input);
                        }
                    }                    
                    else
                   {
		       // chat message 
		       for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                numClients--;
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    
    
    
}
