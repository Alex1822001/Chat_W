package com.example.chatw;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.annotations.NonNull;

public class ChatActivity extends AppCompatActivity {
    private String RecibirUserID, nombre, imagens;
    private TextView nombreusuario, ultimaconexion;
    private CircleImageView usuarioimagen;
    private Toolbar toolbar;
    private EditText mensaje;
    private ImageView botonenviar,botonarchivo;
    private DatabaseReference RootRef;
    private FirebaseAuth auth;
    private String EnviarUserID;
    private final List<Mensajes> mensajesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MensajeAdapter mensajeAdapter;
    private RecyclerView UsuariosrecyclerView;
    private String CurrentTime, CurrentDate;
    private String check="",myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth=FirebaseAuth.getInstance();
        EnviarUserID=auth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

        RecibirUserID=getIntent().getExtras().get("user_id").toString();
        nombre=getIntent().getExtras().get("user_nombre").toString();
        imagens=getIntent().getExtras().get("user_imagen").toString();

        IniciareLayout();
        nombreusuario.setText(nombre);
        Picasso.get().load(imagens).placeholder(R.drawable.logo).into(usuarioimagen);
        dialog=new ProgressDialog(this);
        mensajeAdapter=new MensajeAdapter(mensajesList);
        UsuariosrecyclerView=(RecyclerView)findViewById(R.id.listamensajesrecicler);
        linearLayoutManager=new LinearLayoutManager(this);
        UsuariosrecyclerView.setLayoutManager(linearLayoutManager);
        UsuariosrecyclerView.setAdapter(mensajeAdapter);
        MetodoConexion();
        RootRef.child("Mensajes").child(EnviarUserID).child(RecibirUserID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensajes mensajes=snapshot.getValue(Mensajes.class);
                mensajesList.add(mensajes);
                mensajeAdapter.notifyDataSetChanged();
                UsuariosrecyclerView.smoothScrollToPosition(UsuariosrecyclerView.getAdapter().getItemCount());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void MetodoConexion(){
        RootRef.child("Usuarios").child(RecibirUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if(snapshot.child("estadoUser").hasChild("estadoAct")){
                    String estadoUser=snapshot.child("estadoUser").child("estadoAct").getValue().toString();
                    String fecha=snapshot.child("estadoUser").child("fecha").getValue().toString();
                    String hora=snapshot.child("estadoUser").child("hora").getValue().toString();
                    if(estadoUser.equals("activo")){
                        ultimaconexion.setText("En linea");
                    }else if(estadoUser.equals("inactivo")){
                        ultimaconexion.setText("Ultima conexion:"+fecha+"\n"+hora);
                    }
                }else{
                    ultimaconexion.setText("inactivo");
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }
    private void IniciareLayout() {
        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        CurrentDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1= new SimpleDateFormat("hh:mm a");
        CurrentTime=dateFormat1.format(calendar.getTime());
        toolbar=(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_chat_bar,null);
        actionBar.setCustomView(view);
        nombreusuario=(TextView)findViewById(R.id.usuario_nombre);
        ultimaconexion=(TextView)findViewById(R.id.usuario_conexion);
        usuarioimagen=(CircleImageView)findViewById(R.id.usuario_imagen);
        mensaje=(EditText)findViewById(R.id.mensaje);
        botonenviar=(ImageView)findViewById(R.id.enviar_mensaje_boton);
        botonarchivo=(ImageView)findViewById(R.id.enviar_archivosB);
        botonenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarMensaje();
            }
        });
        botonarchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence opciones[]=new CharSequence[]{
                        "Imagenes",
                        "PDF",
                        "Word"
                };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Selecciona el archivo");
                builder.setItems(opciones, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(i==0){
                            check="imagen";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Seleccionar imagen"),438);
                        }
                        if(i==1){
                            check="pdf";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Seleccionar un archivo PDF"),438);
                        }
                        if(i==2){
                            check="docx";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/docx");
                            startActivityForResult(intent.createChooser(intent,"Seleccionar un archivo DOCX"),438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData() !=null){
            dialog.setTitle("Enviando imagen");
            dialog.setMessage("Enviando");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            fileUri=data.getData();
            if(!check.equals("imagen")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Documentos");
                String mensajeEnviadoRef="Mensajes/"+EnviarUserID+"/"+RecibirUserID;
                String mensajeRecibidoRef="Mensajes/"+RecibirUserID+"/"+EnviarUserID;
                DatabaseReference usuarioMensajeRef=RootRef.child("Mensajes").child(EnviarUserID).child(RecibirUserID).push();
                String mensajePushID=usuarioMensajeRef.getKey();
                final StorageReference filePath= storageReference.child(mensajePushID+"."+check);
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            uploadTask=filePath.putFile(fileUri);
                            uploadTask.continueWithTask(new Continuation() {
                                @Override
                                public Object then(@NonNull Task task) throws Exception {
                                    if(!task.isSuccessful()){
                                        throw task.getException();
                                    }
                                    return  filePath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Uri downloadUrid=task.getResult();
                                        myUrl=downloadUrid.toString();
                                        Map mensajeTxt= new HashMap();
                                        mensajeTxt.put("mensaje",myUrl);
                                        mensajeTxt.put("tipo",check);
                                        mensajeTxt.put("de",EnviarUserID);
                                        mensajeTxt.put("para",RecibirUserID);
                                        mensajeTxt.put("mensajeID",mensajePushID);
                                        mensajeTxt.put("fecha",CurrentDate);
                                        mensajeTxt.put("hora",CurrentTime);
                                        Map mensajeTxtfull= new HashMap();
                                        mensajeTxtfull.put(mensajeEnviadoRef+"/"+mensajePushID,mensajeTxt);
                                        mensajeTxtfull.put(mensajeRecibidoRef+"/"+mensajePushID,mensajeTxt);
                                        RootRef.updateChildren(mensajeTxtfull);
                                        dialog.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@androidx.annotation.NonNull Exception e) {

                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@androidx.annotation.NonNull UploadTask.TaskSnapshot snapshot) {
                        double p=(100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        dialog.setMessage((int) p + "%Subido...");
                    }
                });

            }else if(check.equals("imagen")){
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Archivo");
                String mensajeEnviadoRef="Mensajes/"+EnviarUserID+"/"+RecibirUserID;
                String mensajeRecibidoRef="Mensajes/"+RecibirUserID+"/"+EnviarUserID;
                DatabaseReference usuarioMensajeRef=RootRef.child("Mensajes").child(EnviarUserID).child(RecibirUserID).push();
                String mensajePushID=usuarioMensajeRef.getKey();
                final StorageReference filePath= storageReference.child(mensajePushID+"."+"jpg");
                uploadTask=filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return  filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener <Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUri=task.getResult();
                            myUrl=downloadUri.toString();
                            Map mensajeTxt= new HashMap();
                            mensajeTxt.put("mensaje",myUrl);
                            mensajeTxt.put("tipo",check);
                            mensajeTxt.put("de",EnviarUserID);
                            mensajeTxt.put("para",RecibirUserID);
                            mensajeTxt.put("mensajeID",mensajePushID);
                            mensajeTxt.put("fecha",CurrentDate);
                            mensajeTxt.put("hora",CurrentTime);
                            Map mensajeTxtfull= new HashMap();
                            mensajeTxtfull.put(mensajeEnviadoRef+"/"+mensajePushID,mensajeTxt);
                            mensajeTxtfull.put(mensajeRecibidoRef+"/"+mensajePushID,mensajeTxt);
                            RootRef.updateChildren(mensajeTxtfull).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){
                                        dialog.dismiss();
                                        Toast.makeText(ChatActivity.this,"Mensaje Enviado...",Toast.LENGTH_SHORT).show();
                                    }else{
                                        dialog.dismiss();
                                        Toast.makeText(ChatActivity.this,"Error al enviar",Toast.LENGTH_SHORT).show();
                                    }
                                    mensaje.setText("");
                                }
                            });
                        }
                    }
                });
            }else{
                dialog.dismiss();
                Toast.makeText(this,"ERROR No selecciono",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void EnviarMensaje() {
        String mensajeTexto=mensaje.getText().toString();
        if(TextUtils.isEmpty(mensajeTexto)){
            Toast.makeText(this,"Por favor escriba su mensaje", Toast.LENGTH_SHORT).show();
        }else{
            String mensajeEnviadoRef="Mensajes/"+EnviarUserID+"/"+RecibirUserID;
            String mensajeRecibidoRef="Mensajes/"+RecibirUserID+"/"+EnviarUserID;
            DatabaseReference usuarioMensajeRef=RootRef.child("Mensajes").child(EnviarUserID).child(RecibirUserID).push();
            String mensajePushID=usuarioMensajeRef.getKey();
            Map mensajeTxt= new HashMap();
            mensajeTxt.put("mensaje",mensajeTexto);
            mensajeTxt.put("tipo","texto");
            mensajeTxt.put("de",EnviarUserID);
            mensajeTxt.put("para",RecibirUserID);
            mensajeTxt.put("mensajeID",mensajePushID);
            mensajeTxt.put("fecha",CurrentDate);
            mensajeTxt.put("hora",CurrentTime);
            Map mensajeTxtfull= new HashMap();
            mensajeTxtfull.put(mensajeEnviadoRef+"/"+mensajePushID,mensajeTxt);
            mensajeTxtfull.put(mensajeRecibidoRef+"/"+mensajePushID,mensajeTxt);
            RootRef.updateChildren(mensajeTxtfull).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this,"Mensaje Enviado...",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this,"Error al enviar",Toast.LENGTH_SHORT).show();
                    }
                    mensaje.setText("");
                }
            });
        }
    }
}