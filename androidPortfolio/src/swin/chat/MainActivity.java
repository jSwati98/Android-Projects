package swin.chat;

import java.io.IOException;

import org.json.JSONException;

import com.google.android.c2dm.C2DMessaging;

import swin.chat.SwinChatService.SwinChatServiceBinder;
import swin.chat.util.CallBackAsyncTask;
import swin.chat.util.Server;
import swin.chat.util.Delegate;
import swin.chat.util.SwinChatConstants;
import swin.chat.util.SwinChatGlobals;
import swin.chat.util.SwinChatGlobals.SharedPreferences;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	private MenuItem menuItemLogout;
	
	private SwinChatServiceBinder scsBinder;
	private ServiceConnection swinChatServiceConnection;


	private void initUI() {
		setContentView(R.layout.main);
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

		TabSpec tabSpecUser = tabHost
				.newTabSpec("tabUser")
				.setContent(
						new Intent(MainActivity.this,
								OtherUserActivity.class))
				.setIndicator("Online users");
		TabSpec tabSpecSettings = tabHost.newTabSpec("tabSettings").setContent(new Intent(this, SettingsActivity.class)).setIndicator("Settings");
		tabHost.addTab(tabSpecUser);
		tabHost.addTab(tabSpecSettings);
		tabHost.setCurrentTab(0);
	}



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem.OnMenuItemClickListener clickListener = new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return MainActivity.this.onMenuItemClick(item);
			}
		};

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		menuItemLogout = menu.findItem(R.id.item_logout);
		menuItemLogout.setOnMenuItemClickListener(clickListener);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("swin.chat" , getClass() + " onStart");
		SwinChatGlobals.ActivityUtil.setCurrentActivity(this);
		
		swinChatServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				scsBinder = (SwinChatServiceBinder) service;
			}
		};
		
		startService(new Intent(getApplication(),SwinChatService.class));
		bindService(new Intent(getApplication(), SwinChatService.class), swinChatServiceConnection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i("swin.chat" , getClass() + " onPause");
		SwinChatGlobals.ActivityUtil.setCurrentActivity(null);
		
		if(scsBinder != null)
		{
			unbindService(swinChatServiceConnection);
			scsBinder = null;
			swinChatServiceConnection = null;
		}
	}
 

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	private boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_logout: {
			return menuItemLogout_onMenuItemClick(item);
		}
		default:
			break;
		}
		return false;
	}

	private boolean menuItemLogout_onMenuItemClick(MenuItem item) {
		scsBinder.logout();
		startLoginActivity();
		finish();
		return true;
	}

}