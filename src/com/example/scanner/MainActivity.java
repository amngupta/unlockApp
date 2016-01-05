package com.example.scanner;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

class accountDetails
{
	String account;
	String code;
	String username;
	String password;
}
public class MainActivity extends Activity {

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private static final String PREF_USERNAME = "username:";
	private static final String PREF_PASSWORD = "password:";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set the main content layout of the Activity
		setContentView(R.layout.activity_main);
	}


	//product qr code mode
	public void scanQR(View v) {
		try {
			//start the scanning activity from the com.google.zxing.client.android.SCAN intent
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			//on catch, show the download dialog
			showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	public void addAccount(View v)
	{

		setContentView(R.layout.activity_new_account);
	}
	public void addData(View v)
	{
		EditText username = (EditText) findViewById(R.id.username);
		EditText password = (EditText) findViewById(R.id.password);
		String user = username.getText().toString();
		String pass = password.getText().toString();
		@SuppressWarnings("unchecked")
		String account = ((AdapterView<SpinnerAdapter>) findViewById(R.id.spinner)).getSelectedItem().toString();
		if (user.length()>0 && pass.length()>0)
		{
			getSharedPreferences(account,MODE_PRIVATE)
			.edit()
			.putString(PREF_USERNAME, user)
			.putString(PREF_PASSWORD, pass)
			.commit();
			Toast.makeText(getApplicationContext(),"Saved Successfully",Toast.LENGTH_LONG).show();
			setContentView(R.layout.activity_main);
		}
		else
		{
			Toast.makeText(getApplicationContext(),"Please Enter All Details",Toast.LENGTH_LONG).show();
		}
	}
	//alert dialog for downloadDialog
	private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {

				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return downloadDialog.show();
	}


	//on ActivityResult method
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				//get the extras that are returned from the intent
				final String recievedString = intent.getStringExtra("SCAN_RESULT");
				String[] parts = recievedString.split("::");
				final String account=parts[0].toLowerCase();
				final String code=parts[1];
				
				SharedPreferences settings = getSharedPreferences(account, MODE_PRIVATE);
				final String user;
				final String pass;
				user = settings.getString(PREF_USERNAME, null);
				pass = settings.getString(PREF_PASSWORD, null);
//				Toast.makeText(this,"Message from Server: \n"+ recievedString, 0).show();             

				new Thread(){
					public void run(){
						sendData(account, code, user, pass);				    
						}
				}.start();
			}
		}
	}
	void sendData(String account, String code, String user, String pass)
	{
		HttpURLConnection connection;
		OutputStreamWriter request = null;

		URL url = null;   
		String response = null; 
		String data = "account:" + account + ",code:" + code + ",username:" + user + ",password:" + pass;
		accountDetails temp = new accountDetails();
		temp.account = account;
		temp.code = code;
		temp.password = pass;
		temp.username = user;
		try
		{
			url = new URL("https://phone-unlock.herokuapp.com/login");
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");    

			request = new OutputStreamWriter(connection.getOutputStream());
			request.write(data);
			request.flush();
			request.close();            
			String line = "";               
			InputStreamReader isr = new InputStreamReader(connection.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			// Response from server after login process will be stored in response variable.                
			response = sb.toString();
			// You can perform UI operations here
			Toast.makeText(this,"Message from Server: \n"+ response, 0).show();             
			isr.close();
			reader.close();

		}
		catch(Exception e)
		{
			
		}
	}
}
