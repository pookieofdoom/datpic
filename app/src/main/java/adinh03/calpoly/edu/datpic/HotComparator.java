package adinh03.calpoly.edu.datpic;

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
      Double o1;
      Double o2;
      if (lhs.getLikeCount() == 0)
      {
         if (lhs.getDislikeCount() != 0)
            o1 = -1.0 * lhs.getLikeCount();
         else
            o1 = 0.0;

      }
      else
      {
         if (rhs.getDislikeCount() !=0)
            o1 = 1.0 * lhs.getLikeCount() / lhs.getDislikeCount();
         else
            o1 = 1.0 * lhs.getLikeCount();
      }

      if (rhs.getLikeCount() == 0)
      {
         if (rhs.getDislikeCount() !=0)
            o2 = -1.0 * rhs.getDislikeCount();
         else
            o2 = 0.0;
      }
      else
      {
         if (rhs.getDislikeCount() !=0)
            o2 = 1.0 * rhs.getLikeCount() / rhs.getDislikeCount();
         else
            o2 = 1.0 * rhs.getLikeCount();
      }
      return o2.compareTo(o1);
   }
}
