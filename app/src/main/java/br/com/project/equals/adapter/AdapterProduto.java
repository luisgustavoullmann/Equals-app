package br.com.project.equals.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.project.equals.R;
import br.com.project.equals.api.ProdutoService;
import br.com.project.equals.model.Produto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Luis Gustavo Ullmann
 */

public class AdapterProduto extends RecyclerView.Adapter<AdapterProduto.MyViewHolder> {

    private List<Produto> produtos;
    private Context context;

    private TextView nome;
    private TextView descricao;
    private TextView preco;

    private Retrofit retrofit;
    private String urlWebService = ""; //base url precisa terminar com /

    public AdapterProduto(List<Produto> produtos, Context context) {
        this.produtos = produtos;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_produto, parent, false);

        //Retrofit Config - passar a url
        retrofit = new Retrofit.Builder()
                .baseUrl(urlWebService)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //Retrofit
        recuperarProduto("");


        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Produto produto = produtos.get(i);
        holder.nome.setText(produto.getNome());
        holder.descricao.setText(produto.getDescricao());
        holder.preco.setText("R$ " + produto.getPreco());
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView descricao;
        TextView preco;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textNomeProduto);
            descricao = itemView.findViewById(R.id.textDescricaoProduto);
            preco = itemView.findViewById(R.id.textPreco);
        }
    }

    //call faz a tarefa assincrona dentro de uma thread para trazer as infos
    private void recuperarProduto(String id){
        ProdutoService produtoService = retrofit.create(ProdutoService.class);
        Call<Produto> call = produtoService.recuperarProduto(id);

        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if(response.isSuccessful()){
                    //Converte o body do JSON
                    Produto produto = response.body();
                    if(produto.equals("")) {
                        return ;
                    } else {
                        try {
                            MyViewHolder.class.getName(nome.setText(produto.getNome()));
                            MyViewHolder.class.getName(descricao.setText(produto.getDescricao()));
                            MyViewHolder.class.getName(preco.setText(produto.getPreco()));
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });
    }
}
