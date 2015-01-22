package swin.chat;

import swin.chat.SwinChatService.SwinChatServiceBinder;
import swin.chat.util.Delegate;
import swin.chat.util.NotifyHelper;
import swin.chat.util.SwinChatConstants;
import swin.chat.util.SwinChatGlobals;
import swin.chat.util.TicketLinkedList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.c2dm.C2DMessaging;

public class GreetActivity extends Activity {
	
	private final static int MESSAGE_WHAT_SWINCHATSERVICE = 1;
	
	private SwinChatService.SwinChatServiceBinder scsBinder;
	private ServiceConnection swinChatServiceConnection;

	private class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what)
			{
			case MESSAGE_WHAT_SWINCHATSERVICE:
				switch(msg.arg1)
				{
				case SwinChatService.MESSAGE_ARG1_C2DM_REGISTER:
					onC2DMMessage(msg.getData());
					break;
				case SwinChatService.MESSAGE_ARG1_CHAT_LOGIN:
					onLoginMessage(msg.getData());
					break;
				}
				break;
			}
		}
	}
	
	private TicketLinkedList.Ticket<NotifyHelper> swinChatHandlerTicket;
	
	private MyHandler mHandler;
	
	private Button btnSkip;
	private TextView txtProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mHandler = new MyHandler();
		
		initUI(savedInstanceState);
		
		Intent intent = getIntent();
		if(SwinChatService.ACTION_C2DM_REGISTER.equals(intent.getAction()))
		{
			
		}
	}
	
	private void scsBinder_login()
	{
		txtProgress.setText("Logging in...");
		
		if(scsBinder != null)
		{
			scsBinder.login(SwinChatGlobals.SharedPreferences.getUser(getApplication()),
			SwinChatGlobals.SharedPreferences.getServerUri(getApplication()).toString());
		}
	}
	
	
	private void onC2DMMessage(Bundle data)
	{
		if(data.getInt(SwinChatService.EXTRA_ERROR_CODE, SwinChatService.ERRC_NO_ERROR) != SwinChatService.ERRC_NO_ERROR)
		{
			(new AlertDialog.Builder(this)).setTitle("C2DM Error!").setMessage(data.getString(SwinChatService.EXTRA_ERROR_MESSAGE)).create().show();
			return;
		}
		

		
		if(SwinChatGlobals.SharedPreferences.getUser(getApplication())== null || 
				SwinChatGlobals.SharedPreferences.getServerUri(getApplication())== null)
		{
			startLoginActivity();
			finish();
		}
		else
		{
			scsBinder_login();
		}		
	}
	
	private void onLoginMessage(Bundle data )
	{
		if(data.getInt(SwinChatService.EXTRA_ERROR_CODE, SwinChatService.ERRC_NO_ERROR) != SwinChatService.ERRC_NO_ERROR)
		{
			(new AlertDialog.Builder(this)).setTitle("Login Error!").setMessage(data.getString(SwinChatService.EXTRA_ERROR_MESSAGE)).create().show();
			startLoginActivity();
			finish();
			return;
		}
		
		startActivity(new Intent(GreetActivity.this, MainActivity.class));
		finish();
		
	}



	private void initUI(Bundle savedInstanceState) {

		setContentView(R.layout.greet);
		txtProgress = (TextView) findViewById(R.id.txtProgress);
		btnSkip = (Button) findViewById(R.id.btnSkip);

		initUIListeners();
	}

	private void initUIListeners() {
		this.btnSkip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startLoginActivity();
				finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("swin.chat", getClass() + " onPause");
		SwinChatGlobals.ActivityUtil.setCurrentActivity(null);
		
		if(scsBinder != null)
		{
			scsBinder.removeHandlerTicket(swinChatHandlerTicket);
			getApplication().unbindService(swinChatServiceConnection);
			scsBinder = null;
			swinChatHandlerTicket = null;
			swinChatServiceConnection = null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("swin.chat", getClass() + " onStart");
		SwinChatGlobals.ActivityUtil.setCurrentActivity(this);
		
		swinChatServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				scsBinder = (SwinChatServiceBinder) service;
				swinChatHandlerTicket = scsBinder.addHandler(mHandler, MESSAGE_WHAT_SWINCHATSERVICE, new Delegate<Message, Boolean>() {
					
					@Override
					public Boolean doStep(Message... params) {
						if(params[0].arg1 == SwinChatService.MESSAGE_ARG1_C2DM_REGISTER ||
								params[0].arg1 == SwinChatService.MESSAGE_ARG1_CHAT_LOGIN)
							return true;
						
						return false;
					}
				});
				
				scsBinder_login();	
				
			}
		};
		getApplication().startService(new Intent(getApplication(), SwinChatService.class));
		getApplication().bindService(new Intent(getApplication(), SwinChatService.class), swinChatServiceConnection, BIND_AUTO_CREATE);
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

}