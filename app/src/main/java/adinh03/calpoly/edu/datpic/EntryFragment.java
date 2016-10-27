package adinh03.calpoly.edu.datpic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Anthony on 10/26/16.
 */

public class EntryFragment extends Fragment {

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.entry, container, false);
      //return super.onCreateView(inflater, container, savedInstanceState);
      RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
      recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
      ArrayList<Entry> a = new ArrayList<Entry>();
      MyAdapter adapter = new MyAdapter(a);
      recyclerView.setAdapter(adapter);

      return rootView;
   }
}
