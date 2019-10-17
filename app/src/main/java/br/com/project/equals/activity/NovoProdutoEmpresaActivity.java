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
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.helper.UsuarioFirebase;

import br.com.project.equals.model.Produto;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {

    /*
     * -----------ATENÇÃO--------------------------
     * É nessa Activity que devemos desenvolver o OCR para cadastro do produto
     * Desenvolvi apenas um modelo simples para podermos testar as outras
     * funcionalidades do sistema.
     *
     * A ideia é criamos um Button que aciona o OCR
     * Com a lib de um OCR (como Tesseract, exemplo)
     * Acionaremos a camera do cel, empresa tira DUAS fotos
     * Uma para salvar a imagem do produto
     * E outra para o OCR reconhecer a imagem da price tag
     * Atribuindo os respectivos valores para cada atributo
     * ----------------------------------------------------
     *
     * Foi criado no Database um no para empresa e outro para produto
     * para podermos recuprar os produtos apenas quando necesario,
     * senao a cada vez que recupero o no empresa, todos os produtos vem juntos
     * E principalmente para reduzir o tamanho dos dados retornados
     *
     * */

    private EditText editProdutoNome, editProdutoDescricao, editProdutoPreco;
    private ImageView imagemProduto;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = "";

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
                    produto.setPreco(Double.parseDouble(preco));
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

    

}
