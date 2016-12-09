package adinh03.calpoly.edu.datpic;

import android.util.Log;

import java.util.Comparator;

/**
 * Created by Anthony Dinh on 12/4/2016.
 */

//this comparator puts the highest ratio at the top = position 0
public class HotComparator implements Comparator<Entry>
{

   @Override
   public int compare(Entry lhs, Entry rhs)
   {
      return  getRatio(rhs).compareTo(getRatio(lhs));
   }

   public Double getRatio(Entry entry) {
      if (entry.getLikeCount() == 0)
      {
         if (entry.getDislikeCount() != 0)
            return -1.0 * entry.getDislikeCount();
         else
            return 0.0;

      }
      else
      {
         if (entry.getDislikeCount() != 0)
            return 1.0 * entry.getLikeCount() / entry.getDislikeCount();
         else
            return 1.0 * entry.getLikeCount();
      }
   }
}
