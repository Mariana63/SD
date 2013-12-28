
package kickstater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.lang.String;

public class Client {
     
    private static BufferedReader getSocketReader(Socket client) throws IOException {
        return new BufferedReader(new InputStreamReader(client.getInputStream()));
    }
 
    private static PrintWriter getSocketWriter(Socket client) throws IOException {
        return new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
    }
    
    public static void main(String[] args) throws IOException {
        try (Socket client = new Socket("localhost",Servidor.PORT)) {
            BufferedReader input = getSocketReader(client);
            PrintWriter output = getSocketWriter(client);
            
        }
       
    }
    
}
