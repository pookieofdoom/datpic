package adinh03.calpoly.edu.datpic;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
   private FirebaseDatabase mDataBase;

   private View.OnClickListener likeClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View v)
      {
         EntryViewHolder holder = (EntryViewHolder) v.getTag(R.string.viewHolder);
         User user = (User) v.getTag(R.string.currentUser);
         boolean liked = !v.isSelected();
         v.setSelected(liked);
         setLike(holder.getAdapterPosition(), liked, user);
      }
   };

   private View.OnClickListener commentClickListener = new View.OnClickListener()
   {
      @Override
      public void onClick(View view)
      {
         viewThisComment(view);
      }
   };

   private void viewThisComment(View view)
   {
      Context viewHolderContext = view.getContext();
      EntryViewHolder holder = (EntryViewHolder) view.getTag(R.string.viewHolder);
      Intent intent = new Intent(viewHolderContext, AddCommentActivity.class);
      System.out.println("position: " + holder.getAdapterPosition());
      intent.putExtra("clickedImageIndex", holder.getAdapterPosition());
      viewHolderContext.startActivity(intent);

   }

   private void setLike(int position, boolean isLike, User currentUser)
   {
      String path = StaticEntryList.getInstance().getMap().get(StaticEntryList.getInstance()
            .getEntry(position).getUri().toString());
      DatabaseReference setLike = mDataBase.getInstance().getReference("users")
            .child(currentUser.getId());
      DatabaseReference counterLike = mDataBase.getInstance().getReference("images").child(path)
            .child("LikeCount");

      //like counter breaks when going to different views

      if (isLike)
      {
         setLike.child("Like").child(path).setValue(true);
         counterLike.addListenerForSingleValueEvent(new ValueEventListener()
         {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               long value = (long) dataSnapshot.getValue();
               dataSnapshot.getRef().setValue(++value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
         });
      }
      else if (!isLike)
      {
         setLike.child("Like").child(path).removeValue();
         counterLike.addListenerForSingleValueEvent(new ValueEventListener()
         {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
               long value = (long) dataSnapshot.getValue();
               dataSnapshot.getRef().setValue(--value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
         });
      }


      //counterLike.addListenerForSingleValueEvent(valueEventListener);
   }

   public MyAdapter(ArrayList<Entry> entry, User currentUser)
   {
      mEntry = entry;
      mCurrentUser = currentUser;
   }

   @Override
   public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
   {
      return new EntryViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType,
            parent, false), mCurrentUser, commentClickListener, likeClickListener);
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
