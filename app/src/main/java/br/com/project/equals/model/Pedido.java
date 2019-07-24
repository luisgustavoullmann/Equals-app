package br.com.project.equals.model;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

import br.com.project.equals.helper.ConfiguracaoFirebase;

public class Pedido {

    private String idUsuario;
    private String idEmpresa;
    private String idPedido;
    private String nome;
    private String endereco;
    private List<ItemPedido> itens;
    private Double total;
    private String status = "pendente";
    private int metodoPagamento;
    private String observacao;

    //posição do usuário no map e talvez foto (imageView do usuario),
    // ver comentários em:
    // adapter_pedido,
    // AdapterPedido
    // activity_pedidos,
    // ProdutoActivity (confirmarPedido())
    // e nesse model (Pedido), metodos confirmar e remover


    public Pedido() {
    }

    public Pedido(String idUsuario, String idEmpresa) {
        setIdUsuario(idUsuario);
        setIdEmpresa(idEmpresa);

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuario);
        setIdPedido(pedidoRef.push().getKey());
    }


    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(getIdEmpresa())
                .child(getIdUsuario());
        pedidoRef.setValue(this);
    }

    //Caso queira fazer um estrutura de pedidos que o usuario
    //Podemos manter o no "pedidos",
    // utilizando idEmpresa e idUsuario, podemos recuparar essas informações
    //cria um no de pedidos pendentes para a empresa
    //mas ainda continua pendente para os usuarios
    public void confirmar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos")
                .child(getIdEmpresa())
                .child(getIdPedido());
        pedidoRef.setValue(this);
    }

    //Poderiamos criar um no chamado "meus_pedidos",
    //aonde retornamos todos os pedidos do usuario, para o usuario
    //baseado no idUsuario.
    //No temporario, assim que o usuario confirmar
    // é criado o no de pedidos para empresa
    public void remover(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(getIdEmpresa())
                .child(getIdUsuario());
        pedidoRef.removeValue();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
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

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(int metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }
}
