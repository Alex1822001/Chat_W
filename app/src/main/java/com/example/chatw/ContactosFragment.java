package com.example.chatw;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class ContactosFragment extends Fragment {
    private  View ContactosView;
    private RecyclerView ContacatosLista;
    private DatabaseReference ContactosRef, UserRef;
    private FirebaseAuth auth;
    private String CurrentUserId;
    public ContactosFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ContactosView=inflater.inflate(R.layout.fragment_contactos,container,false);
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos").child(CurrentUserId);
        UserRef=FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContacatosLista=(RecyclerView)ContactosView.findViewById(R.id.contactoslista);
        ContacatosLista.setLayoutManager(new LinearLayoutManager(getContext()));
        return ContactosView;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contactos>().setQuery(ContactosRef,Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos,ContactosViewHolder> adapter=new FirebaseRecyclerAdapter<Contactos, ContactosViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactosViewHolder holder, int position, @NonNull Contactos model) {
                String userIds=getRef(position).getKey();
                UserRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.child("estadoUser").hasChild("estadoAct")){
                                String estadoA=snapshot.child("estadoUser").child("estadoAct").getValue().toString();
                                String fecha=snapshot.child("estadoUser").child("fecha").getValue().toString();
                                String hora=snapshot.child("estadoUser").child("hora").getValue().toString();
                                if(estadoA.equals("activo")){
                                    holder.usuarioactivo.setVisibility(View.VISIBLE);
                                }else if(estadoA.equals("inactivo")){
                                    holder.usuarioactivo.setVisibility(View.GONE);
                                }
                            }else{
                                holder.usuarioactivo.setVisibility(View.GONE);
                            }
                            if(snapshot.hasChild("imagen")){
                                String nombreU=snapshot.child("nombre").getValue().toString();
                                String ciudadU=snapshot.child("ciudad").getValue().toString();
                                String estadoU=snapshot.child("estado").getValue().toString();
                                String imagenU=snapshot.child("imagen").getValue().toString();
                                Picasso.get().load(imagenU).placeholder(R.drawable.error).into(holder.imagen);
                                holder.nombre.setText(nombreU);
                                holder.ciudad.setText(ciudadU);
                                holder.estado.setText(estadoU);

                            }else{
                                String nombreU=snapshot.child("nombre").getValue().toString();
                                String ciudadU=snapshot.child("ciudad").getValue().toString();
                                String estadoU=snapshot.child("estado").getValue().toString();
                                holder.nombre.setText(nombreU);
                                holder.ciudad.setText(ciudadU);
                                holder.estado.setText(estadoU);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            @NonNull
            @Override
            public ContactosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_layout,parent,false);
                ContactosViewHolder viewHolder=new ContactosViewHolder(view);
                return viewHolder;
            }
        };
        ContacatosLista.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactosViewHolder extends RecyclerView.ViewHolder{
        TextView nombre, ciudad, estado;
        CircleImageView imagen;
        ImageView usuarioactivo;
        public ContactosViewHolder (View itemView){
            super(itemView);
            nombre=itemView.findViewById(R.id.user_nombre);
            ciudad=itemView.findViewById(R.id.user_ciudad);
            estado=itemView.findViewById(R.id.user_estado);
            imagen=itemView.findViewById(R.id.user_imagen_perfil);
            usuarioactivo=itemView.findViewById(R.id.user_activo);
        }
    }
}