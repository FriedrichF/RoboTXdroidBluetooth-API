package de.thserv.robodroid;

import java.util.*;
import de.thserv.robotxdroid.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final String TAG = "MainActivity";

	private ListView lVScan = null;

	private TextView tVScan = null;
	private ProgressBar pBScan = null;

	private ListView lVAvailableDevices = null;
	private ArrayAdapter<String> aAdapter = null;
	// Verf�gbare Ger�te
	private ArrayList<String> aLAdapterAvailable = new ArrayList<String>();
	// Gepaarte GEr�te
	private ArrayList<String> aLAdapterPaired = new ArrayList<String>();

	// Adapter f�r lVScan
	private ArrayList<String> aLAdapterScan = new ArrayList<String>();

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//Wenn ein Ger�t gefunden wird
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Abfragen des Bluetoothger�ts vom Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// Hinzuf�gen des Namens und der Adresse des gefundenen Ger�tes zu einem Array und zur Anzeige in einer ListView
				if (device.getName() != null) {
					String deviceName = device.getName() + "\n"
							+ device.getAddress();

					if (aLAdapterAvailable.contains(deviceName) == false) {
						MainActivity.this.aLAdapterAvailable.add(deviceName);

						MainActivity.this.aAdapter.notifyDataSetChanged();
					}
				}

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				tVScan.setVisibility(View.INVISIBLE);
				pBScan.setVisibility(View.INVISIBLE);
			}
		}
	};

	//Optionen men� laden
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	//Ausf�hren wenn ein Element im Optionenmen� gedr�ckt wird
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//Laden des Tutorials
		case R.id.menu_settings:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);

			builder.setTitle(R.string.st_tutMainTitle)
					.setMessage(R.string.st_tutMainMessage)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Tutorial wird geschlossen
								}
							});

			AlertDialog alert = builder.create();
			alert.show();
			return true;

		}
		return true;
	}

	//Initialisierung
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tVScan = (TextView) findViewById(R.id.id_tVMainPB);
		pBScan = (ProgressBar) findViewById(R.id.id_pBMainPB);
		tVScan.setVisibility(View.INVISIBLE);
		pBScan.setVisibility(View.INVISIBLE);

		lVScan = (ListView) findViewById(R.id.id_lVMain);

		ArrayAdapter<String> adapterScan = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_list_item_1,
				aLAdapterScan);
		lVScan.setAdapter(adapterScan);
		adapterScan.add("Scannen nach Ger�ten");

		lVScan.setClickable(true);
		lVScan.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				MainActivity.this.aLAdapterAvailable.clear();

				MainActivity.this.aAdapter.notifyDataSetChanged();

				// Pr�fen ob Bluetooth aktiviert ist
				if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {

					tVScan.setVisibility(View.VISIBLE);
					pBScan.setVisibility(View.VISIBLE);

					BluetoothAdapter.getDefaultAdapter().startDiscovery();

				} else {
					// Aktivieren BT request
					Intent enableIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent,
							("SCAN_BLUETOOTH_REUQUEST").hashCode());
				}
			}
		});

		// Hinzuf�gen von gefundenen Ger�ten zur ListView
		lVAvailableDevices = (ListView) findViewById(R.id.id_lVMainDevices);
		aAdapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, aLAdapterAvailable);
		lVAvailableDevices.setAdapter(aAdapter);

		// Hinzuf�gen eines OnItemClickListener zur ListView
		lVAvailableDevices.setClickable(true);
		lVAvailableDevices.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// MAC-Address
				String[] nameAndMac = ((String) ((TextView) arg1).getText())
						.split("\n");
				String mac = nameAndMac[1];

				// Abfragen von gepaarten Ger�ten
				Set<BluetoothDevice> pairedDevices = BluetoothAdapter
						.getDefaultAdapter().getBondedDevices();
				// Fals gepaarte GEr�te verf�gbar sind
				if (pairedDevices.size() > 0) {
					// Schleife durch die gepaarten Ger�te
					for (BluetoothDevice device : pairedDevices) {
						// Gepaarte zur ListView hinzuf�gen
						aLAdapterPaired.add(device.getName() + "\n"
								+ device.getAddress());
					}
				}

				// Pr�fen ob das gew�hlte Ger�t gepaart ist
				if (aLAdapterPaired.contains(nameAndMac[0] + "\n"
						+ nameAndMac[1])) {
					// http://developer.android.com/reference/android/bluetooth/BluetoothSocket.html#connect%28%29
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					Log.d(TAG, mac);

					Intent intent = new Intent(MainActivity.this,
							ControllActivity.class);
					intent.putExtra("mac", mac);
					MainActivity.this.startActivity(intent);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);

					builder.setTitle(R.string.st_aDMainTitle)
							.setMessage(R.string.st_aDMainMessage)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// Paaren der GEr�te
											Intent pairDeIntent = new Intent();
											pairDeIntent
													.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
											startActivityForResult(
													pairDeIntent, 1);

										}
									});

					AlertDialog alert = builder.create();
					alert.show();

				}
			}
		});

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(broadcastReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(broadcastReceiver, filter);
	} 

	protected void onResume() {
		super.onResume();
		lVScan.getOnItemClickListener().onItemClick(lVScan, lVScan, 0, 0);
	}

	protected void onDestroy() {
		super.onDestroy();

		this.unregisterReceiver(this.broadcastReceiver);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ("SCAN_BLUETOOTH_REUQUEST").hashCode()) {
			if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
				BluetoothAdapter.getDefaultAdapter().startDiscovery();
			} else {
				Toast.makeText(this, R.string.st_toMainBtDisabled,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

} 