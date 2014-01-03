

package kickstater;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Servidor {
    public static final int PORT = 1111;

    
    
    
    private void runServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        TreeMap<Integer,Projeto> p = new TreeMap<>();
        TreeMap<String,Utilizador> u = new TreeMap<>();
        final Lock l1 = new ReentrantLock();
        final Lock l2 = new ReentrantLock();
        final Lock l3 = new ReentrantLock();
        Condition cond = l1.newCondition();
        System.out.println("Server up & ready for connections...\n");
        while(true){
            Socket socket = serverSocket.accept();
            new ServerThread(socket,p,u,l1,l2,l3,cond).start();
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
