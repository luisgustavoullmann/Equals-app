package br.com.project.equals.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import br.com.project.equals.R;

public class NovoProdutoEmpresaActivity extends AppCompatActivity {

    /*
    * -----------ATENÇÃO--------------------------
    * É nessa Activity que devemos desenvolver o OCR para cadastro do produto
    * Desenvolvi apenas um modelo simples para podermos testar as outras
    * funcionalidades do sistema.
    *
    * A ideia é criamos um Button que aciona o OCR
    * Com a lib de um OCR (como Tesseract, exemplo)
    * Acionaremos a camera do cel, empresa tira DUAS fotos
    * Uma para salvar a imagem do produto
    * E outra para o OCR reconhecer a imagem da price tag
    * Atribuindo os respectivos valores para cada atributo
    * */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_produto_empresa);

        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo Produto");
        setSupportActionBar(toolbar);
        //Add o botao de voltar
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }
}
