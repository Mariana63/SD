
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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private Lock lock1 = new ReentrantLock();
    private Lock lock2 = new ReentrantLock();
    private Lock lock3 = new ReentrantLock();
    private Condition cond = lock1.newCondition();
    
    
    
    ServerThread(Socket sock, TreeMap<Integer,Projeto> p , TreeMap<String,Utilizador> u, Lock l1, Lock l2, Lock l3, Condition c){
        this.socket=sock;
        _lUtilizadores = u;
        _lProjetos = p;
        lock1 = l1;
        lock2 = l2;
        lock3 = l3;
        cond = c;
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
        lock2.lock();
        try{
            user = _lUtilizadores.get(aUsername);
        }
        finally{
            lock2.unlock();
        }
        
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
        lock2.lock();
        try{
            _lUtilizadores.put(username, p);
        }
        finally{
            lock2.unlock();
        }
        
        if (p.getUserName().length() != 0){
            output.println("Registado com Sucesso");
            output.flush();
        }else{
            output.println("Username já usado");
            output.flush();
        }
    }
    
    
    public void submeterP(String desig, String desc, float fin){
        try {
            int code = rand.nextInt(100);
            Projeto p = new Projeto(user,desig,desc,fin,code);
            lock3.lock();
            try{
                _lProjetos.put(p.getCod(), p);
            }
            finally{
                lock3.unlock();
            }
            lock1.lock();
            try {
                while(p.getFinAtual() < p.getFinTotal() ){
                    
                    try {
                        output.println("a espera de atualizações....");
                        output.flush();
                        cond.await();
                        output.println("Atualizado");
                        output.println("Financiamento actual: "+p.getFinAtual());
                        output.flush();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            }
            finally{
                lock1.unlock();
            }
                output.println("Projeto Financiado \n");
                output.flush();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    
    public void financiarArgs(int code, float financ){
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
    
    public void financiar(int code, float financ){
        Projeto p;
        float res, restante;
        lock3.lock();
        try{
            p=_lProjetos.get(code);
        }
        finally{
            lock3.unlock();
        }
        lock1.lock();
        try{
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
                    cond.signalAll();
                    output.println("financiamento completo");
                    output.flush();
                    }
                    else {   
                        output.println("Projeto já financiado totalmente \n");
                        output.flush();
                    }
        }
        finally{
            lock1.unlock();
        }
    }
    

    public void listaNaoFin1(String chave){
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
    public void listaNaoFin(String chave){
        boolean b;
        lock3.lock();
        try{
            b = _lProjetos.isEmpty();
            if(b == false){
                for(Projeto p : _lProjetos.values()){
                    lock1.lock();
                    try{
                        if(p.getDescricao().contains(chave)){
                             if(p.getFinAtual() < p.getFinTotal() ){
                                 output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                                 output.flush();
                               }
                         }
                    }
                    finally{
                        lock1.unlock();
                    }
                }    
            }
            else{
                output.println("Não existem projetos");
                output.flush();
            }
        }
        finally{
            lock3.unlock();
        }
    }
    
    
    public void listaFin1(String chave){
        
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
    
    public void listaFin(String chave){
        boolean b;
        lock3.lock();
        try{
            b = _lProjetos.isEmpty();
            if(b == false){
                for(Projeto p : _lProjetos.values()){
                    lock1.lock();
                    try{
                        if(p.getDescricao().contains(chave)){
                             if(p.getFinAtual() == p.getFinTotal() ){
                                 output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                                 output.flush();
                               }
                         }
                    }
                    finally{
                        lock1.unlock();
                    }
                }    
            }
            else{
                output.println("Não existem projetos");
                output.flush();
            }
        }
        finally{
            lock3.unlock();
        }
    }
    
    public void informacao1(int cod, int N){
        Projeto pr = null;
        TreeMap<String,Float> cola;
        cola = new TreeMap<>();

        for(Projeto p : _lProjetos.values()){
            synchronized(_lProjetos){
                synchronized(p){
                    if (cod == p.getCod()) {
                        pr = p;
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
    
    public void informacao(int cod, int N){
        Projeto pr;
        TreeMap<String,Float> cola;
        cola = new TreeMap<>();
        lock3.lock();
        try{
            pr=_lProjetos.get(cod);
        }
        finally{
            lock3.unlock();
        }
        lock1.lock();
        try{
            if(pr.getDesig().isEmpty()){
               output.println("Projeto não encontrado");
                output.flush();
            }
            else{
               cola=pr.top(N);
               pr.setColaboradores(cola);
               output.println(pr.toString());
               output.flush();
            }
        }
        finally{
            lock1.unlock();
        }
    }
    
    public void help(){
        output.println("");
    }
    
    
    @Override
    public void run(){
        try {
            input = getSocketReader(socket);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(socket.isConnected()){
            String line = null;
            try {
                line = input.readLine();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                    financiar(Integer.valueOf(parse[1]),Integer.valueOf(parse[2]));
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
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
                    break;
                default:
                    output.println("Comando errado");
                    output.flush();
                    break;
                    //}
            }
            

        }
            
   
    }
}
