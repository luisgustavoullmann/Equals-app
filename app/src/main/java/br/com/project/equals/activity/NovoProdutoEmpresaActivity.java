package br.com.project.equals.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import br.com.project.equals.R;
import br.com.project.equals.helper.UsuarioFirebase;
import br.com.project.equals.model.Empresa;
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
    //private ImageView imagemProduto;
    private String idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);

       /*Configuracoes inicias*/
        inicializarComponentes();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Produto");
        setSupportActionBar(toolbar);
        //Add o botao de voltar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
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
                    //produto.setImagemProduto();
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
        //imagemProduto = findViewById(R.id.imagemProduto);

    }

}
