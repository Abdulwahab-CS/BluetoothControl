package com.example.bluetoothcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class LedControl extends Activity {

	Button  btnDis;
	String address = null;
	private ProgressDialog progress;
	BluetoothAdapter myBluetooth = null;
	BluetoothSocket btSocket = null;
	private boolean isBtConnected = false;
	private TextView lumn;
	//SPP UUID. Look for it

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent newint = getIntent();
		address = newint.getStringExtra(BTActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

		//view of the ledControl
		setContentView(R.layout.activity_led_control);
		lumn = (TextView) findViewById(R.id.textID);
		//call the widgtes

		new ConnectBT().execute(); //Call the class to connect
		
		//Polling each second
		Timer timer = new Timer();

		timer.schedule(new TimerTask() {      

			@Override
			public void run() {
				LedControl.this.runOnUiThread(new Runnable() {
					public void run() {
						startReading();
					}
				});
			}
		},0, 1000);
	}

	public void Disconnect(View view)
	{
		if (btSocket!=null) //If the btSocket is busy
		{
			try
			{//Nitsukla1
				btSocket.close(); //close connection
			}
			catch (IOException e)
			{ msg("Error");}
		}
		finish(); //return to the first layout

	}

	public void startReading(){
		String out = "";
		try {

			InputStream in = btSocket.getInputStream();
			int readable_char = in.available();
			for(int i11=0;i11<readable_char;i11++){
				int i=in.read();
				if(i!=-1)out += (char)i;
			}//Nitsukla1
			if(readable_char>0){
				WebRequest req = new WebRequest("http://192.168.1.160:8080/abc/RFIDServlet?card="
						+out+ "&mode=card");
				try {
					Toast.makeText(this, req.downloadUrl(), 1000).show();
				} catch (InterruptedException e) {
					Toast.makeText(this, "Error " + e.getMessage(), 1000).show();
					e.printStackTrace();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			out = e.getMessage();
		}
		if(out!=null && out.trim().length()!=0){
			lumn.setText("ID: "+out);

		}
	}

	// fast way to call Toast
	private void msg(String s)
	{
		Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
	}

	private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
	{
		private boolean ConnectSuccess = true; //if it's here, it's almost connected
		private String mssg="-1";
		@Override
		protected void onPreExecute()
		{
			if (btSocket == null || !isBtConnected){
				progress = ProgressDialog.show(LedControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
			}
		}

		@Override
		protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
		{
			try
			{
				if (btSocket == null || !isBtConnected)
				{
					myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
					BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
					//Java Reflection code
					Method m = dispositivo.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
					btSocket = (BluetoothSocket) m.invoke(dispositivo, 1);
					
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					btSocket.connect();//start connection
				}
			}
			catch (Exception e)
			{
				mssg=(e.getMessage());
				ConnectSuccess = false;//if the try failed, you can check the exception here
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
		{
			super.onPostExecute(result);
			if (!ConnectSuccess)
			{
				msg("Connection Failed. Is it a SPP Bluetooth? Try again. "+mssg );
				finish();  //This line closes the current screen
			}
			else
			{
				msg("Connected.");
				isBtConnected = true;
			}
			if(progress!=null)
				progress.dismiss();
		}

	}
}
