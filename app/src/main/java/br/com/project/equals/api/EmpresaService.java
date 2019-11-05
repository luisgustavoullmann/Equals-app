package br.com.project.equals.api;

import br.com.project.equals.model.Empresa;
import retrofit2.Call;
import retrofit2.http.GET;

public interface EmpresaService {

    /*
     * -----> Nota explicativa
     * - A interface API, simplesmente é para a entrega e por exigência da FIAP.
     * Pois, para a API fazer sentido, seria somente quando houvesse uma integração
     * com algum e-commerce (ex.: Magazine Luiza, Mercado Livre, Americanas.com e etc)
     * Sendo assim, os endpoints da API dos e-commerces/marketplaces, estaria sendo
     * integradas a nossa aplicação.
     * */

    @GET("/api/add-loja")
    Call<Empresa> recuperarLoja();
}
