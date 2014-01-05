
package kickstater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    
     
    private static BufferedReader getSocketReader(Socket client){
        try {
            return new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
 
    private static PrintWriter getSocketWriter(Socket client) {
        try {
            return new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
    
    public static void main(String[] args){
        try (Socket client = new Socket("localhost",Servidor.PORT)) {
            
            String line;
            final BufferedReader input = getSocketReader(client);
            PrintWriter output = getSocketWriter(client);
            System.out.println("escreva help para saber os comandos ");
            while(!client.isClosed()){
            Scanner sc = new Scanner(System.in);
            line = sc.nextLine();
            output.println(line);
            
            output.flush();
            
            if(line.equals("logout") || line.equals("encerrarS") )
                break;
            
            new Thread(new Runnable(){
                    public volatile boolean flag = true;
    
                    public void killThread(){
                        flag = false;
                    }
                    @Override
                    public void run() {
                        try {
                            String rec;
                            while(flag){
                                try {
                                    rec = input.readLine();
                                    if(rec == null){
                                        killThread();
                                    }
                                    else
                                        System.out.println(rec);
                                } catch (IOException ex) {
                                    System.err.println(ex);
                                }
                                
                            }
                            input.close();
                            
                        } catch (IOException ex) {
                            System.err.println(ex);
                        }
                    }
                    
                }).start();
            }
            output.close();
            System.exit(0);
        } catch (IOException ex) {
            System.err.println(ex);
            //System.exit(0);
        }
        
    }
    
}
