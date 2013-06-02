package no.skogvoldconsulting.pulse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.UUID;

import no.skogvoldconsulting.pulse.audio.BeepThread;
import no.skogvoldconsulting.pulse.audio.PulseAudio;
import no.skogvoldconsulting.pulse.audio.SilentAudio;
import no.skogvoldconsulting.pulse.audio.WaveThread;
import no.skogvoldconsulting.pulse.sensor.PulseMonitorThread;
import no.skogvoldconsulting.pulse.sensor.SensorListener;

public class MainActivity extends Activity implements SensorListener {

	private static final int REQUEST_ENABLE_BT = 666;
	private static final String TAG = MainActivity.class.getName();
	private TextView pulseView;
	private TextView packetView;
	private TextView fracView;
	private PulseAudio pulseAudio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pulseView = (TextView) findViewById(R.id.pulseText);
		packetView = (TextView) findViewById(R.id.packetView);
		fracView = (TextView) findViewById(R.id.fracView);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		setAudio(new SilentAudio());
		
		startPulseMonitor();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_beat:
            setAudio(new WaveThread(this));
			break;
		case R.id.action_beep:
			setAudio(new BeepThread());
			break;
		case R.id.action_silent:
            setAudio(new SilentAudio());
            break;
		}
		return super.onOptionsItemSelected(item);
	}

    private void setAudio(PulseAudio pulseAudio) {
        if(this.pulseAudio != null)
            this.pulseAudio.cancel();
        this.pulseAudio = pulseAudio;
        pulseAudio.start();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				startPulseMonitor();
			} else {
				finish();
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void startPulseMonitor() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		verifyEnabled(adapter);

		final BluetoothDevice polar = findPolar(adapter);

		Thread t = new PulseMonitorThread(polar, this);
		t.start();
	}

	public void setPacket(final byte[] packet) {
		final int heartRate = packet[5] & 0xFF;
		Log.d(TAG, "New pulse: " + heartRate);
		final StringBuilder byteSb = new StringBuilder();
		final StringBuilder fracSb = new StringBuilder();
		for (byte b : packet) {
			byteSb.append(String.format("%02X ", b));
			fracSb.append(String.format("%.2f ", (b & 0xff) / (double) 0xff));
		}

		getWindow().getDecorView().getHandler().post(new Runnable() {
			public void run() {
				pulseView.setText(Integer.toString(heartRate));
				packetView.setText(byteSb.toString());
				fracView.setText(fracSb.toString());
				pulseAudio.setBpm(heartRate);
			};
		});
	}


	private BluetoothDevice findPolar(BluetoothAdapter adapter) {
		for (BluetoothDevice device : adapter.getBondedDevices()) {
			if ("Polar iWL".equals(device.getName())) {
				return device;
			}
		}
		throw new NoSuchElementException(
				"No Polar iWL connected. Pair it first");
	}

	private void verifyEnabled(BluetoothAdapter adapter) {
		if (!adapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}




}
