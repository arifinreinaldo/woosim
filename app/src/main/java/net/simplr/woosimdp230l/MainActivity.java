package net.simplr.woosimdp230l;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dascom.print.connection.BluetoothConnection;
import com.dascom.print.utils.BluetoothUtils;

import net.simplr.woosimdp230l.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {
    SharedPreferences sp;
    private final String sp_file = "woosimdp230lmac";
    private final String sp_mac = "macaddress";
    private String mac;
    private MainPresenter presenter;
    private String[] arrArgs;
    ActivityMainBinding binding;
    BluetoothConnection bluetoothConnection;
    AdapterDevice adapter;
    List<Device> listDevice = new ArrayList<>();
    String action, value;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d("Printer", "onNewIntent: ");
        processData();
    }

    private void processData() {
        Intent intent = getIntent();
        action = intent.getStringExtra("ACTION_PRINT");
        value = intent.getStringExtra("TXT_TO_PRINT");
        arrArgs = intent.getStringArrayExtra("ARR_TO_PRINT");
        if (action == null) {
            action = "";
        }
        if (value == null) {
            value = "";
        }
        if (arrArgs == null) {
            arrArgs = new String[0];
        }
        if (arrArgs.length > 0) {
            Log.d("Printer", "processData: "+arrArgs.length);
            presenter.processArray(arrArgs);
        } else if (!action.isEmpty() || !value.isEmpty()) {
            Log.d("Printer", "data: " + action + " - " + value);
            presenter.processArgument(action, value);
        } else {
            mac = sp.getString(sp_mac, "");
            if (mac.isEmpty()) {
                //create adapter
                binding.listDevice.setLayoutManager(new LinearLayoutManager(this));
                binding.listDevice.setAdapter(adapter);
                checkActivateBluetooth();
            } else {
                try {
                    Toast.makeText(getApplicationContext(), "Need to call from external application", Toast.LENGTH_SHORT).show();
                    Thread.sleep(500);
                    finishAffinity();
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sp = getSharedPreferences(sp_file, Context.MODE_PRIVATE);
        presenter = new MainPresenter(this, sp);
        adapter = new AdapterDevice(this, listDevice);
        adapter.setClickListener((view, position) -> {
                    Toast.makeText(getBaseContext(), "Address selected", Toast.LENGTH_SHORT).show();
                    presenter.saveBluetoothAddress(adapter.getItem(position).getAddress());
                }
        );
        Log.d("Printer", "onCreate: ");
        processData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkActivateBluetooth() {
        if (!BluetoothUtils.isEnable()) {
            BluetoothUtils.openBluetooth(this, isOn -> {
                if (isOn) {
                    presenter.getBluetoothDevice();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to activate bluetooth", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            });
        }
        presenter.getBluetoothDevice();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showLoading() {
        Log.d("Printer", "showLoading: ");
//        binding.loading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.loading.setVisibility(View.GONE);
    }


    @Override
    public void isConnected(boolean bool) {
        Toast.makeText(getApplicationContext(), "isConnected " + bool, Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra("isSuccess", bool);
        data.putExtra("message", "");
        setResult(RESULT_OK, data);
        finishAffinity();
    }

    @Override
    public void closeActivity(boolean bool, String message) {
//        Intent data = new Intent();
//        data.putExtra("isSuccess", bool);
//        data.putExtra("message", message);
//        setResult(RESULT_OK, data);
        this.finishAffinity();
//        System.exit(0);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete() {
        finishAffinity();
//        System.exit(0);
    }

    @Override
    public void showBluetoothData(List<Device> devices) {
        if (!devices.isEmpty()) {
            adapter.setData(devices);
            binding.loading.setVisibility(View.GONE);
            binding.data.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getApplicationContext(), "No Bluetooth Found", Toast.LENGTH_SHORT).show();
            finishAffinity();
        }

    }
}