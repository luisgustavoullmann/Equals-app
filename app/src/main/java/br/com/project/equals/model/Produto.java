package br.com.project.equals.model;

import com.google.firebase.database.DatabaseReference;

import br.com.project.equals.helper.ConfiguracaoFirebase;

public class Produto {

    private String idUsuario;
    private String idProduto;
    private String nome;
    private String descricao;
    private int quantidade;
    private int preco;
    private String imagemProduto;

    public Produto(String nome, String descricao, int preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public Produto() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoRef = firebaseRef
                .child("produtos");
        setIdProduto(produtoRef.push().getKey());
    }

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoRef = firebaseRef
                .child("produtos")
                .child(getIdUsuario())
                .child(getIdProduto());
        produtoRef.setValue(this);
    }

    public void remover(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference produtoRef = firebaseRef
                .child("produtos")
                .child(getIdUsuario())
                .child(getIdProduto());
        produtoRef.removeValue();
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getPreco() {
        return preco;
    }

    public void setPreco(int preco) {
        this.preco = preco;
    }

    public String getImagemProduto() {
        return imagemProduto;
    }

    public void setImagemProduto(String imagemProduto) {
        this.imagemProduto = imagemProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
