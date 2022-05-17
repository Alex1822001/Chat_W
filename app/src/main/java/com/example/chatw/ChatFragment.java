package com.example.chatw;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    private View ChatViewUnica;
    private RecyclerView ChatLista;
    private DatabaseReference ContactosRef, UserRef;
    private FirebaseAuth auth;
    private String CurrentUserId;
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos").child(CurrentUserId);
        UserRef=FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ChatViewUnica=inflater.inflate(R.layout.fragment_chat,container,false);
        ChatLista=(RecyclerView)ChatViewUnica.findViewById(R.id.chat_lista);
        ChatLista.setLayoutManager(new LinearLayoutManager(getContext()));
        return ChatViewUnica;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contactos> options= new FirebaseRecyclerOptions.Builder<Contactos>().setQuery(ContactosRef,Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos, ChatsViewHolder> adapter= new FirebaseRecyclerAdapter<Contactos, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int i, @NonNull Contactos model) {
                final String usedIds = getRef(i).getKey();
                final String[] imagens = {"default"};
                UserRef.child(usedIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                       if(snapshot.exists()){
                           if(snapshot.hasChild("imagen")){
                                imagens[0] =snapshot.child("imagen").getValue().toString();
                               Picasso.get().load(imagens[0]).placeholder(R.drawable.logo).into(holder.imagenC);
                           }
                           final String nombres = snapshot.child("nombre").getValue().toString();
                           String ciudads = snapshot.child("ciudad").getValue().toString();
                           String estados = snapshot.child("estado").getValue().toString();
                           holder.nombreC.setText(nombres);
                           holder.ciudadC.setText(ciudads);
                           holder.estadoC.setText("Ultima conexion"+"Hora");
                           if(snapshot.child("estadoUser").hasChild("estadoAct")){
                               String estadoUser=snapshot.child("estadoUser").child("estadoAct").getValue().toString();
                               String fecha=snapshot.child("estadoUser").child("fecha").getValue().toString();
                               String hora=snapshot.child("estadoUser").child("hora").getValue().toString();
                               if(estadoUser.equals("activo")){
                                   holder.estadoC.setText("En Linea");
                               }else if(estadoUser.equals("inactivo")){
                                   holder.estadoC.setText("Ultima conexion:\n"+fecha+"\n"+hora);
                               }
                           }else{
                               holder.estadoC.setText("inactivo");
                           }

                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   Intent intent = new Intent(getContext(),ChatActivity.class);
                                   intent.putExtra("user_id",usedIds);
                                   intent.putExtra("user_nombre",nombres);
                                   intent.putExtra("user_imagen", imagens[0]);
                                   startActivity(intent);
                               }
                           });
                       }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }});
            }
            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                return new ChatsViewHolder(view);
            }
        };
        ChatLista.setAdapter(adapter);
        adapter.startListening();
    }
    public  static class ChatsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imagenC;
        TextView nombreC, ciudadC, estadoC;
        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenC=itemView.findViewById(R.id.user_imagen_perfil);
            nombreC=itemView.findViewById(R.id.user_nombre);
            ciudadC=itemView.findViewById(R.id.user_ciudad);
            estadoC=itemView.findViewById(R.id.user_estado);
        }
    }
}