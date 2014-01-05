
package kickstater;

import java.util.Comparator;

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
