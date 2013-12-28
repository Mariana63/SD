/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kickstater;

import java.util.Objects;


public class Utilizador {
    private String _username;
    private String _password;
    
    public Utilizador(String u, String p){
        _username = u;
        _password = p;
    }
    
    public Utilizador(Utilizador u){
        _username = u.getUserName();
        _password = u.getPass();
    }
    
    public String getUserName(){
        return _username;
    }
    
    public String getPass(){
        return _password;
    }
    
    
    @Override
    public boolean equals(Object obs){
    if (obs == null)
        return false;
    if (obs == this)
        return true;
    if (!(obs instanceof Utilizador))
        return false;
    Utilizador ut = (Utilizador)obs;
        return ut.getUserName().equals(this.getUserName());
}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this._username);
        return hash;
    }

    @Override
    public Utilizador clone(){
       return new Utilizador(this);
    }
    
    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("Username: ").append(this._username);
        s.append("\n");
        return s.toString();
    }
    
    
}
