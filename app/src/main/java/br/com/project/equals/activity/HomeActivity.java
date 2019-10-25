package br.com.project.equals.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Region;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.com.project.equals.R;
import br.com.project.equals.adapter.AdapterEmpresa;
import br.com.project.equals.helper.ConfiguracaoFirebase;
import br.com.project.equals.listener.RecyclerItemClickListener;
import br.com.project.equals.model.Empresa;
import br.com.project.equals.model.Produto;

public class HomeActivity extends AppCompatActivity implementes BeaconConsumer {

    private static final String TAG = "HomeActivity";
    private BeaconManager beaconManager;
    private FirebaseAuth autenticacao;
    private MaterialSearchView searchView;
    private RecyclerView recyclerEmpresa;
    private List<Empresa> empresas = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private AdapterEmpresa adapterEmpresa;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Setup da Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Equals");
        setSupportActionBar(toolbar);

        //Configuração recyclerView
        recyclerEmpresa.setLayoutManager(new LinearLayoutManager(this));
        recyclerEmpresa.setHasFixedSize(true);
        adapterEmpresa = new AdapterEmpresa(empresas);
        recyclerEmpresa.setAdapter(adapterEmpresa);

        //Recupera empresas
        recuperarEmpresas();

        //Configuração do search view
        searchView.setHint("Pesquisar");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                pesquisarEmpresas(newText);
                return true;
            }
        });

        //Configuração evento de clique nas empresas
        recyclerEmpresa.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerEmpresa,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Empresa empresaSelecionada = empresas.get(position);
                                Intent intent = new Intent(HomeActivity.this, Produto.class);
                                intent.putExtra("empresa", empresaSelecionada);
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                )
        );

    }


    //Pesquisa empresa por nome, usando search view no activity inicial
    private void pesquisarEmpresas(String pesquisa){
        DatabaseReference empresasRef = firebaseRef
                .child("empresas");
        Query query = empresasRef.orderByChild("nome")
                .startAt(pesquisa)
                .endAt(pesquisa + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                empresas.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarEmpresas(){
        DatabaseReference empresaRef = firebaseRef.child("empresas");
        empresaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                empresas.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    empresas.add(ds.getValue(Empresa.class));
                }
                adapterEmpresa.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Configuração do menu da toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usuario, menu);

        //Configuracoes do btn de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(item);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSair :
                deslogarUsuario();
                break;
            case R.id.menuConfiguracoes :
                abrirConfiguracoes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){
        searchView = findViewById(R.id.materialSearchView);
        recyclerEmpresa = findViewById(R.id.recyclerEmpresa);
        myBeacon();
    }

    private void deslogarUsuario(){
        try {
            autenticacao.signOut();
            finish(); //sem o finish() não desloga realmente
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void abrirConfiguracoes(){
        startActivity(new Intent(HomeActivity.this, ConfiguracoesUsuarioActivity.class));
    }

    private void myBeacon() {
        //Beacon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Este aplicativo precisa de localização em background");
                        builder.setMessage("Por favor, ative o acesso a localização para que o este app possa detectar os beacons em backgroud");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        PERMISSION_REQUEST_BACKGROUND_LOCATION);
                            }

                        });
                        builder.show();
                    }
                    else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Funcionalidade Limitada");
                        builder.setMessage("Caso não haja acesso a localização em background, os beacons não irão funcionar.  Por favor vá para Configurações -> Aplicativos -> Permissões de localização para este aplicativo..");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }

                }
            } else {
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Funcionalidade Limitada");
                    builder.setMessage("Caso não haja acesso a localização, os beacons não irão funcionar.  Por favor vá para Configurações -> Aplicativos -> Permissões de localização para este aplicativo.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);

        //Para detectar beacons proprietarios (como estimote), você precisa adicionar a linha do layout correspondente
        // ao tipo de beacon. Procure por "setBeaconLayout" para buscar outros exemplos
        // No exemplo abaixo, está funcionando para buscar os beacons da estimote (possuem exemplo no aplicativo de simular
        //beacons
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        @Override
        public void onDestroy(){
            super.onDestroy();
            beaconManager.unbind(this);
        }


        @Override
        public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
            switch (requestCode) {
                case PERMISSION_REQUEST_FINE_LOCATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Permissão aceita");
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Funcionalidade Limitada");
                        builder.setMessage("Caso não haja acesso a localização, os beacons não irão funcionar.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                    return;
                }
                case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Aceita a localização em background");
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Funcionalidade limitada");
                        builder.setMessage("Caso não haja acesso a localização em background, os beacons não irão funcionar.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                    return;
                }
            }
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection){
            unbindService(serviceConnection);
        }

        @Override
        public void onBeaconServiceConnection(){
            //Constroi nova região para monitorar
            final Region region = new Region("myBeacons", null, null, null);

            //Adciona uma notificação para ser chamada
            beaconManager.addMonitorNotifier(new MonitorNotifier(){
                /*
                This override method is runned when some beacon will come under the range of device.
            */
                @Override
                public void didEnterRegion(Region region) {
                    System.out.println("----- ENTER -----");
                    try {
                        //Avisa que achou um beacon
                        beaconManager.startRangingBeaconsInRegion(region);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                /*
                     This override method is runned when beacon that comes in the range of device
                     ,now been exited from the range of device.
                 */
                @Override
                public void didExitRegion(Region region) {
                    System.out.println("----- EXIT -----");
                    try {

                        //Avisa que o beacon parou de ser lido
                        beaconManager.stopRangingBeaconsInRegion(region);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }


                /*
                     Este metodo determina o estado do serviço 0 - desligado | 1 - ligado
                */
                @Override
                public void didDetermineStateForRegion(int state, Region region) {
                    System.out.println( "I have just switched from seeing/not seeing beacons: "+state);
                }
            });

            //Adiciona a notificação para ser chamada ser BeaconService pegou uma informação
            beaconManager.addRangeNotifier(new RangeNotifier() {

                /*
                   This Override method tells us all the collections of beacons and their details that
                   are detected within the range by device
                 */
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {


                    // Se o Beacon é encontrado, o tamanho é maior que 0
                    if (beacons.size() > 0) {
                        final ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();

                        // Pega a informação de todos os beacons encontrados
                        for (Beacon b:beacons){

                            //UUID
                            String uuid = String.valueOf(b.getId1());

                            //Major
                            String major = String.valueOf(b.getId2());

                            //Minor
                            String minor = String.valueOf(b.getId3());

                            Log.v("Test",uuid + " " + major + " " + minor);

                        }
                    }
                    // Se nenhum beacon foi encontrado, retorna 0
                    else if (beacons.size()==0) {

                    }
                }
            });

            try{
                //Iniciando monitoramento dos beacons
                beaconManager.startMonitorBeaconsInRegion(region);
            } catch (RemoteException e){
                //
            }
        }
    }
}
