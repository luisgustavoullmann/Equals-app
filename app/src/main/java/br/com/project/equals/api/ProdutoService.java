package br.com.project.equals.api;

import java.util.List;

import br.com.project.equals.model.Produto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ProdutoService {

    /*
     * -----> Nota explicativa
     * - A interface API, simplesmente é para a entrega e por exigência da FIAP.
     * Pois, para a API fazer sentido, seria somente quando houvesse uma integração
     * com algum e-commerce (ex.: Magazine Luiza, Mercado Livre, Americanas.com e etc)
     * Sendo assim, os endpoints da API dos e-commerces/marketplaces, estaria sendo
     * integradas a nossa aplicação.
     * */

    @POST("api/add-produto") //Adiciona um produto na loja.
    Call<Produto> adicionarProduto(@Body Produto produto);

    @FormUrlEncoded
    @POST("api/add-produto")
    Call<Produto> adicionarProduto(
            @Field("nome") String nome,
            @Field("descricao") String descricao,
            @Field("quantidade") int quantidade,
            @Field("preco") int preco
    );

    @PUT("api/edit-produto/{id}") //Possibilita a ediçao de um produto
    Call<Produto> editarProduto(@Path("id") int id, Produto produto);

    @GET("api//produtos") //Lista todos os produtos do comerciante em questão
    Call<List<Produto>> recuperarListaProduto();

    @GET("api/produto/{id}") //retorna informações de um determinado produto
    Call<Produto> recuperarProduto(@Path("id") String id, @Body Produto produto);

    @DELETE("api/delete-produto/{id}")
    Call<Void> deletarProduto(@Path("id") int id);
}
