package swin.chat;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.android.c2dm.C2DMessaging;

import swin.chat.SwinChatService.SwinChatServiceBinder;
import swin.chat.util.CallBackAsyncTask;
import swin.chat.util.CallBackAsyncTask.CallBack;
import swin.chat.util.Delegate;
import swin.chat.util.NotifyHelper;
import swin.chat.util.Server;
import swin.chat.util.SwinChatConstants;
import swin.chat.util.SwinChatGlobals;
import swin.chat.util.TicketLinkedList;
import swin.chat.util.TicketLinkedList.Ticket;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private final static int MESSAGE_WHAT_SWINCHATSERVICE = 0;
	
	
	private ProgressDialog progressDialog;

	private Button btnConnect;
	
	private EditText txtServer;
	private EditText txtUser;
	
	private class ScsHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_WHAT_SWINCHATSERVICE:
				switch(msg.arg1)
				{
				case SwinChatService.MESSAGE_ARG1_CHAT_LOGIN:
					scsHandler_login(msg);
					break;
				}
				break;
			}
		}

	};
	
	private Handler scsHandler;
	
	private SwinChatServiceBinder scsBinder;
	private ServiceConnection swinServiceConnection;
	private Ticket<NotifyHelper> scsTicket;	
	
	
	private View.OnClickListener btnConnectOnCLickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			String txtServerString = txtServer.getText().toString();
			URI uri = null;
			try {
				uri = new URI(txtServerString);
			} catch (URISyntaxException e) {
				Toast.makeText(LoginActivity.this, "server uri is not valid",
						Toast.LENGTH_LONG).show();
				Log.e("swin.chat", e.getMessage(), e);
				return;
			}

			if (uri.getScheme() == null) {
				try {
					uri = new URI("http:/" + txtServerString);
				} catch (URISyntaxException e) {
					Toast.makeText(LoginActivity.this,
							"server uri is not valid", Toast.LENGTH_LONG)
							.show();
					Log.e("swin.chat", e.getMessage(), e);
					return;
				}
			}

			if (uri.getHost() == null) {

				Toast.makeText(LoginActivity.this, "must be absolute host",
						Toast.LENGTH_LONG).show();
				return;
			}
			if (!uri.getScheme().equals("http")) {
				Toast.makeText(LoginActivity.this,
						uri.getScheme() + " is not supported",
						Toast.LENGTH_LONG).show();
				return;
			}


			progressDialog.setTitle("Please Wait...");
			progressDialog.setMessage("Logging in...");
			progressDialog.show();
			
			scsBinder.login(txtUser.getText().toString(), uri.toString());
		}
	};

	private void attachListeners() {
		btnConnect.setOnClickListener(btnConnectOnCLickListener);
	}

	private void findUIComponents() {
		progressDialog = new ProgressDialog(this);
		txtServer = (EditText) findViewById(R.id.txtServer);
		txtUser = (EditText) findViewById(R.id.txtUser);
		btnConnect = (Button) findViewById(R.id.btnConnect);
	}

	private void initialize(Bundle stateBundle) {
		

		URI serverUri = SwinChatGlobals.SharedPreferences
				.getServerUri(getApplication());
		String server = serverUri == null ? null : serverUri.toString();
		String user = SwinChatGlobals.SharedPreferences
				.getUser(getApplication());

		if (server != null)
			txtServer.setText(server);

		if (user != null)
			txtUser.setText(user);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("swin.chat", "LoginActivity.onCreate");
		
		scsHandler = new ScsHandler();
		setContentView(R.layout.login);
		findUIComponents();
		attachListeners();
		initialize(savedInstanceState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);

		txtServer.setText(savedInstanceState.getString("txtServer"));
		txtUser.setText(savedInstanceState.getString("txtUser"));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("txtServer", txtServer.getText().toString());
		outState.putString("txtUser", txtUser.getText().toString());
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("swin.chat", getClass() + " onStart");
		SwinChatGlobals.ActivityUtil.setCurrentActivity(this);
		
		swinServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				scsBinder = (SwinChatServiceBinder) service;
				scsTicket = scsBinder.addHandler(scsHandler, MESSAGE_WHAT_SWINCHATSERVICE, new Delegate<Message, Boolean>() {
					@Override
					public Boolean doStep(Message... params) {
						if(params[0].arg1 == SwinChatService.MESSAGE_ARG1_CHAT_LOGIN)
							return true;
						return false;
					}
				});
			}
		};
		getApplication().startService(new Intent(getApplication(), SwinChatService.class));
		getApplication().bindService(new Intent(getApplication(), SwinChatService.class), swinServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("swin.chat", getClass() + " onPause");
		SwinChatGlobals.ActivityUtil.setCurrentActivity(null);
		
		if(scsBinder != null)
		{
			scsBinder.removeHandlerTicket(scsTicket);
			getApplication().unbindService(swinServiceConnection);
			swinServiceConnection = null;
			scsBinder = null;
			scsTicket = null;
		}
	}
	
	private void scsHandler_login(Message msg) {
		progressDialog.dismiss();
		Bundle data = msg.getData();
		if(data.getInt(SwinChatService.EXTRA_ERROR_CODE, SwinChatService.ERRC_NO_ERROR) == SwinChatService.ERRC_NO_ERROR)
		{
			startActivity(new Intent(this, MainActivity.class));
			finish();
		}
		else
		{
			(new AlertDialog.Builder(this)).setTitle("Login Error").setMessage(data.getString(SwinChatService.EXTRA_ERROR_MESSAGE)).show();
		}
	}


}
