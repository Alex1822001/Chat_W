package com.example.chatw;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;
import static com.google.firebase.auth.PhoneAuthProvider.*;
public class LoginActivity extends AppCompatActivity {
    private EditText numero,codigo;
    private Button enviarT,enviarC;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mverificacion;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadignBar;
    private String phoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        numero=(EditText)findViewById(R.id.numero);
        codigo=(EditText)findViewById(R.id.codigo);
        enviarT=(Button)findViewById(R.id.enviarT);
        enviarC=(Button)findViewById(R.id.enviarC);
        mAuth = FirebaseAuth.getInstance();
        loadignBar=new ProgressDialog(this);
        enviarT.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                phoneNumber = numero.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginActivity.this,"Ingrese numero",Toast.LENGTH_SHORT).show();
                }else{
                    loadignBar.setTitle("Enviando codigo");
                    loadignBar.setMessage("Espera...");
                    loadignBar.show();
                    loadignBar.setCancelable(true);
                    PhoneAuthOptions options=PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phoneNumber).setTimeout(60L, TimeUnit.SECONDS).setActivity(LoginActivity.this).setCallbacks(callbacks).build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
        enviarC.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                numero.setVisibility(View.GONE);
                enviarT.setVisibility(View.GONE);
                String verificacionCode = codigo.getText().toString();
                if(TextUtils.isEmpty(verificacionCode)){
                    Toast.makeText(LoginActivity.this,"Ingresa codigo recibido",Toast.LENGTH_SHORT).show();
                }else{
                    loadignBar.setTitle("Entrando");
                    loadignBar.setMessage("...");
                    loadignBar.show();
                    loadignBar.setCancelable(true);
                    PhoneAuthCredential credential= PhoneAuthProvider.getCredential(mverificacion,verificacionCode);
                    signInPhoneAuhtCredential(credential);
                }
            }
        });
        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInPhoneAuhtCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadignBar.dismiss();
                Toast.makeText(LoginActivity.this,"Numero equivocado, prueba otra vez",Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.VISIBLE);
                enviarT.setVisibility(View.VISIBLE);
                codigo.setVisibility(View.GONE);
                enviarC.setVisibility(View.GONE);
            }
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token){
                mverificacion=verificationId;
                mResendingToken= token;
                loadignBar.dismiss();
                Toast.makeText(LoginActivity.this,"Codigo enviado mira en mensajes",Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.GONE);
                enviarT.setVisibility(View.GONE);
                codigo.setVisibility(View.VISIBLE);
                enviarC.setVisibility(View.VISIBLE);
            }
        };
    }//************ONCREATE***************
    private void signInPhoneAuhtCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    loadignBar.dismiss();
                    Toast.makeText(LoginActivity.this,"Todo correcto",Toast.LENGTH_SHORT).show();
                    EnviarInicio();
                }else{
                    String mensaje = task.getException().toString();
                    Toast.makeText(LoginActivity.this,"Error"+mensaje,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    protected void OnStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!= null){
            EnviarInicio();
        }
    }
    private void EnviarInicio() {
        Intent intent=new Intent(LoginActivity.this,InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}