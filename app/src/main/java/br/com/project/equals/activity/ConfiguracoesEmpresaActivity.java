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
import br.com.project.equals.api.EmpresaService;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.helper.UsuarioFirebase;
import br.com.project.equals.model.Empresa;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfiguracoesEmpresaActivity extends AppCompatActivity {

    private EditText editEmpresaNome, editEmpresaEndereco, editEmpresaCategoria, editEmpresaTempo, editEmpresaTaxa;
    private ImageView imagePerfilEmpresa;

    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = "";
    private Retrofit retrofit;
    private String urlWebService = ""; //base url precisa terminar com /

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_empresa);

        //Config iniciais
        inicializarComponentes();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage(); //Acesso a referencia do Storage
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);
        //Add o botao de voltar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        //Evento de clique na imagem para add foto do perfil da empresa
        imagePerfilEmpresa.setOnClickListener(new View.OnClickListener() {
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

        //Retrofit Config - passar a url
        retrofit = new Retrofit.Builder()
                .baseUrl(urlWebService)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //Retrofit
        recuperarEmpresa();

        /*Recuperar dados da emresa*/
        recuperarDadosEmpresa();
    }


    //Falta inserir no metodo o load da imagem para travar a tela enquanto executado
    private void recuperarDadosEmpresa() {
        DatabaseReference empresaRef = firebaseRef
                .child("empresas")
                .child(idUsuarioLogado);
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Empresa empresa = dataSnapshot.getValue(Empresa.class);
                    editEmpresaNome.setText(empresa.getNome());
                    editEmpresaEndereco.setText(empresa.getEndereco());
                    editEmpresaCategoria.setText(empresa.getCategoria());
                    editEmpresaTaxa.setText(empresa.getPrecoEntrega().toString());
                    editEmpresaTempo.setText(empresa.getTempo());

                    urlImagemSelecionada = empresa.getUrlImagem();
                    if (urlImagemSelecionada != "") {
                        Picasso.get()
                                .load(urlImagemSelecionada)
                                .into(imagePerfilEmpresa);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Metodo que salva os dados da empresa
    public void validarDadosEmpresa(View view) {

        //Valida se os campos foram preenchidos
        String nome = editEmpresaNome.getText().toString();
        String endereco = editEmpresaEndereco.getText().toString();
        String taxa = editEmpresaTaxa.getText().toString();
        String categoria = editEmpresaCategoria.getText().toString();
        String tempo = editEmpresaTempo.getText().toString();

        if (!nome.isEmpty()) {
            if(!endereco.isEmpty()) {
                if (!taxa.isEmpty()) {
                    if (!categoria.isEmpty()) {
                        if (!tempo.isEmpty()) {

                            Empresa empresa = new Empresa();
                            empresa.setIdUsuario(idUsuarioLogado);
                            empresa.setNome(nome);
                            empresa.setEndereco(endereco);
                            empresa.setPrecoEntrega(Double.parseDouble(taxa));
                            empresa.setCategoria(categoria);
                            empresa.setTempo(tempo);
                            empresa.setUrlImagem(urlImagemSelecionada);
                            empresa.salvar();

                            exibirMensagem("Empresa salvo com sucesso");
                            finish(); //nao esqueca o finish ;)

                        } else {
                            exibirMensagem("Digite um tempo de entrega");
                        }
                    } else {
                        exibirMensagem("Digite uma categoria");
                    }
                } else {
                    exibirMensagem("Digite uma taxa de entrega");
                }
            } else {
                exibirMensagem("Digite o endereço da empresa");
            }
        } else {
            exibirMensagem("Digite um nome para a empresa");
        }

    }

    //Exibe o Toast de acordo com a mensagem
    private void exibirMensagem(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    //Ver como recupera imagem diretamente da camera do celular
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
                    imagePerfilEmpresa.setImageBitmap(imagem);

                    //Upload da imagem em JPEG com uma qualidade de 70px
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("empresa")
                            .child(idUsuarioLogado + "jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesEmpresaActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            urlImagemSelecionada = taskSnapshot.getDownloadUrl().toString();
                            Toast.makeText(ConfiguracoesEmpresaActivity.this,
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

    private void inicializarComponentes(){
        editEmpresaNome = findViewById(R.id.editEmpresaNome);
        editEmpresaEndereco = findViewById(R.id.editEmpresaEndereco);
        editEmpresaCategoria = findViewById(R.id.editEmpresaCategoria);
        editEmpresaTaxa = findViewById(R.id.editEmpresaTaxa);
        editEmpresaTempo = findViewById(R.id.editEmpresaTempo);
        imagePerfilEmpresa = findViewById(R.id.imagePerfilEmpresa);
    }


    private void recuperarEmpresa(){
        EmpresaService empresaService = retrofit.create(EmpresaService.class);
        Call<Empresa> call = empresaService.recuperarLoja();

        //call faz a tarefa assincrona dentro de uma thread para trazer as infos
        call.enqueue(new Callback<Empresa>() {
            @Override
            public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                //Nossa resposta
                if(response.isSuccessful()){
                    //Converte o body do JSON
                    Empresa empresa = response.body();
                    editEmpresaNome.setText(empresa.getNome());
                    editEmpresaEndereco.setText(empresa.getEndereco());
                }
            }

            @Override
            public void onFailure(Call<Empresa> call, Throwable t) {
                //Ao falhar
            }
        });

    }

}
