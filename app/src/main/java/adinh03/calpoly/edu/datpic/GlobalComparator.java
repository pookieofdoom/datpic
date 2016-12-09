package adinh03.calpoly.edu.datpic;

import android.util.Log;

import java.util.Comparator;

/**
 * Created by Anthony on 12/7/2016.
 */

//this comparator will list all images based on upload time
public class GlobalComparator implements Comparator<Entry>
{
   @Override
   public int compare(Entry lhs, Entry rhs)
   {
      Long o1 = lhs.getUploadTime();
      Long o2 = rhs.getUploadTime();
      if(o1 == o2) {
         if(lhs.getUrl() == null) {
            return -1;
         } else if(rhs.getUrl() == null) {
            return 1;
         } else {
            return 0;
         }
      }
      return o2.compareTo(o1);
   }
}
