
package kickstater;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread{
    Socket socket;
    Utilizador user = null;
    private Random rand;
    private TreeMap<Integer,Projeto> _lProjetos;
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
    
    public void registar(String username, String pass){
        Utilizador p = null;
        p.setUsername(username);
        p.setPass(pass);
        _lUtilizadores.put(username, p);
        if (p != null){
            output.println("Registado com Sucesso");
        }else   output.println("Username já usado");
    }
    
    
    public void submeterP(String desig, String desc, float fin){
        try {
            int code = rand.nextInt(100);
            while (_lProjetos.keySet().contains(code));
            Projeto p = new Projeto(user,desig,desc,fin,code);
            synchronized(p){
            while(p.getFinAtual() < p.getFinTotal()){
            while(p.getUpdate() == false ){
                try {
                    p.wait();
                    output.println("Finaciamento Atual é de "+p.getFinAtual()+"\n");
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             p.setFalse();
            }
            output.println("Projeto Financiado \n");
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    
    public void financiarArgs(int code, float financ) throws FileNotFoundException{
        Projeto p;
        float res, restante;
        for(Integer c : _lProjetos.keySet()){
                synchronized(_lProjetos){
                if(c == code){
                        p = _lProjetos.get(c);
                        synchronized(p){
                            if(p.getFinAtual() < p.getFinTotal()){
                            res = p.getFinAtual() + financ;
                            restante = p.getFinTotal() - p.getFinAtual();
                            if (res > p.getFinTotal() ){
                                output.println("O valor" +financ + "ultrapassa o valor total de financiamento" +p.getFinTotal());
                                p.aumentaFin(restante);
                                output.println("O valor financiado foi de "+restante);
                            }
                            else{
                                p.aumentaFin(financ);
                            }
                            p.addColaborador(user.getUserName(),financ);
                            p.setTrue();
                            p.notifyAll();
                            }
                            else    output.println("Projeto já financiado totalmente \n");
                        }
                    }
                }
            }
    }
    

    public void listaNaoFin(String chave){
        synchronized(_lProjetos){
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    if(p.getDescricao().contains(chave)){
                        if(p.getFinAtual() < p.getFinTotal() )
                            output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                    }
                }
                
            }
        }
    }
    
    public void listaFin(String chave){
        synchronized(_lProjetos){
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    if(p.getDescricao().contains(chave)){
                        if(p.getFinAtual() < p.getFinTotal() )
                            output.println(p.toString());
                    }
                }
            }
        }
    }
    
    public void informacao(int cod, int N){
        Projeto pr = null;
        TreeMap<String,Float> cola;
        cola = new TreeMap<>();

        for(Projeto p : _lProjetos.values()){
            synchronized(_lProjetos){
                synchronized(p){
                    if (cod == p.getCod()) {
                        pr = p.clone();
                    }
                    }
                }
            }
        if(pr != null){
               cola=pr.top(N);
               pr.setColaboradores(cola);
               output.println(pr.toString());
        }
        else   output.println("Projeto não encontrado");
    }
    
    public void help(){
        output.println("");
    }
    
    
    @Override
    public void run(){
        try {
            input = getSocketReader(this.socket);
            String line = input.readLine();
            String[] parse = line.split("\u0020");
            String comando = parse[0];
            switch(comando){
                case ("registar"):
                    registar(parse[1],parse[2]);
                    break;
                case ("login"):  
                    loginArgs(parse[1],parse[2]);
                    break;
                case ("encerrar"):
                    socket.close();
                    break;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
            
   
    }
}
