package br.com.project.equals.model;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

import br.com.project.equals.helper.ConfiguracaoFirebase;

public class Usuario implements Serializable {

    private String idUsuario;
    private String nome;
    private String endereco;
    private String telefone;
    private String cpf;
        private String urlImagem;

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference empresaRef = firebaseRef.child("empresas")//criando um no de empresas
                .child(getIdUsuario());
        empresaRef.setValue(this);
    }

    public Usuario() {
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
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

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }
}
