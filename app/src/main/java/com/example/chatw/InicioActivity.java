package com.example.chatw;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static java.lang.Thread.sleep;
public class InicioActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager MViewPager;
    private TabLayout MtabLayout;
    private AcesoTabsAdapter MacesoTabsAdapter;
    private String CurrentUserId;
    private FirebaseAuth MAuth;
    private DatabaseReference UserRef, RootRef;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        toolbar= (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatW");
        MViewPager=(ViewPager)findViewById(R.id.TabPagerMain);
        MacesoTabsAdapter = new AcesoTabsAdapter(getSupportFragmentManager());
        MViewPager.setAdapter(MacesoTabsAdapter);
        MtabLayout=(TabLayout)findViewById(R.id.tablaMain);
        MtabLayout.setupWithViewPager(MViewPager);
        UserRef= FirebaseDatabase.getInstance().getReference().child("Usuarios");
        RootRef= FirebaseDatabase.getInstance().getReference().child("Grupos");
        MAuth = FirebaseAuth.getInstance();
        CurrentUserId=MAuth.getCurrentUser().getUid();
    }
    protected void onStart(){
            super.onStart();
            FirebaseUser curUser= MAuth.getCurrentUser();
            if(curUser==null){
                EnviarAlogin();
            }else{
                VerificarUsuario();
                ActualizarActividad("activo");
            }
    }
    @Override
    protected void onStop() {
            super.onStop();
            FirebaseUser curUser= MAuth.getCurrentUser();
            if(curUser !=null){
                ActualizarActividad("inactivo");
            }
    }
    @Override
    protected void onDestroy() {
            super.onDestroy();
            FirebaseUser curUser= MAuth.getCurrentUser();
            if(curUser !=null){
                ActualizarActividad("inactivo");
            }
    }
    private void VerificarUsuario() {
        final String currentUserID = MAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(currentUserID)){
                    CompletarDatosUsuarios();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }});
    }
    private void CompletarDatosUsuarios(){
        Intent intent = new Intent(InicioActivity.this,SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void EnviarAlogin(){
        Intent intent = new Intent(InicioActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menusopciones,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.Buscar_contacto){
            BuscarContactos();
        }
        if(item.getItemId()==R.id.NuevoGrupo){
            NuevoGrupo();
        }
        if(item.getItemId()==R.id.MPerfilMenu){
            Intent intent = new Intent(InicioActivity.this,MiPerfilActivity.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.CerrarMenu){
            MAuth.signOut();
            EnviarAlogin();
        }
        return true;
    }
    private void BuscarContactos() {
        Intent intent = new Intent(InicioActivity.this,BuscarAmigoActivity.class);
        startActivity(intent);
    }
    private void NuevoGrupo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InicioActivity.this,R.style.AlertDialog);
        builder.setTitle("Nombre de grupo:");
        final EditText nombregrupo = new EditText(InicioActivity.this);
        nombregrupo.setHint("ejem. Roll or Die");
        builder.setView(nombregrupo);
        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreG = nombregrupo.getText().toString();
                if(TextUtils.isEmpty(nombreG)){
                    Toast.makeText(InicioActivity.this,"Ingrese el nombre del grupo",Toast.LENGTH_SHORT).show();
                }else {
                    CrearGrupoFirebase(nombreG);
                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void CrearGrupoFirebase(String nombreG) {
        RootRef.child(nombreG).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(InicioActivity.this,"Grupo creando con existo...",Toast.LENGTH_SHORT).show();
                }else{
                    String error= task.getException().getMessage().toString();
                    Toast.makeText(InicioActivity.this,"Error"+error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    protected void ActualizarActividad(String estado){
        String CurrentTime, CurrentDate;
        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd,yyyy");
        CurrentDate=dateFormat.format(calendar.getTime());
        SimpleDateFormat dateFormat1= new SimpleDateFormat("hh:mm a");
        CurrentTime=dateFormat1.format(calendar.getTime());
        HashMap<String,Object>EstadoOnline=new HashMap<>();
        EstadoOnline.put("hora",CurrentTime);
        EstadoOnline.put("fecha",CurrentDate);
        EstadoOnline.put("estadoAct",estado);
        UserRef.child(CurrentUserId).child("estadoUser").updateChildren(EstadoOnline);
    }
}