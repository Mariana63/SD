
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
    
    
    
    ServerThread(Socket sock, TreeMap<Integer,Projeto> p , TreeMap<String,Utilizador> u){
        this.socket=sock;
        _lUtilizadores = u;
        _lProjetos = p;
        rand = new Random();
        try {
            input = getSocketReader(sock);
            output = getSocketWriter(sock);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static BufferedReader getSocketReader(Socket client) throws IOException {
        return new BufferedReader(new InputStreamReader(client.getInputStream()));
    }
 
    private static PrintWriter getSocketWriter(Socket client) throws IOException {
        return new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
    }
    
    public void loginArgs(String aUsername, String aPassword){
        user = _lUtilizadores.get(aUsername);
        if((user != null) && (user.getPass().equals(aPassword))){
            
            output.println("login efetuado com Sucesso");
            output.flush();
            }
        else{
                output.println("login efetuado sem Sucesso");
                output.flush();
                }    
    }
    
    public void registar(String username, String pass){
        Utilizador p = new Utilizador(username,pass);
        _lUtilizadores.put(username, p);
        if (p.getUserName().length() != 0){
            output.println("Registado com Sucesso");
            output.flush();
        }else{
            output.println("Username já usado");
            output.flush();
        }
    }
    
    
    public void submeterP(String desig, String desc, float fin) throws FileNotFoundException, InterruptedException{
        try {
            int code = rand.nextInt(100);
            //while (_lProjetos.keySet().contains(code));
            
            Projeto p = new Projeto(user,desig,desc,fin,code);
            _lProjetos.put(p.getCod(), p);
            synchronized(p){
                while(p.getFinAtual() < p.getFinTotal() ){
                try {
                    System.out.println("wait");
                    p.wait();
                    System.out.println("continua");
                    output.println("Financiamento actual: "+p.getFinAtual());
                    output.flush();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                output.println("Projeto Financiado \n");
                output.flush();
                
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    
    public void financiarArgs(int code, float financ) throws FileNotFoundException{
        Projeto p;
        float res, restante;
        for(Integer c : _lProjetos.keySet()){
                //synchronized(_lProjetos){
                if(c == code){
                        p = _lProjetos.get(c);
                        synchronized(p){
                            if(p.getFinAtual() < p.getFinTotal()){
                                res = p.getFinAtual() + financ;
                                restante = p.getFinTotal() - p.getFinAtual();
                            if (res > p.getFinTotal() ){
                                output.println("O valor " +financ + " ultrapassa o valor total de financiamento " +p.getFinTotal());
                                output.flush();
                                p.aumentaFin(restante);
                                output.println("O valor financiado foi de "+restante);
                                output.flush();
                            }
                            
                            else{
                                p.aumentaFin(financ);
                            }
                            p.addColaborador(user.getUserName(),financ);
                            output.println("financiamento completo");
                            output.flush();
                            System.out.println(p.getFinAtual());
                            p.notifyAll();
                            }
                            else {   
                                output.println("Projeto já financiado totalmente \n");
                                output.flush();
                            }
                            
                        }
                      
                    }
                //}
            }
    }
    

    public void listaNaoFin(String chave){
        //synchronized(_lProjetos){
        boolean b;
        b = _lProjetos.isEmpty();
             if(b == true){
                 output.println("Não existem projetos");
                 output.flush();
             }
             else {
                 for(Projeto p : _lProjetos.values()){
                     synchronized(p){
                         if(p.getDescricao().contains(chave)){
                             if(p.getFinAtual() < p.getFinTotal() ){
                                 output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                                 output.flush();
                         }
                         }
                     }
                     
                 }
        }
        //}
    }
    
    public void listaFin(String chave){
        
        //synchronized(_lProjetos){
            if(_lProjetos.isEmpty()){
                output.println("Não existem projetos");
                output.flush();
            }
            else{
            for(Projeto p : _lProjetos.values()){
                synchronized(p){
                    if(p.getDescricao().contains(chave)){
                        if(p.getFinAtual() == p.getFinTotal() ){
                            output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                            output.flush();
                        }
                    }
                }
            }
        //}
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
               output.flush();
        }
        else   {
            output.println("Projeto não encontrado");
            output.flush();
        }
    }
    
    public void help(){
        output.println("");
    }
    
    
    @Override
    public void run(){
        try {
            input = getSocketReader(socket);
            while(socket.isConnected()){
                String line = input.readLine();
                //while(line != null){
                    String[] parse = line.split("\u0020");
                String comando = parse[0];
                switch(comando){
                    case ("registar"):
                        registar(parse[1],parse[2]);
                        break;
                    case ("login"):  
                        loginArgs(parse[1],parse[2]);
                        break;
                    case ("submeterProjeto"):  
                        submeterP(parse[1],parse[2],Float.valueOf(parse[3]));
                        break;
                    case ("Financiar"):  
                        financiarArgs(Integer.valueOf(parse[1]),Integer.valueOf(parse[2]));
                        break;
                    case ("LNaoFin"):  
                        listaNaoFin(parse[1]);
                        break;
                    case ("LFin"):  
                        listaFin(parse[1]);
                        break;
                    case ("Informacao"):  
                        informacao(Integer.valueOf(parse[1]),Integer.valueOf(parse[2]));
                        break;
                    case ("encerrar"):
                        socket.close();
                        break;
                    default:
                        output.println("Comando errado");
                        output.flush();
                        break;
                //}
                }
                
                
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
            
   
    }
}
