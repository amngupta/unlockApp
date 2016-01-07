package com.example.scanner;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;


public class MainActivity extends Activity {

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	public static final String PREF_USERNAME = "username:";
	public static final String PREF_PASSWORD = "password:";
	public static final String PREF_KEY = "key:";
	public static String account = null;
	ArrayList<Integer> hashcodes = new ArrayList<Integer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart()
	{   
		super.onStart();
	}


	@Override
	protected void onResume()
	{   
		super.onResume();		
		createButtons();

	}

	void createButtons()
	{
		String[] accounts =  (getResources().getStringArray(R.array.accounts));
		LinearLayout yourlayout= (LinearLayout) findViewById(R.id.container);
		for (final String accountBtn: accounts)
		{
			SharedPreferences settings = getSharedPreferences(accountBtn, MODE_PRIVATE);
			int accountCode = accountBtn.hashCode();
			String user  = settings.getString(PREF_USERNAME, null);	
			if(user != null && hashcodes.contains(accountCode)==false)
			{
				hashcodes.add(accountCode);
				Button btn = new Button (this);
				btn.setId(accounts.hashCode());
				btn.setWidth(250);
				btn.setHeight(80);
				btn.setText(accountBtn);

				//btn.setBackgroundColor(Color.BLUE);
				btn.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						account = accountBtn;
						scanQR();
					}	
				});
				yourlayout.addView(btn);
			}
		}
	}

	/*
	 * Method that scans the QR Code
	 */
	public void scanQR() {
		try {
			//start the scanning activity from the com.google.zxing.client.android.SCAN intent
			Intent intent = new Intent(ACTION_SCAN);
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			//on catch, show the download dialog
			showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}
	/*
	 * Open the NewAccount acitivity
	 */
	public void addAccount(View v)
	{
		Intent nextScreen = new Intent(getApplicationContext(), AddAccount.class);
		startActivity(nextScreen);
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
	private AlertDialog promptDialog(final Activity act, final String title, final String account)
	{
		AlertDialog.Builder prompt = new AlertDialog.Builder(act);
		prompt.setTitle(title);
		prompt.setMessage("Account Creditentials not found. Do you want to add?");
		if(account != null){
			prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					Intent nextScreen = new Intent(getApplicationContext(), AddAccount.class);
					nextScreen.putExtra("account", account);
					startActivityForResult(nextScreen, 100);			
				}
			});
			prompt.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
				}
			});
		}
		else {
			prompt.setMessage("There was an error. Please restart the app!");
		}
		return prompt.show();

	}
	/*
	 *Method that handles the result from the qrcode scan
	 */	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				//get the extras that are returned from the intent
				try{
					String codeSent = null;
					final String returnedString = intent.getStringExtra("SCAN_RESULT");
					if(returnedString.contains("::"))
					{
						String[] parts = returnedString.split("::");				
						codeSent = parts[1];
						Log.w("Exception", "String is here " + account +"   "+codeSent);
					}
					else
						codeSent = returnedString;
					final String code = codeSent;
					SharedPreferences settings = getSharedPreferences(account, MODE_PRIVATE);
					final String user  = settings.getString(PREF_USERNAME, null);
					String pass = settings.getString(PREF_PASSWORD, null);
					final String key = settings.getString(PREF_KEY, null);
					final String decryptPass = EncryptionFunctions.decrypt(pass, key);
					account = account.toLowerCase();
					if(user==null || pass == null)
					{
						promptDialog(MainActivity.this,"Account Not Found", account).show();
					}
					else{
						new Thread(){
							public void run(){
								sendData(account,code, user, decryptPass);				    
							}
						}.start();
					}}catch(Exception e)
				{
						promptDialog(MainActivity.this, "Entered A Problem", null).show();
				}
			}

		}
	}
	/*
	 * Method to Post to Server
	 */
	void sendData(String account, String code, String user, String pass)
	{

		HttpURLConnection connection;
		OutputStreamWriter request = null;

		URL url = null;   
		String response = null; 
		String password = null;
		password = EncryptionFunctions.encrypt(pass,code);

		try
		{
			String data = "account!!!" + account + ",code!!!" + code + ",username!!!" + user + ",password!!!" + password;
			Log.w("Exception", data);
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
			isr.close();
			reader.close();

		}
		catch(Exception e)
		{

		}
	}
}
