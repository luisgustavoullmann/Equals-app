package br.com.project.equals.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.project.equals.R;
import br.com.project.equals.adapter.AdapterProduto;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.model.Empresa;
import br.com.project.equals.model.Produto;

public class ProdutoActivity extends AppCompatActivity {

    private RecyclerView recyclerListaProdutos;
    private ImageView imagemEmpresaProduto;
    private TextView textNomeEmpresaProduto;
    private Empresa empresaSelecionada;

    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresa;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto);

        //Configurações iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();

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

        //Recupera produtos da empresa para o usuario
        recuperarProdutos();

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

            }
        });
    }

    private void inicializarComponentes(){
        recyclerListaProdutos = findViewById(R.id.recyclerListaProdutos);
        imagemEmpresaProduto = findViewById(R.id.imageEmpresaProduto);
        textNomeEmpresaProduto = findViewById(R.id.textNomeEmpresaProduto);
    }
}
