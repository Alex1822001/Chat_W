package com.example.chatw;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.util.HashMap;
import de.hdodenhof.circleimageview.CircleImageView;
public class MiPerfilActivity extends AppCompatActivity {
    private EditText nombre, ciudad, edad, estado;
    private Button actualizar;
    private CircleImageView imagenPerfil;
    private Toolbar toolbar;
    private String CurrentuserID;
    private FirebaseAuth auth;
    private DatabaseReference RootRef;
    private static int Gallery_PICK=1;
    private ProgressDialog dialog;
    private StorageReference UserProfileImagen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_perfil);
        Componentes();
        dialog=new ProgressDialog(this);
        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActualizarPerfil();
            }
        });
        UserProfileImagen= FirebaseStorage.getInstance().getReference().child("Imagen de perfil");
        RootRef= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        CurrentuserID=auth.getCurrentUser().getUid();
        RootRef.child("Usuarios").child(CurrentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&& snapshot.hasChild("imagen")){
                    String nom1 = snapshot.child("nombre").getValue().toString();
                    String ciu1 = snapshot.child("ciudad").getValue().toString();
                    String est1 = snapshot.child("estado").getValue().toString();
                    String eda1 = snapshot.child("Edad").getValue().toString();
                    String imagen1 = snapshot.child("imagen").getValue().toString();
                    nombre.setText(nom1);
                    ciudad.setText(ciu1);
                    estado.setText(est1);
                    edad.setText(eda1);
                    Picasso.get().load(imagen1).placeholder(R.drawable.logo).error(R.drawable.error).into(imagenPerfil);
                }else if(snapshot.exists()){
                    String nom = snapshot.child("nombre").getValue().toString();
                    String ciu = snapshot.child("ciudad").getValue().toString();
                    String est = snapshot.child("estado").getValue().toString();
                    String eda = snapshot.child("edad").getValue().toString();
                    nombre.setText(nom);
                    ciudad.setText(ciu);
                    estado.setText(est);
                    edad.setText(eda);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Gallery_PICK);
            }
        });
    }
    private void Componentes() {
        nombre = (EditText) findViewById(R.id.nombrePerfil);
        ciudad = (EditText) findViewById(R.id.ciudadPerfil);
        edad = (EditText) findViewById(R.id.edadPerfil);
        estado = (EditText) findViewById(R.id.estadoPerfil);
        actualizar = (Button) findViewById(R.id.actualizar);
        imagenPerfil = (CircleImageView) findViewById(R.id.imagenP);
        toolbar = findViewById(R.id.toolbarMiPerfil);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mi Perfil");
    }
    private void ActualizarPerfil() {
        String nom = nombre.getText().toString();
        String ciu = ciudad.getText().toString();
        String est = estado.getText().toString();
        String eda = edad.getText().toString();
        if (TextUtils.isEmpty(nom)) {
            Toast.makeText(this, "Debes ingresar el nombre", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(ciu)) {
            Toast.makeText(this, "Debes ingresar la ciudad", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(est)) {
            Toast.makeText(this, "Debes ingresar un estado", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(eda)) {
            Toast.makeText(this, "Debes ingresar tu edad", Toast.LENGTH_SHORT).show();
        } else {
            HashMap profile=new HashMap();
            profile.put("uid",CurrentuserID);
            profile.put("nombre",nom);
            profile.put("ciudad",ciu);
            profile.put("estado",est);
            profile.put("Edad",eda);
            RootRef.child("Usuarios").child(CurrentuserID).updateChildren(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        EnviaralInicio();
                        Toast.makeText(MiPerfilActivity.this, "Guardado exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        String err = task.getException().getMessage().toString();
                        Toast.makeText(MiPerfilActivity.this, "Error" + err, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==Gallery_PICK && resultCode==RESULT_OK && data!=null){
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
                StorageReference filaPath = UserProfileImagen.child(CurrentuserID+".jpg");;;
                final File url=new File(resultUri.getPath());
                filaPath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MiPerfilActivity.this,"Guardado",Toast.LENGTH_SHORT).show();
                            UserProfileImagen.child(CurrentuserID+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    RootRef.child("Usuarios").child(CurrentuserID).child("imagen").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Picasso.get().load(downloadUri).error(R.drawable.logo).into(imagenPerfil);
                                                Toast.makeText(MiPerfilActivity.this,"Se guardo en la base de datos",Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }else {
                                                String error= task.getException().getMessage();
                                                Toast.makeText(MiPerfilActivity.this,"No se pudo guardar"+error,Toast.LENGTH_SHORT).show();
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
            }
        }
    }
    private void EnviaralInicio() {
        Intent intent = new Intent(MiPerfilActivity.this,InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}