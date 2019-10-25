package br.com.project.equals.model;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Region;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.BeaconService;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public abstract class MainApplication extends Application implements BootstrapNotifier {

    public static BeaconManager beaconManager;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    public static Region region1;

    @Override
    public void onCreate() {
        super.onCreate();

        // Pegando o objeto bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Checka se o bluetooth é suportado pelo device
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth Not Supported",Toast.LENGTH_LONG).show();
        } else {
            // Se o bluetooth é suportado e não está habilitado, pede para habilitar
            if (!mBluetoothAdapter.isEnabled()) {
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                bluetoothIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(bluetoothIntent);
            }
        }

        // Pega a Application do BeaconManager
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);

        //Para detectar beacons proprietarios (como estimote), você precisa adicionar a linha do layout correspondente
        // ao tipo de beacon. Procure por "setBeaconLayout" para buscar outros exemplos
        // No exemplo abaixo, está funcionando para buscar os beacons da estimote (possuem exemplo no aplicativo de simular
        //beacons
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        //Altera o default de tempo que os beacons são procuradso
        beaconManager.setForegroundScanPeriod(1100l);
        beaconManager.setForegroundBetweenScanPeriod(0l);

        //Se estiver usando um device API 21+m usa a API Android L para buscar beacons
        beaconManager.setAndroidLScanningDisabled(true);

        //Seta a duração em milisegundos para o ciclo de busca BLE quando o clientes não estão em foreground
        beaconManager.setBackgroundBetweenScanPeriod(01);
        beaconManager.setBackgroundScanPeriod(1100l);

        try {
            //Altera as informações
            beaconManager.updateScanPeriods();
        } catch (Exception e) {
        }

        // acorda quando o beacon é acordado
        region1 = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region1);
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    /*
        //Avisa que achou um beacon
     */
    @Override
    public void didEnterRegion(Region region) {
        try {

            //Iniciando BeaconService que extende a classe serviceService
            Intent i = new   Intent(getApplicationContext(), BeaconService.class);
            startService(i);
        } catch (Exception e){}
    }

    /*
        Avisa que o beacon parou de ser lido
     */
    @Override
    public void didExitRegion(Region region) {

        try {
            //Iniciando BeaconService que extende a classe serviceService
            Intent k = new Intent(getApplicationContext(),BeaconService.class);
            startService(k);
        }
        catch (Exception e) {
        }

    }

    /*
      Este metodo determina o estado do serviço 0 - desligado | 1 - ligado
     */
    @Override
    public void didDetermineStateForRegion(int i, Region region) {

        try {
            //Iniciando BeaconService que extende a classe serviceService
            Intent k = new Intent(getApplicationContext(), BeaconService.class);
            startService(k);
        }
        catch (Exception e) {
        }
    }

}
