package com.example.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_account, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent nextScreen = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(nextScreen);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
			stringKey=null;
		}
		else
		{
			promptDialog(AddAccount.this,"Account Not Found", account).show();
		}
		finish();
	}
}
