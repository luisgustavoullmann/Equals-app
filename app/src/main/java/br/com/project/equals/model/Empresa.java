package br.com.project.equals.model;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

import br.com.project.equals.api.EmpresaService;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import retrofit2.Call;

public class Empresa implements Serializable {

    private String idUsuario;
    private String urlImagem;
    private String nome;
    private String tempo;
    private String categoria;
    private Double precoEntrega;
    private String endereco;
    private String logradouro;
    private int numero;
    private String bairro;
    private String complemento;
    private String cidade;
    private String cep;
    private String estado;


    public Empresa(){
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef.child("empresas")//criando um no de empresas
                .child(getIdUsuario());
        empresaRef.setValue(this);
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(final String endereco) {
        if(this.endereco.isEmpty()){
            EmpresaService empresaService = new EmpresaService() {

                @Override
                public Call<Empresa> recuperarLoja() {
                    String address = logradouro + " " +
                          numero + " " +
                          bairro + " " +
                          complemento + " " +
                          cidade + " " +
                          cep + " " + estado;
                    return null;
                }
            };
        } else {
            this.endereco = endereco;
        }
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getPrecoEntrega() {
        return precoEntrega;
    }

    public void setPrecoEntrega(Double precoEntrega) {
        this.precoEntrega = precoEntrega;
    }
}
