package com.example.scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class AddAccount extends Activity {

	static String stringKey = null;
	static String accSent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_account);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
             accSent = (String) data.getExtras().get("account");
        }
		setContentView(R.layout.activity_new_account);

    }
	
	/*
	 * Method to get data from the textfields. 
	 */
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
			saveData(account, user, pass);
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
	 * Method to store the data in mobile memory
	 */
	private void saveData(String account, String user, String pass)
	{
		String encryptedPass = EncryptionFunctions.encrypt(pass);
		if(stringKey != null){
			getSharedPreferences(account, MODE_PRIVATE)
			.edit()
			.putString(MainActivity.PREF_USERNAME, user)
			.putString(MainActivity.PREF_PASSWORD, encryptedPass)
			.putString(MainActivity.PREF_KEY, stringKey)
			.commit();
			Log.w("Exception", "Username and Pass Saved SaveData() " + user + " " + encryptedPass);
			Toast.makeText(getApplicationContext(),"Saved Successfully",Toast.LENGTH_LONG).show();
			finish();
			stringKey=null;
		}
	}
}
