package br.com.project.equals.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import br.com.project.equals.R;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.helper.UsuarioFirebase;
import br.com.project.equals.model.Empresa;
import br.com.project.equals.model.Usuario;

public class ConfiguracoesUsuarioActivity extends AppCompatActivity {

    private EditText editNomeUsuario, editEnderecoUsuario, editTelefoneUsuario, editCpfUsuario;
    private ImageView imagePerfilUsuario;
   // private void idUsuario;
    private DatabaseReference firebaseRef;

    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = "";



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_usuario);

        //Config iniciais
        inicializarComponentes();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage(); //Acesso a referencia do Storage
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurações usuário");
        setSupportActionBar(toolbar);
        //Add o botao de voltar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        //Evento de clique na imagem para add foto do perfil da empresa
        imagePerfilUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });

        //Recupera dados do Usuario
        recuperarDadosUsuario();
    }


    public void validarDadosUsuario(View view){

        //Valida se os campos foram preenchidos
        String nome = editNomeUsuario.getText().toString();
        String endereco = editEnderecoUsuario.getText().toString();
        String telefone = editTelefoneUsuario.getText().toString();
        String cpf = editCpfUsuario.getText().toString();

        if (!nome.isEmpty()) {
            if (!endereco.isEmpty()) {
                if (!telefone.isEmpty()) {
                    if (!cpf.isEmpty()) {

                        Usuario usuario = new Usuario();
                        usuario.setIdUsuario(idUsuarioLogado);
                        usuario.setNome(nome);
                        usuario.setEndereco(endereco);
                        usuario.setTelefone(telefone);
                        usuario.setCpf(cpf);
                        usuario.setUrlImagem(urlImagemSelecionada);
                        usuario.salvar();

                        exibirMensagem("Dados salvos com sucesso");
                        finish(); //nao esqueca o finish ;)

                    } else {
                        exibirMensagem("Digite CPF válido");
                    }
                } else {
                    exibirMensagem("Digite um tefefone válido");
                }
            } else {
                exibirMensagem("Digite um endereço");
            }

        } else {
            exibirMensagem("Digite o seu nome");
        }

    }

    //Exibe o Toast de acordo com a mensagem
    private void exibirMensagem(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }


    //Falta inserir no metodo o load da imagem para travar a tela enquanto executado
    private void recuperarDadosUsuario() {
        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    editNomeUsuario.setText(usuario.getNome());
                    editEnderecoUsuario.setText(usuario.getEndereco());
                    editTelefoneUsuario.setText(usuario.getTelefone().toString());
                    editCpfUsuario.setText(usuario.getCpf().toString());

                    urlImagemSelecionada = usuario.getUrlImagem();
                    if (urlImagemSelecionada != "") {
                        Picasso.get()
                                .load(urlImagemSelecionada)
                                .into(imagePerfilUsuario);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //Add metodo de pegar a foto do usuario, assim como fiz para empresa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_OK) {
            Bitmap imagem = null;
            try {
                switch (requestCode) {
                    case SELECAO_GALERIA:
                        Uri localImagem = data.getData();
                        imagem = MediaStore.Images
                                .Media
                                .getBitmap(
                                        getContentResolver(),
                                        localImagem
                                );
                        break;
                }

                if (imagem != null) {
                    imagePerfilUsuario.setImageBitmap(imagem);

                    //Upload da imagem em JPEG com uma qualidade de 70px
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("usuarios")
                            .child(idUsuarioLogado + "jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesUsuarioActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            urlImagemSelecionada = taskSnapshot.getDownloadUrl().toString();
                            Toast.makeText(ConfiguracoesUsuarioActivity.this,
                                    "Sucesso ao fazer o upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void inicializarComponentes() {
        editNomeUsuario = findViewById(R.id.editNomeUsuario);
        editEnderecoUsuario = findViewById(R.id.editEnderecoUsuario);
        editTelefoneUsuario = findViewById(R.id.editTelefoneUsuario);
        editCpfUsuario = findViewById(R.id.editCpfUsuario);
        imagePerfilUsuario = findViewById(R.id.imagePerfilUsuario);
    }


}
