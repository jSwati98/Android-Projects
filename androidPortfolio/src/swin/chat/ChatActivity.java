package swin.chat;

import swin.chat.SwinChatService.SwinChatServiceBinder;
import swin.chat.model.User;
import swin.chat.util.Delegate;
import swin.chat.util.MessageDBHelper;
import swin.chat.util.NotifyHelper;
import swin.chat.util.SwinChatGlobals;
import swin.chat.util.TicketLinkedList.Ticket;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ChatActivity extends Activity {
	
	private final class ScsHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what)
			{
			case MESSAGE_WHAT_SWINCHATSERVICE:
				switch(msg.arg1)
				{
				case SwinChatService.MESSAGE_ARG1_NEW_MESSAGE:
					scsHandler_new_message();
					break;
				case SwinChatService.MESSAGE_ARG1_LIST_USERS:
					scsHandler_list_user();
					break;
				}
				break;
			}
		}
	}
	
	private final class ChatAdapter extends CursorAdapter
	{
		
		private class RowTag
		{

			public TextView lblUser;
			public TextView lblMessage;
			
		}

		public ChatAdapter(Context context, Cursor c, boolean autoRequery) {
			super(context, c, autoRequery);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return super.getView(position, convertView, parent);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			RowTag rowTag = (RowTag) view.getTag();
			if(rowTag == null)
			{
				rowTag = new RowTag();
				rowTag.lblUser = (TextView) view.findViewById(R.id.row_lblUser);
				rowTag.lblMessage = (TextView) view.findViewById(R.id.row_lblMessage);
				view.setTag(rowTag);
			}
			String user = cursor.getString(cursor.getColumnIndex(MessageDBHelper.FROMUSER));
			if(user.equals(SwinChatGlobals.SharedPreferences.getUser(getApplication())))
				user = "You";
			rowTag.lblUser.setText(user);
			rowTag.lblMessage.setText(cursor.getString(cursor.getColumnIndex(MessageDBHelper.MESSAGE)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.chat_list_row, parent, false);
			bindView(v, context, cursor);
			return v;
		}
		
	}

	public final static String EXTRA_USER = "swin.chat.ChatActivity.EXTRA_USER";
	public final static int MESSAGE_WHAT_SWINCHATSERVICE = 1;
	
	private final static String STATE_TOUSERNAME = "swin.chat.ChatActivity.STATE_TOUSERNAME";
	
	private ListView listChat;
	private EditText txtMessage;
	private Button btnSend;
	private CursorAdapter chatAdapter;
	
	private Ticket<NotifyHelper> scsHandlerTicket;
	
	private SwinChatServiceBinder scsBinder;
	private ServiceConnection swinChatServiceConnection;
	
	private Handler scsHandler;
	private User toUser;
	private String toUserName;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		scsHandler = new ScsHandler();
		loadUI();
		if(savedInstanceState != null)
			restoreState(savedInstanceState);
		else
			initState();
	}
	
	private void initState() {
		toUserName = getIntent().getStringExtra(EXTRA_USER);
	}

	private void loadUI()
	{
		setContentView(R.layout.chat);
		listChat = (ListView) findViewById(R.id.listChat);
		txtMessage = (EditText) findViewById(R.id.txtMessage);
		btnSend = (Button) findViewById(R.id.btnSend);
		
		attachUIListeners();
	}
	
	private void attachUIListeners()
	{
		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnSend_onClick(v);	
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		
		
		swinChatServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				scsBinder = (SwinChatServiceBinder) service;
				scsHandlerTicket = scsBinder.addHandler(scsHandler, MESSAGE_WHAT_SWINCHATSERVICE, new Delegate<Message, Boolean>() {
					
					@Override
					public Boolean doStep(Message... params) {
						if(params[0].arg1 == SwinChatService.MESSAGE_ARG1_NEW_MESSAGE ||
								params[0].arg1 == SwinChatService.MESSAGE_ARG1_MESSAGE_SENT)
							return true;
						return false;
					}
				});
				scsHandler_list_user();
			}
		};
		getApplication().startService(new Intent(getApplication(), SwinChatService.class));
		getApplication().bindService(new Intent(getApplication(), SwinChatService.class), swinChatServiceConnection, BIND_AUTO_CREATE);
		
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(swinChatServiceConnection != null)
		{
			scsBinder.removeHandlerTicket(scsHandlerTicket);
			getApplication().unbindService(swinChatServiceConnection);
			scsBinder = null;
			swinChatServiceConnection = null;
			scsHandlerTicket = null;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_TOUSERNAME, toUserName);
	}
	
	private void restoreState(Bundle state)
	{
		toUserName = state.getString(STATE_TOUSERNAME);
	}
	
	
	private void scsHandler_list_user()
	{
		toUser = scsBinder.findOtherUser(toUserName);
		if(toUser == null)
			onToUserOffline();
		else
			onToUserOnline();
	}
	
	private void scsHandler_new_message()
	{
		if(toUser != null)
		{
			MessageDBHelper mdb = SwinChatGlobals.DB.getMessagedbhelper(getApplication());
			
			SQLiteDatabase db = mdb.getReadableDatabase();
			Cursor cursor = db.query(MessageDBHelper.TB_NAME, new String[]{
					"_ID",
					MessageDBHelper.MESSAGEID,
					MessageDBHelper.FROMUSER,
					MessageDBHelper.TOUSER,
					MessageDBHelper.DATETIME,
					MessageDBHelper.MESSAGE,
					"datetime(" + MessageDBHelper.DATETIME + ") as dt1"
					
			}, "(" + MessageDBHelper.TOUSER + "=? AND " + MessageDBHelper.FROMUSER+"=?) OR " 
				+ "(" + MessageDBHelper.TOUSER + "=? AND " + MessageDBHelper.FROMUSER+"=?)", new String[]{
					SwinChatGlobals.SharedPreferences.getUser(getApplication()),
					toUserName,
					toUserName,
					SwinChatGlobals.SharedPreferences.getUser(getApplication())},null,null,"dt1");
			
			Log.i("swin.chat", "scsHandler_new_message select " + cursor.getCount());
			db.close();
			
			if(chatAdapter == null)
			{	
				chatAdapter = new ChatAdapter(this, cursor, false);
				listChat.setAdapter(chatAdapter);
			}
			else
			{
				chatAdapter.changeCursor(cursor);
				chatAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private void btnSend_onClick(View v)
	{
		if(scsBinder !=null)
		{
			scsBinder.sendMessage(toUserName, txtMessage.getText().toString());
			txtMessage.setText("");
			scsHandler_new_message();
		}
	}
	
	private void onToUserOffline()
	{
		btnSend.setClickable(false);
	}
	
	private void onToUserOnline()
	{
		btnSend.setClickable(true);
		scsHandler_new_message();
	}
	
	
}
