
package kickstater;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread{
    Socket socket;
    Utilizador user = new Utilizador();
    private Random rand;
    private TreeMap<Integer,Projeto> _lProjetos;
    private TreeMap<String,Utilizador> _lUtilizadores;
    private ArrayList<String> _login;
    private BufferedReader input;
    private PrintWriter output;
    private Lock lock1 = new ReentrantLock();
    private Lock lock2 = new ReentrantLock();
    private Lock lock3 = new ReentrantLock();
    private Lock lock4 = new ReentrantLock();
    private Condition cond = lock1.newCondition();
    
    
    
    ServerThread(Socket sock, TreeMap<Integer,Projeto> p , TreeMap<String,Utilizador> u, ArrayList<String> log, Lock l1, Lock l2, Lock l3,Lock l4, Condition c){
        this.socket=sock;
        _lUtilizadores = u;
        _lProjetos = p;
        _login = log;
        lock1 = l1;
        lock2 = l2;
        lock3 = l3;
        lock4 = l4;
        cond = c;
        rand = new Random();
        input = getSocketReader(sock);
        output = getSocketWriter(sock);
        
    }
    
    private static BufferedReader getSocketReader(Socket client){
        try {
            return new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
 
    private static PrintWriter getSocketWriter(Socket client){
        try {
            return new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        return null;
    }
    
    public void addlogin(String username){
        lock4.lock();
        boolean c = false;
        try{
            c = _login.contains(username);
        }
        finally{
            lock4.unlock();
        }
        if(c == true){
                user = new Utilizador();
                output.println("login já efetuado");
                output.flush();
            }
        else{
            _login.add(username);
            output.println("login efetuado com Sucesso");
            output.flush();
        }
    }
    
    public void login(String aUsername, String aPassword){
        lock2.lock();
        try{
            user = _lUtilizadores.get(aUsername);
        }
        finally{
            lock2.unlock();
        }
        
        if((user != null) && (user.getPass().equals(aPassword))){
            
            addlogin(user.getUserName());
            }
        else{
                output.println("Precisa dwe registar-se primeiro");
                output.flush();
                }    
    }
    
    
    
    public void registar(String username, String pass){
        Utilizador ut;
        Utilizador u = new Utilizador(username,pass);
        lock2.lock();
        try{
            ut = _lUtilizadores.get(username);
            
        }
        finally{
            lock2.unlock();
        }
        if(ut != null){
                output.println("Username já usado");
                output.println("Registe-se novamente");
                output.flush();
            }
        else{
            lock2.lock();
            try{
                _lUtilizadores.put(username, u);
            }
            finally{
                lock2.unlock();
            }
                
            output.println("Registado com Sucesso");
            output.flush();
            
            }

    }
    
    
    public void submeterP(String desig, String desc, float fin){
        if(user.getUserName().isEmpty()){
            output.println("Para executar esta operação precisa registar-se e autenticar-se");
            output.flush();
        }
        else{
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
            System.err.println(ex);
        }
        
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
    

    
    
    public void listaNaoFin(String chave){
        boolean b;
        lock3.lock();
        try{
            b = _lProjetos.isEmpty();
            if(b != false){
                output.println("Não existem projetos");
                output.flush();
            }
            else{
                for(Projeto p : _lProjetos.values()){
                    lock1.lock();
                    try{
                        if(p.getDescricao().contains(chave)){
                            if(p.getFinAtual() < p.getFinTotal() ){
                                output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                                output.flush();
                            }
                            else{
                                output.println("Projeto já financiado");
                                output.flush();
                            }
                        }
                        else{
                            output.println("Não existem projetos com a chave fornecida");
                            output.flush();
                        }
                    }
                    finally{
                        lock1.unlock();
                    }
                }
            }
        }
        finally{
            lock3.unlock();
        }
    }
    
    
    
    public void listaFin(String chave){
        boolean b;
        lock3.lock();
        try{
            b = _lProjetos.isEmpty();
            if(b != false){
                output.println("Não existem projetos");
                output.flush();
            }
            else{
                for(Projeto p : _lProjetos.values()){
                    lock1.lock();
                    try{
                        if(p.getDescricao().contains(chave)){
                            if(p.getFinAtual() == p.getFinTotal() ){
                                output.println("Codigo : "+ p.getCod() + "  Designação : "+p.getDesig());
                                output.flush();
                            }
                            else{
                            output.println("Projeto ainda nao totalmente financiado");
                            output.flush();
                            }
                        }
                        else{
                            output.println("Não existem projetos com a chave fornecida");
                            output.flush();
                        }
                    }
                    finally{
                        lock1.unlock();
                    }
                }
            }
        }
        finally{
            lock3.unlock();
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
            if(pr == null){
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
        output.println("Registar : registar <username> <password>");
        output.println("Login : login <username> <password>");
        output.println("Submeter um projeto : adicionar <designacao> <descricao> <financiamento total requerido>");
        output.println("Financiar um Projeto : financiar <codigo do projeto> <montante a financiar>");
        output.println("Lista de projetos ainda sem financiamento total : LNF <palavra chave>");
        output.println("Lista de projetos já totalmente financiados : LF <palavra chave>");
        output.println("Informação sobre o projeto : informacao <codigo do projeto> <n de colaboradores>");
        output.println("Encerrar cliente : logout");
        output.println("Encerrar cliente e servidor : encerrarS");
        output.flush();
    }
    
    private void closeConnection() { 
        try {
            _login.remove(user.getUserName());
            socket.shutdownInput();  
            socket.shutdownOutput();  
            socket.close();
            input.close();  
            output.close();

        } catch (IOException ex) {
            System.err.println(ex);
        }
    } 
    
    public int toInt(String s){
        int r = 0;
        try{  
            r = Integer.parseInt(s);  
        }catch(NumberFormatException e){  
              output.println("O codigo tem de ser um numero inteiro");
              output.flush();
        }  
        return r;
    }
    
    public Float toFloat(String s){
        float r = 0;
        try{  
             r = Float.parseFloat(s);  
        }catch(NumberFormatException e){  
              output.println("O Financiamento tem de ser um numero real");
              output.flush();
    }  
        return r;
    }
    
    @Override
    public void run(){
        try{
        input = getSocketReader(socket);
        System.out.println("Um Cliente ligou-se ao servidor");
        while(!socket.isClosed() && socket.isConnected()){
            String line = null;
            try {
                line = input.readLine();
            } catch (IOException ex) {
                System.err.println(ex);
            }
            if (line == null)
                break;
            String[] parse = line.split("\u0020");
            String comando = parse[0];
            switch(comando){
                case ("registar"):
                    if(parse.length==3)
                        registar(parse[1],parse[2]);
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("login"):
                    if(parse.length==3)
                        login(parse[1],parse[2]);
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("adicionar"):
                    if(parse.length == 4 )
                        submeterP(parse[1],parse[2],toFloat(parse[3]));
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("financiar"):
                    if(parse.length == 3)
                        financiar(toInt(parse[1]),toFloat(parse[2]));
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("LNF"):
                    if(parse.length == 2)
                        listaNaoFin(parse[1]);
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("LF"):
                    if(parse.length == 2)
                        listaFin(parse[1]);
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("informacao"):
                    if(parse.length == 3)
                        informacao(toInt(parse[1]),toInt(parse[2]));
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("help"):
                    if(parse.length == 1)
                        help();
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                case ("encerrarS"):
                    if (parse.length == 1){
                        closeConnection();
                        System.exit(0);
                        
                    }
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    case ("logout"):
                    if (parse.length == 1){
                        closeConnection();
                        
                    }
                    else{
                        output.println("Numero de argumentos errado");
                        output.flush();
                    }
                    break;
                default:
                    output.println("Comando errado");
                    output.flush();
                    break;
            }
            

        }
        socket.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
