package br.com.project.equals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import br.com.project.equals.activity.HomeActivity;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.helper.UsuarioFirebase;

public class AutenticacaoActivity extends AppCompatActivity {

    private Button botaoAcessar;
    private EditText campoEmail, campoSenha;
    private Switch tipoAcesso, tipoUsuario;
    private LinearLayout linearTipoUsuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacao);

        //esconde a action bar
        getSupportActionBar().hide();

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        //autenticacao.signOut();  Para deslogar o usuario

        //Verifica se o usuario esta logado
        verificaarUsuarioLogado();

        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    //Caso o switch esteja configurado, sabemos que é uma empresa
                    linearTipoUsuario.setVisibility(View.VISIBLE);
                } else {//Usuario switch
                    linearTipoUsuario.setVisibility(View.GONE);
                }
            }
        });

        //Usado para exiber que tipo de usuário irá aparecer no primeiro switch
        tipoAcesso.setOnCheckedChangeListener();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();

                if(!email.isEmpty()){
                    if(!senha.isEmpty()){

                        //Verificar o estado do switch
                        if(tipoAcesso.isChecked()){
                            //Cadastro
                            //Caso o cadastro seja validade com sucesso, temos uma intent para HomeActivity
                            //Caso contrario, é apresentado o catch de erro adequado
                            autenticacao.createUserWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Cadastro realizado com sucesso",
                                                Toast.LENGTH_SHORT).show();

                                        String tipoUsuario = getTipoUsuario();
                                        UsuarioFirebase.autalizarTipoUsuario(tipoUsuario);//recupera o tipo do usuario
                                        abrirTelaPrincipal(tipoUsuario);
                                    } else {
                                        String erroExcecao = "";
                                        try {
                                            throw task.getException();
                                        } catch (FirebaseAuthWeakPasswordException e){
                                            erroExcecao = "Digite uma senha mais forte";
                                        } catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Por favor, digite um e-mail válido";
                                        } catch (FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Esta conta já foi cadastrada!";
                                        } catch (Exception e){
                                            erroExcecao = "Ao cadastrar usuário " + e.getMessage();
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                        } else {
                            //Login
                            autenticacao.signInWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Login feito com sucesso",
                                                Toast.LENGTH_SHORT).show();
                                        abrirTelaPrincipal();
                                    } else{
                                        Toast.makeText(AutenticacaoActivity.this,
                                                "Erro ao fazer login: " + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }

                    } else {
                        Toast.makeText(AutenticacaoActivity.this,
                                "Preencha a senha",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AutenticacaoActivity.this,
                            "Preencha o E-mail",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Metodo que verifica se o usuario já esta logado
    private void verificaarUsuarioLogado(CompoundButton.OnCheckedChangeListener onCheckedChangeListener){
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if( usuarioAtual != null){
            abrirTelaPrincipal();
        }
    }

    //Metodo que retorna o tipo do usuario
    private String getTipoUsuario(){
        return tipoUsuario.isChecked() ? "empresa" : "usuario";
    }

    //Metodo que dá um start para HomeActivity apos confirmacao de login ou cadastro
    private void abrirTelaPrincipal(String tipoUsuario){
        if(tipoUsuario.equals("empresa")){
            //startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
        } else {//usuario
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        }
    }

    //Inicializa os componentes da AutenticacaoActiity
    private void inicializarComponentes(){
        campoEmail = findViewById(R.id.editCadasroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
        botaoAcessar = findViewById(R.id.buttonAcessar);
        tipoAcesso = findViewById(R.id.switchAcesso);
        tipoUsuario = findViewById(R.id.switchTipoUsuario);
        linearTipoUsuario = findViewById(R.id.linearTipoUsuario);
    }
}
