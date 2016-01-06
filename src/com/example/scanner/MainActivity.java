package com.example.scanner;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	private static final String PREF_USERNAME = "username:";
	private static final String PREF_PASSWORD = "password:";
	private static final String PREF_KEY = "key:";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	/*
	 * Method that scans the QR Code
	 */
	public void scanQR(View v) {
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

	private String encrypt(String plainData, String key)
	{
		String encrypted = null;
		if (plainData == null)
		{
			Log.w("Exception", "Strig was empty");
		}
		else{
			try{
				byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
				Key secretKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");  
				Cipher aesCipher = Cipher.getInstance("AES");
				aesCipher.init(Cipher.ENCRYPT_MODE, secretKey); 
				byte[] byteCipherText = aesCipher.doFinal(plainData.getBytes()); 
				encrypted =  Base64.encodeToString(byteCipherText, Base64.DEFAULT);
				return encrypted;	
			}catch(Exception e)
			{
				Log.w("Exception", e.toString());
			}
		}
		return encrypted;

	}

	private String decrypt(String encryptedText, String key)
	{
		String decrypted = null;
		try{
			byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
			Key secretKey = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");  
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] encryptedTextBytes = Base64.decode(encryptedText, Base64.DEFAULT);
			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
			decrypted = new String(decryptedTextBytes);
			return decrypted;
		}
		catch(Exception e)
		{
			Log.w("Exception", e.toString());
		}
		return decrypted;
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

	private static AlertDialog promptDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo)
	{
		AlertDialog.Builder prompt = new AlertDialog.Builder(act);
		prompt.setTitle(title);
		prompt.setMessage(message);
		prompt.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				act.setContentView(R.layout.activity_new_account);
			}
		});
		prompt.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
			}
		});
		return prompt.show();
		
	}

	
	/*
	*Method that handles the result from the qrcode scan
	*/	
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
				if(user==null && pass == null)
				{
					promptDialog(MainActivity.this, "Account Not Found", 
						"You have not added creditentials for " + account + " account. Add them?", 
						"Yes", "No").show();
				}
				else{
					new Thread(){
						public void run(){
							sendData(account,code, user, decryptPass);				    
						}
					}.start();
				}}
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
		password = encrypt(pass,code);

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
