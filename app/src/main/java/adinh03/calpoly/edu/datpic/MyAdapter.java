package adinh03.calpoly.edu.datpic;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Anthony on 10/26/2016.
 */

public class MyAdapter extends RecyclerView.Adapter<EntryViewHolder> {
   private ArrayList<Entry> mEntry;

   public MyAdapter(ArrayList<Entry> entry) { mEntry = entry;}

   @Override
   public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new EntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
   }

   @Override
   public void onBindViewHolder(EntryViewHolder holder, int position) {

   }

   @Override
   public int getItemCount() {
      return 0;
   }
}
