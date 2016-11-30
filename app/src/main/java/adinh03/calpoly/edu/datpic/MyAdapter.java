package adinh03.calpoly.edu.datpic;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static adinh03.calpoly.edu.datpic.R.layout.entry;

/**
 * Created by Anthony on 10/26/2016.
 */

public class MyAdapter extends RecyclerView.Adapter<EntryViewHolder>
{
   private ArrayList<Entry> mEntry;
   private User mCurrentUser;

//   private ValueEventListener valueEventListener = new ValueEventListener()
//   {
//      @Override
//      public void onDataChange(DataSnapshot dataSnapshot)
//      {
//
//      }
//
//      @Override
//      public void onCancelled(DatabaseError databaseError)
//      {
//
//      }
//   };
//
//   private View.OnClickListener likeClickListener = new View.OnClickListener()
//   {
//      @Override
//      public void onClick(View v)
//      {
//         EntryViewHolder viewHolder = (EntryViewHolder) v.getTag(R.string.viewHolder);
//         User user = (User) v.getTag();
//
//
//         boolean liked = !v.isSelected();
//         v.setSelected(liked);
//         setLike(liked, user);
//      }
//   };



   public MyAdapter(ArrayList<Entry> entry, User currentUser)
   {
      mEntry = entry;
      mCurrentUser = currentUser;
   }

   @Override
   public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
   {
      return new EntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType,
            parent, false), mCurrentUser);
   }

   @Override
   public void onBindViewHolder(EntryViewHolder holder, int position)
   {
      Entry entry = mEntry.get(position);
      holder.bind(entry);
   }

   @Override
   public int getItemCount()
   {
      return mEntry.size();
   }

   @Override
   public int getItemViewType(int position)
   {
      return entry;
   }

}
