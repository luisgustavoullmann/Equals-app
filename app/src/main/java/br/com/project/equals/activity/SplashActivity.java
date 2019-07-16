package br.com.project.equals;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import br.com.project.equals.AutenticacaoActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //esconde a action bar
        getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                abrirAutenticacao();
            }
        }, 3000);
    }

    //Metodo que abre a AutenticacaoActivity apos a SplashActivity
    private void abrirAutenticacao(){
        Intent intent = new Intent(SplashActivity.this, AutenticacaoActivity.class);
        startActivity(intent);
        finish(); //fechando a SplashActivity
    }
}
