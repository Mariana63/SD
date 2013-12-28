/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kickstater;

import java.util.Comparator;

/**
 *
 * @author Demo
 */
public class FloatComparator implements Comparator {
    

    @Override
    public int compare(Object f11 , Object f22){
         float f1 = (Float) f11;
         float f2 = (Float) f22;
         if (f1 > f2) {
              return -1;
            }
         if (f1 < f2) {
              return 1;
            }
         return 0;
    }


}
