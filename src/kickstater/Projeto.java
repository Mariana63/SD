
package kickstater;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Projeto {
    private final Utilizador _utilizador;
    private String _designacao;
    private String _descricao;
    private float _fTotal;
    private BufferedReader _codigo;
    private boolean _update = false;
    private float _fAtual;
    private TreeMap<String,Float> _colaboradores;
    
    public Projeto(Utilizador u, String des, String dc, float fin) throws FileNotFoundException{
        _utilizador = u;
        _designacao = des;
        _descricao = dc;
        _fTotal = fin;
        _codigo = new BufferedReader(new FileReader("d:/inicio.txt"));
        _fAtual = 0;
        _colaboradores = new TreeMap<>();
    }
    
    public Projeto(Projeto p){
        _utilizador = p.getUtilizador();
        _designacao = p.getDesig();
        _descricao = p.getDescricao();
        _fTotal = p.getFinTotal();
        _codigo = p.getCod();
        _fAtual = p.getFinAtual();
        _colaboradores = p.getColaboradores();
    }
    
    public Utilizador getUtilizador(){
        return _utilizador;
    }
    
    public String getDesig(){
        return _designacao;
    }
    
    public void setDesig(String d){
        _designacao = d;
    }
    
    public String getDescricao(){
        return _descricao;
    }
    
    public void setDescricao(String d){
        _descricao = d;
    }
    
    public float getFinTotal(){
        return _fTotal;
    }
    
    public BufferedReader getCod(){
        return _codigo;
    }
    
    public void setCod(String path) throws FileNotFoundException{
        _codigo =new BufferedReader(new FileReader("d:/inicio.txt"));
    }
    
    public boolean getUpdate(){
        return _update;
    }
    
    public void setTrue(){
        _update = true;
    }
    
    public synchronized float getFinAtual(){
        return _fAtual;
    }
    
    public TreeMap<String,Float> getColaboradores(){
        TreeMap<String,Float> result = new TreeMap<>();
        synchronized(_colaboradores){
            result.putAll(_colaboradores);
        }
        return result;
    }
    
    public ArrayList<String> getColaboradoresName(){
        ArrayList<String> result = new ArrayList<>();
        synchronized(_colaboradores){
            for(String u : _colaboradores.keySet()){
                result.add(u);
            }
        }
        return result;
    }

    
    @Override
    public Projeto clone(){
        return new Projeto(this);
    }
    
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append(_utilizador.toString()).append("\n");
        s.append("Designação: ").append(_designacao).append("\n");
        s.append("Descrição: ").append(_descricao).append("\n");
        s.append("Financiamento Requerido: ").append(_fTotal).append("\n");
        s.append("Financiamento Atual: ").append(_fAtual).append("\n");
        synchronized(_colaboradores){
            for(String u : _colaboradores.keySet()){
                s.append("Utilizador: ").append(u).append("\n");
            }
        }
        return s.toString();
    }
    
    public synchronized void aumentaFin(float f){
        _fAtual+=f;
    }
    
    public synchronized void addColaborador(String username, float financ){
        _colaboradores.put(username, financ);
    }
    
    
    public TreeMap<String,Float> top(int N){
        TreeMap<String,Float> result = new TreeMap<>();
        Float value;
        List<String> aux1;
        List<Float> aux2;
        synchronized(_colaboradores){
            aux1 = new ArrayList<>(_colaboradores.keySet());
            aux2 = new ArrayList<>(_colaboradores.values());
        }
        Collections.sort(aux2,new FloatComparator());
        if(N != 0){
            for(int i=0 ; i < N ; i++){
                value = aux2.get(i);
                for(String s : aux1){
                    synchronized(_colaboradores){
                        if(_colaboradores.get(s) == value )
                            result.put(s, value);
                    }
                    
                }
                
            }
        }
        else    result = (TreeMap<String, Float>) _colaboradores.clone();
        return result;
    }
    
    public boolean compareCodigo(BufferedReader cod){
        boolean result = false;
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        String line;
        try {
            line = _codigo.readLine();
            while(line != null)
                list1.add(line);
            _codigo.close();
            line = cod.readLine();
            while(line != null)
                list2.add(line);
            cod.close();
            list1.containsAll(list2);
            
        } catch (IOException ex) {
            Logger.getLogger(Projeto.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
  
}