
package kickstater;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.TreeSet;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread{
    Socket socket;
    Utilizador user = null;
    private TreeMap<String,Projeto> _lProjetos;
    private TreeMap<String,Utilizador> _lUtilizadores;
    private BufferedReader input;
    private PrintWriter output;
    
    ServerThread(Socket sock){
        this.socket=sock;
    }
    
    private static BufferedReader getSocketReader(Socket client) throws IOException {
        return new BufferedReader(new InputStreamReader(client.getInputStream()));
    }
 
    private static PrintWriter getSocketWriter(Socket client) throws IOException {
        return new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
    }
    
    public synchronized boolean loginArgs(String aUsername, String aPassword){
        user = _lUtilizadores.get(aUsername);
        return ((user != null) && (user.getPass().equals(aPassword)));
    }
    
    public void login(){
        try {
            input = getSocketReader(this.socket);
            String line = input.readLine();
            String[] parse = line.split("\u0020");
            if (parse.length == 3) {
                String Comando = parse[0];
                loginArgs(parse[1],parse[2]);
            }else   
                output.println("login <username> <password>\n");
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
      
    }
    
    public void submeterP(String desig, String desc, float fin){
        try {
            Projeto p = new Projeto(user,desig,desc,fin);
            synchronized(p){
            while(p.getUpdate() == false){
                try {
                    p.wait();
                    output.println("Finaciamento Atual é de "+p.getFinAtual());
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    
    public float financiarArgs(String path, float financ) throws FileNotFoundException{
        BufferedReader cod = new BufferedReader(new FileReader(path));
        float restante = 0;
        boolean comp = false;
        synchronized(_lProjetos){
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    comp=p.compareCodigo(cod);
                if(comp == true){
                    p.addColaborador(user.getUserName(),financ);
                    p.aumentaFin(financ);
                    p.setTrue();
                    p.notifyAll();
                    restante = p.getFinAtual() - financ;
                    }
                }
            }
            
        }
        
        return restante;
    }
    

    public TreeSet<Projeto> listaNaoFin(String chave){
        TreeSet<Projeto> result = new TreeSet<>();
        synchronized(_lProjetos){
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    if(p.getDescricao().contains(chave)){
                        if(p.getFinAtual() < p.getFinTotal() )
                            result.add(p.clone());
                    }
                }
                
            }
        }
        return result;
    }
    
    public TreeSet<Projeto> listaFin(String chave){
        TreeSet<Projeto> result = new TreeSet<>();
        synchronized(_lProjetos){
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    if(p.getDescricao().contains(chave)){
                        if(p.getFinAtual() < p.getFinTotal() )
                            result.add(p.clone());
                    }
                }
            }
        }
        return result;
    }
    
    public void informacao(BufferedReader cod, int N){
        boolean comp = false;
        Projeto pr = null;
        float fact;
        TreeMap<String,Float> cola;
        cola = new TreeMap<>();
        String desig, desc;
        synchronized(_lProjetos){
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    comp = p.compareCodigo(cod);
                    if (comp) {
                        pr = p.clone();
                    }
                    }
                }
            }
        if(pr != null){
               desig = pr.getDesig();
               desc = pr.getDescricao();
               fact = pr.getFinAtual();
               cola = pr.getColaboradores();
               System.out.println(desig);
               System.out.println(desc);
               System.out.println(fact);
               cola=pr.top(N);
               System.out.println(cola.toString());
        }
        else    System.out.println("Projeto não encontrado");
    }
    
    
    
    @Override
    public void run(){
        try {
            input = getSocketReader(this.socket);
            String line = input.readLine();
            String[] parse = line.split("\u0020");
            String comando = parse[0];
            switch(comando){
                case ("login"):  
                    loginArgs(parse[1],parse[2]);
                    break;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
            
   
    }
}
