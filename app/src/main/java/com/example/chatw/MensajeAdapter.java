package com.example.chatw;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajesViewHolder> {
    private List<Mensajes> usuarioMensajes;
    private FirebaseAuth auth;
    private DatabaseReference UserRef;
    public MensajeAdapter(List<Mensajes> usuarioMensajes){
        this.usuarioMensajes=usuarioMensajes;
    }
    public class MensajesViewHolder extends RecyclerView.ViewHolder{
        public TextView enviarMensajeTexto, recibirMensajeTexto;
        public CircleImageView recibirImagenPerfil;
        public ImageView mensajeImagenEnviar, mensajeImagenRecibir;
        public MensajesViewHolder(@NonNull View itemView) {
            super(itemView);
            enviarMensajeTexto=(TextView)itemView.findViewById(R.id.enviar_mensaje);
            recibirMensajeTexto=(TextView)itemView.findViewById(R.id.recibir_mensaje);
            recibirImagenPerfil=(CircleImageView)itemView.findViewById(R.id.mensaje_imagen_perfil);
            mensajeImagenEnviar=(ImageView)itemView.findViewById(R.id.mensajeenviarimagen);
            mensajeImagenRecibir=(ImageView)itemView.findViewById(R.id.mensajerecibirimagen);
        }
    }
    @NonNull
    @Override
    public MensajesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.usuario_mensaje_layout,parent,false);
        auth=FirebaseAuth.getInstance();
        return new MensajesViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MensajesViewHolder holder, int i) {
        String mensajeEnviadoID=auth.getCurrentUser().getUid();
        Mensajes mensajes =usuarioMensajes.get(i);
        String DeUsuarioId=mensajes.getDe();
        String TipoMensaje=mensajes.getTipo();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuario").child(DeUsuarioId);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("imagen")){
                    String ImagenRecibido=snapshot.child("imagen").getValue().toString();
                    Picasso.get().load(ImagenRecibido).placeholder(R.drawable.error).into(holder.recibirImagenPerfil);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
        holder.recibirMensajeTexto.setVisibility(View.GONE);
        holder.recibirImagenPerfil.setVisibility(View.GONE);
        holder.enviarMensajeTexto.setVisibility(View.GONE);
        holder.mensajeImagenRecibir.setVisibility(View.GONE);
        holder.mensajeImagenEnviar.setVisibility(View.GONE);
        if(TipoMensaje.equals("texto")){
            if(DeUsuarioId.equals(mensajeEnviadoID)){
                holder.enviarMensajeTexto.setVisibility(View.VISIBLE);
                holder.enviarMensajeTexto.setBackgroundResource(R.drawable.enviar_mensaje_layout);
                holder.enviarMensajeTexto.setTextColor(Color.WHITE);
                holder.enviarMensajeTexto.setText(mensajes.getMensaje()+"\n\n"+mensajes.getFecha()+"-"+mensajes.getHora());
            }else{
                holder.recibirImagenPerfil.setVisibility(View.GONE);
                holder.recibirMensajeTexto.setVisibility(View.VISIBLE);
                holder.recibirMensajeTexto.setBackgroundResource(R.drawable.recibir_mensaje_layout);
                holder.recibirMensajeTexto.setTextColor(Color.BLACK);
                holder.recibirMensajeTexto.setText(mensajes.getMensaje()+"\n\n"+mensajes.getFecha()+"-"+mensajes.getHora());
            }
        }else if(TipoMensaje.equals("imagen")){
            if(DeUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load(mensajes.getMensaje()).into(holder.mensajeImagenEnviar);
            }else{
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);
                Picasso.get().load(mensajes.getMensaje()).into(holder.mensajeImagenRecibir);
            }
        }else if(TipoMensaje.equals("pdf")||TipoMensaje.equals("docx")){
            if(DeUsuarioId.equals(mensajeEnviadoID)){
                holder.mensajeImagenEnviar.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chat-w-2fcc1.appspot.com/o/Archivo%2FArchivos.png?alt=media&token=e2301f99-e795-4e2f-9ddc-6df5e8e90edf").into(holder.mensajeImagenEnviar);
                
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(i).getMensaje()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }else {
                holder.recibirImagenPerfil.setVisibility(View.VISIBLE);
                holder.mensajeImagenRecibir.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chat-w-2fcc1.appspot.com/o/Archivo%2FArchivos.png?alt=media&token=e2301f99-e795-4e2f-9ddc-6df5e8e90edf").into(holder.mensajeImagenRecibir);
            }

        }
        if(DeUsuarioId.equals(mensajeEnviadoID)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (usuarioMensajes.get(i).getTipo().equals("pdf") || usuarioMensajes.get(i).getTipo().equals("docx")) {
                        CharSequence opciones[] = new CharSequence[]{
                                "Eliminar para mi",
                                "Descargar y Ver",
                                "Cancelar",
                                "Eliminar para todos"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    EliminarMensajesEnviados(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (position == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(i).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (position == 3) {
                                    EliminarMensajesTodos(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (usuarioMensajes.get(i).getTipo().equals("texto")) {
                        CharSequence opciones[] = new CharSequence[]{
                                "Eliminar para mi",
                                "Cancelar",
                                "Eliminar para todos"

                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    EliminarMensajesEnviados(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }else if (position == 2) {
                                    EliminarMensajesTodos(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (usuarioMensajes.get(i).getTipo().equals("imagen")) {
                        CharSequence opciones[] = new CharSequence[]{
                                "Eliminar para mi",
                                "Ver Imagen",
                                "Cancelar",
                                "Eliminar para todos"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    EliminarMensajesEnviados(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (position == 1) {
                                    Intent intent= new Intent(holder.itemView.getContext(),ImagenActivity.class);
                                    intent.putExtra("url",usuarioMensajes.get(i).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (position == 3) {
                                    EliminarMensajesTodos(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
            ///Recibidos///
        }else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (usuarioMensajes.get(i).getTipo().equals("pdf") || usuarioMensajes.get(i).getTipo().equals("docx")) {
                        CharSequence opciones[] = new CharSequence[]{
                                "Eliminar para mi",
                                "Descargar y Ver",
                                "Cancelar"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    EliminarMensajesRecibido(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (position == 1) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usuarioMensajes.get(i).getMensaje()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (usuarioMensajes.get(i).getTipo().equals("texto")) {
                        CharSequence opciones[] = new CharSequence[]{
                                "Eliminar para mi",
                                "Cancelar"

                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    EliminarMensajesRecibido(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    } else if (usuarioMensajes.get(i).getTipo().equals("imagen")) {
                        CharSequence opciones[] = new CharSequence[]{
                                "Eliminar para mi",
                                "Ver Imagen",
                                "Cancelar"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setItems(opciones, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if (position == 0) {
                                    EliminarMensajesRecibido(i,holder);
                                    Intent intent= new Intent(holder.itemView.getContext(),InicioActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                } else if (position == 1) {
                                    Intent intent= new Intent(holder.itemView.getContext(),ImagenActivity.class);
                                    intent.putExtra("url",usuarioMensajes.get(i).getMensaje());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }



    @Override
    public int getItemCount() {
        return usuarioMensajes.size();
    }
    private void EliminarMensajesEnviados(final int position, final MensajesViewHolder holder){
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Mensajes").child(usuarioMensajes.get(position).getDe())
                .child(usuarioMensajes.get(position).getPara())
                .child(usuarioMensajes.get(position).getMensajeID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Mensaje Eliminado",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(),"Error al eliminar",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void EliminarMensajesRecibido(final int position, final MensajesViewHolder holder){
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Mensajes").child(usuarioMensajes.get(position).getPara())
                .child(usuarioMensajes.get(position).getDe())
                .child(usuarioMensajes.get(position).getMensajeID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(),"Mensaje Eliminado",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(holder.itemView.getContext(),"Error al eliminar",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void EliminarMensajesTodos(final int position, final MensajesViewHolder holder){
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("Mensajes").child(usuarioMensajes.get(position).getPara())
                .child(usuarioMensajes.get(position).getDe())
                .child(usuarioMensajes.get(position).getMensajeID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    rootRef.child("Mensajes").child(usuarioMensajes.get(position).getDe())
                            .child(usuarioMensajes.get(position).getPara())
                            .child(usuarioMensajes.get(position).getMensajeID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(),"Mensaje Eliminado",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(holder.itemView.getContext(),"Error al eliminar",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
