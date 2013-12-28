

package kickstater;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Servidor {
    public static final int PORT = 4444;
    
    
    
    private void runServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server up & ready for connections...\n");
        while(true){
            Socket socket = serverSocket.accept();
            new ServerThread(socket).start();
        }
        
    }
    
    public static void main(String[] args) {
        try {
            new Servidor().runServer();
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
      
}
