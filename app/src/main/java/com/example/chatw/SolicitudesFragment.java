package com.example.chatw;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SolicitudesFragment extends Fragment {
    private View SolicitudesFragmentView;
    private RecyclerView ReciclerSolicitudesLista;
    private DatabaseReference SolicitudesRef, UseRef, ContactosRef;
    private FirebaseAuth auth;
    private String CurrentUserId;
    public SolicitudesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SolicitudesFragmentView=inflater.inflate(R.layout.fragment_solicitudes,container,false);
        ReciclerSolicitudesLista=(RecyclerView)SolicitudesFragmentView.findViewById(R.id.reciclersolicitudeslista);
        ReciclerSolicitudesLista.setLayoutManager(new LinearLayoutManager(getContext()));
        SolicitudesRef=FirebaseDatabase.getInstance().getReference().child("Solicitudes");
        UseRef=FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContactosRef=FirebaseDatabase.getInstance().getReference().child("Contactos");
        auth=FirebaseAuth.getInstance();
        CurrentUserId=auth.getCurrentUser().getUid();
        return SolicitudesFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contactos> options = new FirebaseRecyclerOptions.Builder<Contactos>().setQuery(SolicitudesRef.child(CurrentUserId),Contactos.class).build();
        FirebaseRecyclerAdapter<Contactos,SolicitudesViewHolder>adapter=new FirebaseRecyclerAdapter<Contactos, SolicitudesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SolicitudesViewHolder holder, int position, @NonNull Contactos model) {
                holder.itemView.findViewById(R.id.Solicitud_aceptarB).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.Solicitud_cancelarB).setVisibility(View.VISIBLE);
                final String user_id= getRef(position).getKey();
                DatabaseReference getTipo= getRef(position).child("tipo").getRef();
                getTipo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String tipo = snapshot.getValue().toString();
                            if(tipo.equals("Recibido")){
                                UseRef.child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("imagen")) {

                                            String img = snapshot.child("imagen").getValue().toString();
                                            Picasso.get().load(img).placeholder(R.drawable.error).into(holder.imagenes);
                                        }
                                            final String nom=snapshot.child("nombre").getValue().toString();
                                            String ciu=snapshot.child("ciudad").getValue().toString();
                                            String est=snapshot.child("estado").getValue().toString();
                                            holder.nombres.setText(nom);
                                            holder.ciudades.setText(ciu);
                                            holder.estados.setText("Quiere contatactar contigo");
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence opciones []= new CharSequence[]{
                                                        "Aceptar",
                                                        "Cancelar"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Solicitud de "+nom);
                                                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i==0){
                                                            ContactosRef.child(CurrentUserId).child(user_id).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        ContactosRef.child(user_id).child(CurrentUserId).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){
                                                                                    SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        Toast.makeText(getContext(),"Nuevo Contacto Agregado",Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        if(i==1){
                                                            SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(getContext(),"Solicitud Eliminada",Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }});
                            }else if (tipo.equals("enviado")){
                                Button solicitud_aceptar=holder.itemView.findViewById(R.id.Solicitud_aceptarB);
                                solicitud_aceptar.setText("Enviado");
                                holder.itemView.findViewById(R.id.Solicitud_cancelarB).setVisibility(View.GONE);
                                UseRef.child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("imagen")) {

                                            String img = snapshot.child("imagen").getValue().toString();
                                            Picasso.get().load(img).placeholder(R.drawable.error).into(holder.imagenes);
                                        }
                                        final String nom=snapshot.child("nombre").getValue().toString();
                                        String ciu=snapshot.child("ciudad").getValue().toString();
                                        String est=snapshot.child("estado").getValue().toString();
                                        holder.nombres.setText(nom);
                                        holder.ciudades.setText(ciu);
                                        holder.estados.setText("Enviaste una solicitud a:"+nom);
                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence opciones []= new CharSequence[]{
                                                        "Cancelar"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("¿Seguro de cancelar?");
                                                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int i) {
                                                        if(i==0){
                                                            SolicitudesRef.child(CurrentUserId).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        SolicitudesRef.child(user_id).child(CurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(getContext(),"Cancelaste la solicitud",Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }

                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }});
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }});
            }
            @NonNull
            @Override
            public SolicitudesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view =LayoutInflater.from(getContext()).inflate(R.layout.user_display_layout,parent,false);
                SolicitudesViewHolder viewHolder = new SolicitudesViewHolder(view);
                return viewHolder;
            }
        };
        ReciclerSolicitudesLista.setAdapter(adapter);
        adapter.startListening();
    }
    private static class SolicitudesViewHolder extends RecyclerView.ViewHolder{
        TextView nombres, ciudades, estados;
        CircleImageView imagenes;
        Button aceptar, cancelar;
        public SolicitudesViewHolder(@NonNull View itemView) {
            super(itemView);
            nombres=itemView.findViewById(R.id.user_nombre);
            ciudades=itemView.findViewById(R.id.user_ciudad);
            estados=itemView.findViewById(R.id.user_estado);
            imagenes=itemView.findViewById(R.id.user_imagen_perfil);
            aceptar=itemView.findViewById(R.id.Solicitud_aceptarB);
            cancelar=itemView.findViewById(R.id.Solicitud_cancelarB);
        }
    }
}