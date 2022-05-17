package com.example.chatw;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
public class SetupActivity extends AppCompatActivity {
    private EditText Nombre, Ciudad, Estado, Edad;
    private Button Guardar;
    private CircleImageView ImagenSetup;
    private FirebaseAuth auth;
    private DatabaseReference UserRef;
    private ProgressDialog dialog;
    private String CurrenUserID;
    private static int Gallery_PICK=1;
    private StorageReference UserProfileImagen;
    private Toolbar toolbar;
    private String token;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Nombre=(EditText)findViewById(R.id.nombreSetup);
        Ciudad=(EditText)findViewById(R.id.CidadSetup);
        Estado=(EditText)findViewById(R.id.estadoSetup);
        Edad=(EditText)findViewById(R.id.edadSetup);
        Guardar=(Button)findViewById(R.id.guardarSetup);
        ImagenSetup=(CircleImageView)findViewById(R.id.imagenSetup);
        toolbar=(Toolbar)findViewById(R.id.toolbarSetup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Complea el perfil");
        dialog=new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        CurrenUserID=auth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        UserProfileImagen= FirebaseStorage.getInstance().getReference().child("Imagen de perfil");
        Guardar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
             GuardarDB();
            }
        });
        ImagenSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);

            }
        });
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("imagen")){
                        String imagen = snapshot.child("imagen").getValue().toString();
                        Picasso.get().load(imagen).placeholder(R.drawable.logo).error(R.drawable.logo).into(ImagenSetup);
                    }else{
                        Toast.makeText(SetupActivity.this,"Se puede cargar una foto",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==Gallery_PICK && resultCode == RESULT_OK && data != null){
            Uri imageUri=data.getData();
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                dialog.setTitle("Imagen de perfil");
                dialog.setMessage("Guardando...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                final Uri resultUri=result.getUri();
                StorageReference filaPath = UserProfileImagen.child(CurrenUserID+".jpg");;;
                final File url=new File(resultUri.getPath());
                filaPath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this,"Guardado",Toast.LENGTH_SHORT).show();
                            UserProfileImagen.child(CurrenUserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    UserRef.child(CurrenUserID).child("imagen").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Picasso.get().load(downloadUri).error(R.drawable.error).into(ImagenSetup);
                                                Toast.makeText(SetupActivity.this,"Se guardo en la base de datos",Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }else {
                                                String error= task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this,"No se pudo guardar"+error,Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(this,"No se puede exportar la imagen",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }
    private void GuardarDB(){
        String nom = Nombre.getText().toString();
        String ciu = Ciudad.getText().toString();
        String est = Estado.getText().toString();
        String eda = Edad.getText().toString();
        if(TextUtils.isEmpty(nom)){
            Toast.makeText(this,"Debes ingresar el nombre",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(ciu)){
            Toast.makeText(this,"Debes ingresar la ciudad",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(est)){
            Toast.makeText(this,"Debes ingresar un estado",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(eda)){
            Toast.makeText(this,"Debes ingresar tu edad",Toast.LENGTH_SHORT).show();
        }else{
            dialog.setTitle("Guardando datos");
            dialog.setTitle("Por favor espere a que se termine el proceso");
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if(task.isSuccessful()){
                        token=task.getResult();
                        HashMap map = new HashMap();
                        map.put("uid",CurrenUserID);
                        map.put("nombre",nom);
                        map.put("ciudad", ciu);
                        map.put("estado",est);
                        map.put("Edad",eda);
                        map.put("token",token);
                        UserRef.child(CurrenUserID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(SetupActivity.this,"Datos guardados",Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    EnviarAlInicio();
                                }else{
                                    String err= task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this,"Error"+err,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    private void EnviarAlInicio() {
        Intent intent = new Intent(SetupActivity.this,InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}