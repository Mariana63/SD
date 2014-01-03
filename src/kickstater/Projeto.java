
package kickstater;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;


public class Projeto {
    private final Utilizador _utilizador;
    private String _designacao;
    private String _descricao;
    private final float _fTotal;
    private int _codigo;
    private float _fAtual;
    private TreeMap<String,Float> _colaboradores;
    
    public Projeto(Utilizador u, String des, String dc, float fin, int cod) throws FileNotFoundException{
        _utilizador = u;
        _designacao = des;
        _descricao = dc;
        _fTotal = fin;
        _codigo = cod;
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
    
    public int getCod(){
        return _codigo;
    }
    
    public void setCod(int cod) throws FileNotFoundException{
        _codigo = cod;
    }
    
    
    public float getFinAtual(){
        return _fAtual;
    }
    
    public TreeMap<String,Float> getColaboradores(){
        TreeMap<String,Float> result = new TreeMap<>();
            result.putAll(_colaboradores);
        return result;
    }
    
    public void setColaboradores(TreeMap<String,Float> res){
        _colaboradores.clear();
        _colaboradores.putAll(res);
    }
    
    public ArrayList<String> getColaboradoresName(){
        ArrayList<String> result = new ArrayList<>();
            for(String u : _colaboradores.keySet()){
                result.add(u);
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
                s.append("Utilizador: ").append(u).append(" Contribuição: ").append(_colaboradores.get(u)).append("\n");
            }
        }
        return s.toString();
    }
    
    public void aumentaFin(float f){
        _fAtual+=f;
    }

    
    public void addColaborador(String username, float financ){
        _colaboradores.put(username, financ);
    }
    
    
    public TreeMap<String,Float> top(int N){
        TreeMap<String,Float> result = new TreeMap<>();
        Float value;
        List<String> aux1;
        List<Float> aux2;
            aux1 = new ArrayList<>(_colaboradores.keySet());
            aux2 = new ArrayList<>(_colaboradores.values());
        Collections.sort(aux2,new FloatComparator());
        if(N != 0){
            for(int i=0 ; i < N ; i++){
                value = aux2.get(i);
                for(String s : aux1){
                        if(_colaboradores.get(s) == value )
                            result.put(s, value);
                    }
                    
                }
        }
        else    result = (TreeMap<String, Float>) _colaboradores.clone();
        return result;
    }

}
