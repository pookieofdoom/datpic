package adinh03.calpoly.edu.datpic;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Dylan on 10/30/16.
 */

public class StaticEntryList {
   private ArrayList<Entry> mEntry;
   private HashMap<String, String> mMap;


   public void setMap(String key, String value) {
      mMap.put(key, value);
   }

   public HashMap<String, String> getMap() {
      return mMap;
   }

   public void setEntry(ArrayList<Entry> in) {
      mEntry = in;
   }

   public Entry getEntry(int pos) {
      return mEntry.get(pos);
   }

   private static final StaticEntryList staticEntryList = new StaticEntryList();

   public static StaticEntryList getInstance() {
      return staticEntryList;
   }

   public StaticEntryList() {
      mMap = new HashMap<String, String>();
   }

}
