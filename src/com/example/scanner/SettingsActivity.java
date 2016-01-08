package com.example.scanner;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class SettingsActivity extends Activity {

	ArrayList<String> selectedAccounts = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
	}

	protected void onResume()
	{
		super.onResume();
		selectedAccounts.clear();
		addList();
		checkEmpty();

	}

	void checkEmpty()
	{
		Button del = (Button) findViewById(R.id.deleteAccount);
		if (selectedAccounts.isEmpty())
		{
			del.setVisibility(View.GONE);
		}
		else
		{
			del.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void deleteAccount(View v)
	{
		for (String account : selectedAccounts)
		{
			int code = account.hashCode();
			CheckBox chk = (CheckBox) findViewById(code);
			chk.setVisibility(View.GONE);
			SharedPreferences settings = getSharedPreferences(account, MODE_PRIVATE);
			settings.edit().clear().commit();
			selectedAccounts.remove(account);
			checkEmpty();
			Log.w("Exception", account + " Del");
		}
	}

	public void shareFB(View v)
	{
		share("com.facebook.katana");
	}

	public void share(String application) {

		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, "Hello World");
		intent.setPackage(application);
		startActivity(intent);


	}

	void addList()
	{
		LinearLayout yourlayout= (LinearLayout) findViewById(R.id.list);
		for (final String account : MainActivity.addedAccounts)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.topMargin = 10;
			params.leftMargin = 10;
			params.bottomMargin = 10;
			params.rightMargin = 10;
			params.height = 80;
			params.gravity = Gravity.TOP;
			final CheckBox btn = new CheckBox (this);
			btn.setLayoutParams(params);
			btn.setHeight(80);
			btn.setGravity(Gravity.CENTER);
			btn.setId(account.hashCode());
			btn.setText(account);
			btn.setTextSize(24);
			btn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					if(btn.isChecked())
					{
						selectedAccounts.add(account);
						checkEmpty();
					}
					else
					{
						selectedAccounts.remove(account);
						checkEmpty();
					}
				}	
			});
			yourlayout.addView(btn);
		}
	}

}
