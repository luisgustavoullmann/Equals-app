package br.com.project.equals.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


import br.com.project.equals.R;
import br.com.project.equals.model.ItemPedido;
import br.com.project.equals.model.Pedido;

/**
 * Created by Luis Gustavo Ullmann
 */

public class AdapterPedido extends RecyclerView.Adapter<AdapterPedido.MyViewHolder> {

    private List<Pedido> pedidos;

    public AdapterPedido(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pedidos, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {

        //Aqui entra a posição do usuário no map ao invés de endereço,
        // caso tenha escolhido retirar no ponto de venda
        // E não esqueça de incluir no MyViewHolder, mais abaixo

        //Colocar a foto do usuário, talvez

        Pedido pedido = pedidos.get(i);
        holder.nome.setText( pedido.getNome() );
        holder.endereco.setText( "Endereço: "+pedido.getEndereco() );
        holder.observacao.setText( "Obs: "+ pedido.getObservacao() );

        List<ItemPedido> itens = new ArrayList<>();
        itens = pedido.getItens();
        String descricaoItens = "";

        int numeroItem = 1;
        Double total = 0.0;
        for( ItemPedido itemPedido : itens ){

            int qtde = itemPedido.getQuantidade();
            Double preco = itemPedido.getPreco();
            total += (qtde * preco);//total recebe ++ a cada item pedido

            //Concatena cada resultado de itens
            String nome = itemPedido.getNomeProduto();
            descricaoItens += numeroItem + ") " + nome + " / (" + qtde + " x R$ " + preco + ") \n";
            numeroItem++;
        }
        //Pega todos os itens e add o total
        descricaoItens += "Total: R$ " + total;
        holder.itens.setText(descricaoItens);

        int metodoPagamento = pedido.getMetodoPagamento();
        //Caso tenha mais meios de pagamento, add switch para verificar
        String pagamento = metodoPagamento == 0 ? "Cartão" : "Dinheiro" ;
        holder.pgto.setText( "Pagamento: " + pagamento );

    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView endereco;
        TextView pgto;
        TextView observacao;
        TextView itens;

        public MyViewHolder(View itemView) {
            super(itemView);

            //ver adapter_pedidos
            nome        = itemView.findViewById(R.id.textPedidoNome);
            endereco    = itemView.findViewById(R.id.textPedidoEndereco);
            pgto        = itemView.findViewById(R.id.textPedidoPgto);
            observacao  = itemView.findViewById(R.id.textPedidoObs);
            itens       = itemView.findViewById(R.id.textPedidoItens);
        }
    }

}
