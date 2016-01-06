package com.example.scanner;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
	private static final String PREF_KEY = "key:";

	private static String stringKey = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set the main content layout of the Activity
		setContentView(R.layout.activity_main);
	}

	//	void createButtons()
	//	{
	//		Resources res = getResources();
	//		String[] accounts = res.getStringArray(R.array.accounts);
	//		for (String account: accounts)
	//		{
	//			Log.w("accounts", account);
	//			Button btn= new Button(this);  
	//			btn.setText(account);  
	//			btn.setOnClickListener(new View.OnClickListener()   
	//			{
	//			    public void onClick(View view) 
	//			     {
	//			           scanQR(view);
	//			     }
	//			});
	//		}		
	//	}

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
	/*
	 * Open the NewAccount acitivity
	 */
	public void addAccount(View v)
	{
		setContentView(R.layout.activity_new_account);
	}

	/*
	 * Store the Data in Mobile Memory
	 */
	public void addData(View v)
	{
		EditText username = (EditText) findViewById(R.id.username);
		EditText password = (EditText) findViewById(R.id.password);
		String user = username.getText().toString();
		String pass = password.getText().toString();
		@SuppressWarnings("unchecked")
		String account = ((AdapterView<SpinnerAdapter>) findViewById(R.id.spinner)).getSelectedItem().toString().toLowerCase();
		if (user.length()>0 && pass.length()>0)
		{
			String encryptedUser = encrypt(user);
			String encryptedPass = encrypt(pass);
			if(stringKey != null){
				getSharedPreferences(account,MODE_PRIVATE)
				.edit()
				.putString(PREF_USERNAME, user)
				.putString(PREF_PASSWORD, encryptedPass)
				.putString(PREF_KEY, stringKey)
				.commit();
				Toast.makeText(getApplicationContext(),"Saved Successfully",Toast.LENGTH_LONG).show();
				setContentView(R.layout.activity_main);
				stringKey=null;
			}
		}
		else if (user.length() >0 && pass.length() == 0 )
		{
			Toast.makeText(getApplicationContext(),"Please Enter Password for " + account,Toast.LENGTH_LONG).show();
		}
		else if (user.length() == 0 && pass.length() > 0 )
		{
			Toast.makeText(getApplicationContext(),"Please Enter Username for " + account,Toast.LENGTH_LONG).show();
		}
		else
		{
			Toast.makeText(getApplicationContext(),"Please Enter Login Details for " + account,Toast.LENGTH_LONG).show();
		}
	}
	/*
	 * Method to encrypt a String using AES encryption
	 */
	private String encrypt(String plainData)
	{
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES"); 
			keyGen.init(128); 
			SecretKey secretKey = keyGen.generateKey(); 
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE,secretKey); 
			byte[] byteCipherText = aesCipher.doFinal(plainData.getBytes()); 
			String encrypted =  Base64.encodeToString(byteCipherText, Base64.DEFAULT);
			stringKey = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
			return encrypted;
		}
		catch(Exception e) { } 
		return null;
	}
	private String encrypt(String plainData, String key)
	{
		try{
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] digest = md.digest(key.getBytes());

			SecretKey secretKey = new SecretKeySpec(digest, "AES");
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE,secretKey); 
			byte[] byteCipherText = aesCipher.doFinal(plainData.getBytes()); 
			String encrypted =  Base64.encodeToString(byteCipherText, Base64.DEFAULT);
			Toast.makeText(this,"Message from Server: \n"+ encrypted, 0).show();             

			return encrypted;	
		}catch(Exception e)
		{
			Toast.makeText(this,"Exception Here", 0).show();             

		}
		return null;
	}
	private  String decrypt(String encryptedText, String key)
	{
		try{
			SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			byte[] encryptedTextBytes = Base64.decode(encryptedText, Base64.DEFAULT);
			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
			String decrypted = new String(decryptedTextBytes);
			Toast.makeText(this,"Message from Server: \n"+ decrypted, 0).show();             

			return decrypted;
		}
		catch(Exception e)
		{}
		return null;
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
				final String account=parts[0];
				final String code=parts[1];

				SharedPreferences settings = getSharedPreferences(account, MODE_PRIVATE);
				final String user  = settings.getString(PREF_USERNAME, null);
				String pass = settings.getString(PREF_PASSWORD, null);
				final String key = settings.getString(PREF_KEY, null);
				final String decryptPass = decrypt(pass, key);
				Toast.makeText(this,"Message from Server: \n"+ decryptPass, 0).show();             
				new Thread(){
					public void run(){
						sendData(account,code, user, decryptPass);				    
					}
				}.start();
			}
		}
	}

	/*
	 * Method to Post to Server
	 */
	@SuppressLint("ShowToast")
	void sendData(String account, String code, String user, String pass)
	{

		HttpURLConnection connection;
		OutputStreamWriter request = null;

		URL url = null;   
		String response = null; 
		String password = null;

		try
		{
			password = encrypt(pass,code);

			String data = "account!!!" + account + ",code!!!" + code + ",username!!!" + user + ",password!!!" + password;

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
