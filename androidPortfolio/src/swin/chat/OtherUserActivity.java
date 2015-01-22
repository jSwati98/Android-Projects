package swin.chat;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import swin.chat.SwinChatService.SwinChatServiceBinder;
import swin.chat.model.User;
import swin.chat.util.CallBackAsyncTask;
import swin.chat.util.Delegate;
import swin.chat.util.NotifyHelper;
import swin.chat.util.Server;
import swin.chat.util.SwinChatConstants;
import swin.chat.util.SwinChatGlobals;
import swin.chat.util.TicketLinkedList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OtherUserActivity extends Activity {



	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_WHAT_SWINCHATSERVICE:
				switch (msg.arg1) {
				case SwinChatService.MESSAGE_ARG1_LIST_USERS:
					otherUserAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}
	
	
	private class UserAdapter extends ArrayAdapter<User> {
		private class RowHolder {
			public ImageView rowIcon;
			public TextView rowLabel;
		}


		public UserAdapter(Context context, int resource,
				int textViewResourceId, List<User> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.otheruserlistrow,
						parent, false);
			}

			RowHolder holder = (RowHolder) convertView.getTag();
			if (holder == null) {
				holder = new RowHolder();
				holder.rowIcon = (ImageView) convertView
						.findViewById(R.id.row_icon);
				holder.rowLabel = (TextView) convertView
						.findViewById(R.id.row_label);

				convertView.setTag(holder);
			}
			User user = getItem(position);
			holder.rowLabel.setText(user.getName());
			return convertView;
		}
		
	}
	
	private final static int MESSAGE_WHAT_SWINCHATSERVICE = 1;
	

	private Handler handler;
	private TicketLinkedList.Ticket<NotifyHelper> scsHandlerTicket;

	private LayoutInflater layoutInflater;
	private ListView listView;
	private Button btnRefresh;

	private UserAdapter otherUserAdapter;

	private SwinChatService.SwinChatServiceBinder scsBinder;

	private ServiceConnection swinChatServiceConnection;
	

	private void findUIComponents() {
		listView = (ListView) findViewById(R.id.listOtherUser);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		attachEventListeners();
	}
	
	private void attachEventListeners()
	{
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				User user = (User) parent.getItemAtPosition(position);
				
				Intent chatIntent = new Intent(OtherUserActivity.this, ChatActivity.class);
				chatIntent.putExtra(ChatActivity.EXTRA_USER, user.getName());
				startActivity(chatIntent);
			}
		});
		
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(scsBinder != null)
				{
					scsBinder.updateOtherUserList();
				}
			}
		});
	}


	private void initialize() {
		handler = new MyHandler();
		
		layoutInflater = getLayoutInflater();
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("swin.chat", OtherUserActivity.class.getName() + ".onCreate");
		setContentView(R.layout.otheruser);
		findUIComponents();
		initialize();
	}

	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i("swin.chat", OtherUserActivity.class.getName() + ".onStart");
		
		swinChatServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				scsBinder = (SwinChatServiceBinder) service;
				
				otherUserAdapter = new UserAdapter(OtherUserActivity.this, R.layout.otheruserlistrow,
						R.id.row_label, scsBinder.getOtherUserList());
				listView.setAdapter(otherUserAdapter);
				
				scsHandlerTicket = scsBinder.addHandler(handler, MESSAGE_WHAT_SWINCHATSERVICE, new Delegate<Message, Boolean>() {
					
					@Override
					public Boolean doStep(Message... params) {
						if(params[0].arg1 == SwinChatService.MESSAGE_ARG1_LIST_USERS)
							return true;
						else
							return false;
						
					}
				});
				scsBinder.updateOtherUserList();
			}
		};
		getApplication().startService(new Intent(getApplication(), SwinChatService.class));
		getApplication().bindService(new Intent(getApplication(), SwinChatService.class), swinChatServiceConnection, BIND_AUTO_CREATE);
		
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i("swin.chat", OtherUserActivity.class.getName() + ".onPause");
		
		if(scsBinder != null)
		{

			scsBinder.removeHandlerTicket(scsHandlerTicket);
			getApplication().unbindService(swinChatServiceConnection);
			scsBinder = null;
			swinChatServiceConnection = null;
			scsHandlerTicket = null;
		}
	}
}
