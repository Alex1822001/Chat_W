package com.example.chatw;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
public class PerfilActivity extends AppCompatActivity {
    private String usuario_revid, usuario_enviarid, CurrenEstado, CurrenUserId;
    private CircleImageView usuarioima;
    private TextView usuarionom,usuariociu, usuarioest, usuarioeda;
    private Button enviarmensaje, cancelarmensaje;
    private FirebaseAuth auth;
    private DatabaseReference UserRef, SolicitudRef, ContactosRef, NotificacionesRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        usuario_revid=getIntent().getExtras().get("usuario_id").toString();
        auth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        ContactosRef= FirebaseDatabase.getInstance().getReference().child("Contactos");
        SolicitudRef=FirebaseDatabase.getInstance().getReference().child("Solicitudes");
        NotificacionesRef=FirebaseDatabase.getInstance().getReference().child("Notificaciones");
        usuario_enviarid=auth.getCurrentUser().getUid();
        usuarionom=(TextView)findViewById(R.id.usuario_vic_nombre);
        usuariociu=(TextView)findViewById(R.id.usuario_vic_ciudad);
        usuarioest=(TextView)findViewById(R.id.usuario_vic_estado);
        usuarioeda=(TextView)findViewById(R.id.usuario_vic_edad);
        usuarioima=(CircleImageView)findViewById(R.id.usuario_vic_id);
        enviarmensaje=(Button)findViewById(R.id.usuario_vic_enviarmensaje);
        cancelarmensaje=(Button)findViewById(R.id.usuario_vic_cancelar_mensaje);
        CurrenEstado="nuevo";
        ObtenerInformacionDB();
    }
    private void ObtenerInformacionDB() {
        UserRef.child(usuario_revid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists())&&(snapshot.hasChild("imagen"))){
                    String nombreUser=snapshot.child("nombre").getValue().toString();
                    String ciudadUser=snapshot.child("ciudad").getValue().toString();
                    String estadoUser=snapshot.child("estado").getValue().toString();
                    String imagenUser=snapshot.child("imagen").getValue().toString();
                    String edadUser=snapshot.child("Edad").getValue().toString();
                    Picasso.get().load(imagenUser).placeholder(R.drawable.error).into(usuarioima);
                    usuarionom.setText(nombreUser);
                    usuariociu.setText(ciudadUser);
                    usuarioest.setText(estadoUser);
                    usuarioeda.setText(edadUser);
                    EnviarRequerimiento();
                }else{
                    String nombreUser=snapshot.child("nombre").getValue().toString();
                    String ciudadUser=snapshot.child("ciudad").getValue().toString();
                    String estadoUser=snapshot.child("estado").getValue().toString();
                    String edadUser=snapshot.child("Edad").getValue().toString();
                    usuarionom.setText(nombreUser);
                    usuariociu.setText(ciudadUser);
                    usuarioest.setText(estadoUser);
                    usuarioeda.setText(edadUser);
                    EnviarRequerimiento();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void EnviarRequerimiento() {
        SolicitudRef.child(usuario_enviarid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(usuario_revid)){
                    String requerimiento=snapshot.child(usuario_revid).child("tipo").getValue().toString();
                    if(requerimiento.equals("enviado")){
                        CurrenEstado="enviada";
                        enviarmensaje.setText("Cancelar Solicitud");
                    }
                    else if(requerimiento.equals("Recibido")){
                        CurrenEstado="recibida";
                        enviarmensaje.setText("Aceptar Solicitud");
                        cancelarmensaje.setVisibility(View.VISIBLE);
                        cancelarmensaje.setEnabled(true);
                        cancelarmensaje.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelarSolicitudM();
                            }
                        });
                    }
                }else{
                    ContactosRef.child(usuario_enviarid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(usuario_revid)){
                                CurrenEstado="amigos";
                                enviarmensaje.setText("Eliminar Contacto");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if(!usuario_enviarid.equals(usuario_revid)){
            enviarmensaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enviarmensaje.setEnabled(false);
                    if(CurrenEstado.equals("nuevo")){
                        EnviarSolicitarM();
                    }
                    if(CurrenEstado.equals("enviada")){
                        CancelarSolicitudM();
                    }
                    if(CurrenEstado.equals("recibida")){
                        AceptarSolicitudM();
                    }
                    if(CurrenEstado.equals("amigos")){
                        EliminarContacto();
                    }
                }
            });
        }else{
            enviarmensaje.setVisibility(View.GONE);
        }
    }

    private void EliminarContacto() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ContactosRef.child(usuario_revid).child(usuario_enviarid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                enviarmensaje.setEnabled(true);
                                CurrenEstado="nueva";
                                enviarmensaje.setText("Enviar Mensaje");
                                cancelarmensaje.setVisibility(View.GONE);
                                cancelarmensaje.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AceptarSolicitudM() {
        ContactosRef.child(usuario_enviarid).child(usuario_revid).child("Contactos").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    ContactosRef.child(usuario_revid).child(usuario_enviarid).child("Contacto").setValue("Aceptado").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SolicitudRef.child(usuario_enviarid).child(usuario_revid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            SolicitudRef.child(usuario_revid).child(usuario_enviarid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    enviarmensaje.setEnabled(true);
                                                    CurrenEstado="amigo";
                                                    enviarmensaje.setText("Eliminar Contacto");
                                                    cancelarmensaje.setVisibility(View.GONE);
                                                    cancelarmensaje.setEnabled(false);
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

    private void CancelarSolicitudM() {
        SolicitudRef.child(usuario_enviarid).child(usuario_revid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    SolicitudRef.child(usuario_revid).child(usuario_enviarid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                enviarmensaje.setEnabled(true);
                                CurrenEstado="nueva";
                                enviarmensaje.setText("Enviar Mensaje");
                                cancelarmensaje.setVisibility(View.GONE);
                                cancelarmensaje.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }
    private void EnviarSolicitarM() {
        SolicitudRef.child(usuario_enviarid).child(usuario_revid).child("tipo").setValue("enviado").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    SolicitudRef.child(usuario_revid).child(usuario_enviarid).child("tipo").setValue("Recibido").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                HashMap<String,String>chatNotification=new HashMap<>();
                                chatNotification.put("de",usuario_revid);
                                chatNotification.put("tipo","requerimiento");
                                NotificacionesRef.child(usuario_enviarid).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            enviarmensaje.setEnabled(true);
                                            CurrenEstado="vieja";
                                            enviarmensaje.setText("Cancelar Requerimiento");
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
}