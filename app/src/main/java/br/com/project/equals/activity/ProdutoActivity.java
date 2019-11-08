package br.com.project.equals.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.com.project.equals.R;
import br.com.project.equals.adapter.AdapterProduto;
import br.com.project.equals.api.ProdutoService;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.helper.UsuarioFirebase;
import br.com.project.equals.listener.RecyclerItemClickListener;
import br.com.project.equals.model.Empresa;
import br.com.project.equals.model.ItemPedido;
import br.com.project.equals.model.Pedido;
import br.com.project.equals.model.Produto;
import br.com.project.equals.model.Usuario;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProdutoActivity extends AppCompatActivity {

    private RecyclerView recyclerListaProdutos;
    private ImageView imagemEmpresaProduto;
    private TextView textNomeEmpresaProduto;
    private Empresa empresaSelecionada;
    private AlertDialog dialog;
    private TextView textCarrinhoQtd, textCarrinhoTotal;

    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private List<ItemPedido> itemPedidos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Pedido pedidoRecuperado;
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private int metodoPagamento;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        //Configurações iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Recupera a empresa selecionada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa"); //mesma key da HomeActivity

            //Configurando as info da empresa para cabeçalho ao clicar na empresa selecionada
            textNomeEmpresaProduto.setText(empresaSelecionada.getNome());
            idEmpresa = empresaSelecionada.getIdUsuario(); //tambem serve para empresa, todos são usuários

            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imagemEmpresaProduto);
        }

        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Produtos");
        setSupportActionBar(toolbar);
        //Add o botao de voltar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        //Config RecyclerView
        recyclerListaProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerListaProdutos.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerListaProdutos.setAdapter(adapterProduto);

        //Configurar evento de clique
        recyclerListaProdutos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerListaProdutos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                confirmarQuantidade(position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );


    }

    private void confirmarQuantidade(final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantidade");
        builder.setMessage("Digite a quantidade");

        final EditText editQuantidade = new EditText(this);
        editQuantidade.setText("1");

        builder.setView(editQuantidade);

        builder.setPositiveButton(
                "Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String quantidade = editQuantidade.getText().toString();

                        Produto produtoSelecionado = produtos.get(position);
                        ItemPedido itemPedido = new ItemPedido();
                        itemPedido.setIdProduto(produtoSelecionado.getIdProduto());
                        itemPedido.setNomeProduto(produtoSelecionado.getNome());
                        itemPedido.setDescricaoProduto(produtoSelecionado.getDescricao());
                        itemPedido.setPreco((double) produtoSelecionado.getPreco());
                        itemPedido.setQuantidade(Integer.parseInt(quantidade)); //qtd não pode ser zero

                        //Add pedidos no carrinho
                        //itemPedidos.add(itemPedido); //for para validar se o item já foi adicionado
                        for(ItemPedido itensPedido : itemPedidos){
                            itemPedidos.add(itemPedido); //for para validar se o item já foi adicionado
                            quantidade += quantidade; //corrigir se estiver errado
                        }

                        //Recuperando se o pedido já existe
                        if(pedidoRecuperado == null){
                            pedidoRecuperado = new Pedido(idUsuarioLogado, idEmpresa);
                        }

                        //Config do pedido
                        pedidoRecuperado.setNome(usuario.getNome());
                        pedidoRecuperado.setEndereco(usuario.getEndereco());

                        //Config itens do pedido
                        pedidoRecuperado.setItens(itemPedidos); //retorna o List<Pedido>

                        //Salvando o pedido
                        pedidoRecuperado.salvar();

                    }
                });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Spot de load enquanto carrega os dados do usuario
    private void recuperarDadosUsuario(){
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null ){
                    usuario = dataSnapshot.getValue(Usuario.class);
                }

                recuperarPedido();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //Recupera o pedido de acordo com o usuario logado e o recuperarDadosUsuario
    private void recuperarPedido() {
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuarioLogado);

        //A cada item  que foi add, iremos recupera-lo
        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Zerando valores do carrinho de compras
                qtdItensCarrinho = 0;
                totalCarrinho = 0.0;
                itemPedidos = new ArrayList<>();

                if (dataSnapshot.getValue() != null) {
                    pedidoRecuperado = dataSnapshot.getValue(Pedido.class);

                    //Montagem de itens, retorna a listagem de itens pedidos
                    itemPedidos = pedidoRecuperado.getItens();
                    for(ItemPedido itemPedido : itemPedidos){
                        int quantidade = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();

                        totalCarrinho += (quantidade * preco);
                        qtdItensCarrinho += quantidade;
                    }
                }

                DecimalFormat df = new DecimalFormat("0.00"); //apagar com mascara de moeda

                //Exibindo os dados-quantidades
                textCarrinhoQtd.setText("qtd: " + String.valueOf(qtdItensCarrinho));
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));// aplica mask de moeda


                dialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarProdutos(){
        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idEmpresa);
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    produtos.add(ds.getValue(Produto.class));
                }
                adapterProduto.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProdutoActivity.this,
                        "O procedimento falhou!",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_produto, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuPedido :
                //Confirmando Pedido
                confirmarPedido(); //Usuário escolhe o meio de pagamento e coloca uma observação, possível de colocar um chat
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Usuário escolhe o meio de pagamento e coloca uma observação, possível de colocar um chat
    //Caso tenha escolhido meio de entrega, a empresa confirma a saída
    //Pode implementar um meio de pagamento, pedindo dados do cartão
    private void confirmarPedido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione o meio de pagamento que você prefere: ");

        //Opções de pagamento
        //Poderemos colocar que se escolher cartão, um input ou confirmação do mesmo
        CharSequence[] itens = new  CharSequence[]{
            "Cartão", "Dinheiro"
        };
        //Por padrão, cartão é a opção checked
        builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
            // i = indica qual forma de pagamento o usuário selecionou
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                metodoPagamento = i;
            }
        });

        final EditText editObservacao = new EditText(this);
        editObservacao.setHint("Digite uma observação");
        builder.setView(editObservacao);

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String observacao = editObservacao.getText().toString();
                //Implementar - Aqui podemos chamar o dialog caso o usuario tenha
                // escolhido cartão, pedir input ou confirmar os dados
                pedidoRecuperado.setMetodoPagamento(metodoPagamento);
                pedidoRecuperado.setObservacao(observacao);
                //Alteramos o status do pedido de pendente para confirmado
                pedidoRecuperado.setStatus("confirmado");

                //A cada pedido confirmado,
                //poderemos traçar no mapa qual o melhor caminho para o usuario,
                //usando  algoritmos inteligentes de busca,
                // para termos o melhor caminho baseado nos endereços
                // que o usuario terá que percorrer
                pedidoRecuperado.confirmar();

                //removendo o pedido, temporario,
                //pois podemos criar um novo nó, como está no comentário do metdo
                pedidoRecuperado.remover();
                pedidoRecuperado = null;
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void inicializarComponentes(){
        recyclerListaProdutos = findViewById(R.id.recyclerListaProdutos);
        imagemEmpresaProduto = findViewById(R.id.imageEmpresaProduto);
        textNomeEmpresaProduto = findViewById(R.id.textNomeEmpresaProduto);

        textCarrinhoQtd = findViewById(R.id.textCarrinhoQtd);
        textCarrinhoTotal = findViewById(R.id.textCarrinhoTotal);
    }

}
