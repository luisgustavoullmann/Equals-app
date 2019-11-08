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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import br.com.project.equals.R;
import br.com.project.equals.api.ProdutoService;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.helper.UsuarioFirebase;

import br.com.project.equals.model.Produto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {


    private EditText editProdutoNome, editProdutoDescricao, editProdutoPreco;
    private ImageView imagemProduto;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = "";
    private Retrofit retrofit;
    private String urlWebService = ""; //base url precisa terminar com /
    public ProdutoService produtoService;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);

       /*Configuracoes inicias*/
        inicializarComponentes();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage(); //Acesso a referencia do Storage
        firebaseRef = ConfiguracaoFirebase.getFirebase();


        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Produto");
        setSupportActionBar(toolbar);
        //Add o botao de voltar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        //Evento de clique na imagem para add foto do produto
        imagemProduto.setOnClickListener(new View.OnClickListener() {
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
        //Retrofit;
        adicionarProduto();
        editarProduto();
        deletarProduto();

        produtoService = retrofit.create(ProdutoService.class);

    }

    //Metodo que salva os dados da empresa
    public void validarDadosProduto(View view) {

        //Valida se os campos foram preenchidos
        String nome = editProdutoNome.getText().toString();
        String descricao = editProdutoDescricao.getText().toString();
        String preco = editProdutoPreco.getText().toString();

        if (!nome.isEmpty()) {
            if (!descricao.isEmpty()) {
                if (!preco.isEmpty()) {

                    Produto produto = new Produto();
                    produto.setIdUsuario(idUsuarioLogado);
                    produto.setNome(nome);
                    produto.setDescricao(descricao);
                    produto.setPreco(Integer.parseInt(preco));
                    produto.setImagemProduto(urlImagemSelecionada);
                    produto.salvar();
                    finish(); //nao esqueca o finish ;)
                    exibirMensagem("Produto salvo com sucesso");

                } else {
                    exibirMensagem("Digite um preço para o produto");
                }
            } else {
                exibirMensagem("Digite uma descrição para o produto");
            }

        } else {
            exibirMensagem("Digite um nome para o produto");
        }

    }

    //Exibe o Toast de acordo com a mensagem
    private void exibirMensagem(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void inicializarComponentes() {
        editProdutoNome = findViewById(R.id.editProdutoNome);
        editProdutoDescricao = findViewById(R.id.editProdutoDescricao);
        editProdutoPreco = findViewById(R.id.editProdutoPreco);
        //editar imagem do produto também
        imagemProduto = findViewById(R.id.imageProduto);

    }

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
                    imagemProduto.setImageBitmap(imagem);

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
                            Toast.makeText(NovoProdutoEmpresaActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            urlImagemSelecionada = taskSnapshot.getDownloadUrl().toString();
                            Toast.makeText(NovoProdutoEmpresaActivity.this,
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

    private void adicionarProduto(){

        //Config o obj
       // final Produto produto = new Produto();
       // produto.setNome("");
      //  produto.setDescricao("");
      //  produto.setPreco(0);

        //Recupera o serviço e salva o produto
        //ProdutoService produtoService = retrofit.create(ProdutoService.class);
        Call<Produto> call = produtoService.adicionarProduto("","",0 , 0);

        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if(response.isSuccessful()){
                    Produto produtoResposta = response.body();
                    String resposta = editProdutoNome + " " + editProdutoDescricao;
                    Toast.makeText(
                            NovoProdutoEmpresaActivity.this,
                            response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });
    }

    private void editarProduto(){
        Produto produto = new Produto();
        Call<Produto> call = (Call<Produto>) produtoService.editarProduto(0, produto);

        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if(response.isSuccessful()){
                    Produto produtoResposta = response.body();
                    String resposta = editProdutoNome + " " + editProdutoDescricao;
                    Toast.makeText(
                            NovoProdutoEmpresaActivity.this,
                            response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                Toast.makeText(NovoProdutoEmpresaActivity.this,
                        "O procedimento falhou!",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void deletarProduto(){
        Call<Void> call = produtoService.deletarProduto(0);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    String resposta = editProdutoNome + " " + editProdutoDescricao;
                    Toast.makeText(
                            NovoProdutoEmpresaActivity.this,
                            response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NovoProdutoEmpresaActivity.this,
                        "O procedimento falhou!",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

    }


}
