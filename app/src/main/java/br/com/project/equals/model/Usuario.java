package br.com.project.equals.model;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

import br.com.project.equals.api.UsuarioService;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import retrofit2.Call;

public class Usuario implements Serializable {

    private String idUsuario;
    private String nome;
    private String endereco;
    private String telefone;
    private String cpf;
    private String urlImagem;
    private String logradouro;
    private int numero;
    private String bairro;
    private String complemento;
    private String cidade;
    private String cep;
    private String estado;

    public void salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference ususarioRef = firebaseRef.child("usuarios")//criando um no de usuarios
                .child(getIdUsuario());
        ususarioRef.setValue(this);
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
        if(this.endereco.isEmpty()){
            UsuarioService usuarioService = new UsuarioService() {

                @Override
                public Call<Usuario> recuperarUsuario() {
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
