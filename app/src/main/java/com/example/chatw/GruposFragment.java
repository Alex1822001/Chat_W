package com.example.chatw;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GruposFragment extends Fragment {

    private View grupoFracmentoView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listaGrupo = new ArrayList<>();
    private DatabaseReference GrupoRef;
    public GruposFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        grupoFracmentoView= inflater.inflate(R.layout.fragment_grupos,container,false);
        GrupoRef= FirebaseDatabase.getInstance().getReference().child("Grupos");
        IniciarLista();
        MostraListaG();
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String CurrenGrupoNombre = parent.getItemAtPosition(position).toString();
                Intent intent = new Intent(getContext(), GrupoChatActivity.class);
                intent.putExtra("nombregrupo",CurrenGrupoNombre);
                startActivity(intent);
            }
        });
        return grupoFracmentoView;
    }
    private void MostraListaG() {
        GrupoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = (Iterator) snapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                listaGrupo.clear();
                listaGrupo.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void IniciarLista(){
        list_view=(ListView) grupoFracmentoView.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,listaGrupo);
        list_view.setAdapter(arrayAdapter);

    }
}