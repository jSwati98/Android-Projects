package swin.chat;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import swin.chat.model.ChatMessage;
import swin.chat.model.User;
import swin.chat.util.CallBackAsyncTask;
import swin.chat.util.CallBackAsyncTask.CallBack;
import swin.chat.util.Delegate;
import swin.chat.util.MessageDBHelper;
import swin.chat.util.NotifyHelper;
import swin.chat.util.Server;
import swin.chat.util.SwinChatConstants;
import swin.chat.util.SwinChatGlobals;
import swin.chat.util.TicketLinkedList;
import swin.chat.util.TicketLinkedList.Ticket;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.google.android.c2dm.C2DMessaging;

public class SwinChatService extends Service {
	
	
	public final static int NOTIFY_REQID = 1;
	
	public final static String ACTION_C2DM_REGISTER = "swin.chat.SwinChatService.ACTION_C2DM_REGISTER";
	public final static String ACTION_C2DM_ERROR = "swin.chat.SwinChatService.ACTION_C2DM_ERROR";
	public final static String ACTION_C2DM_UNREGISTERED = "swin.chat.SwinChatService.ACTION_C2DM_UNREGISTERED";
	
	public static final String ACTION_LOGOUT = "swin.chat.SwinChatService.ACTION_LOGOUT";
	public static final String ACTION_LOGIN = "swin.chat.SwinChatService.ACTION_LOGIN";
	public static final String ACTION_NEW_MESSAGE = "swin.chat.SwinChatService.ACTION_NEW_MESSAGE";
	public static final String ACTION_SENDING_MESSAGE = "swin.chat.SwinChatService.ACTION_SENDING_MESSAGE";
	public static final String ACTION_MESSAGE_SENT = "swin.chat.SwinChatService.ACTION_MESSAGE_SENT";
	
	public static final String EXTRA_ERROR_MESSAGE = "swin.chat.SwinChatService.EXTRA_ERROR_MESSAGE";
	public static final String EXTRA_ERROR_CODE = "swin.chat.SwinChatService.EXTRA_ERROR_CODE";
	
	public static final int ERRC_NO_ERROR = 0;
	public static final int ERRC_HANDSHAKE_ERROR = 1;
	public static final int ERRC_LOGIN_ERROR = 2;
	
	public static final int ERRC_C2DM_ERROR = 3;
	public static final int ERRC_LIST_USER_ERROR = 4;
	public static final int ERRC_SEND_MESSAGE = 5;
	
	public final static int MESSAGE_ARG1_C2DM_REGISTER = 1;
	
	public final static int MESSAGE_ARG1_CHAT_LOGIN = 2;
	public final static int MESSAGE_ARG1_CHAT_LOGOUT = 3;
	public final static int MESSAGE_ARG1_LIST_USERS = 4;
	public static final int MESSAGE_ARG1_NEW_MESSAGE = 5;
	public static final int MESSAGE_ARG1_SENDING_MESSAGE = 6;
	public static final int MESSAGE_ARG1_MESSAGE_SENT = 7;

	
	private class C2DMResponseReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("swin.chat", "receive push intent " + intent.getAction());
			
			if(C2DMReceiver.BROADCAST_ACTION_REGISTERED.equals(intent.getAction()))
			{
				
				notifyClient(notifyList, MESSAGE_ARG1_C2DM_REGISTER, ACTION_C2DM_REGISTER,
						"Registered with Google!",
						"App now can push message!",
						NOTIFY_REQID, 
						PendingIntent.FLAG_CANCEL_CURRENT, 
						SwinChatConstants.Notification.DEFAULT_ID, 
						notificationHandlerActivityClass, intent.getExtras());
			}
			else if(C2DMReceiver.BROADCAST_ACTION_ERROR.equals(intent.getAction()))
			{
				Bundle errorBundle = new Bundle();
				String errorMessage =  intent.getStringExtra(C2DMReceiver.EXTRA_ERROR_MESSAGE);
				errorBundle.putInt(EXTRA_ERROR_CODE, ERRC_C2DM_ERROR);
				errorBundle.putString( EXTRA_ERROR_MESSAGE, errorMessage);
				
				notifyClient(notifyList, MESSAGE_ARG1_C2DM_REGISTER, ACTION_C2DM_REGISTER,
						"Error message",
						errorMessage,
						NOTIFY_REQID, 
						PendingIntent.FLAG_CANCEL_CURRENT, 
						SwinChatConstants.Notification.DEFAULT_ID, 
						notificationHandlerActivityClass, errorBundle);
			}
			else if(C2DMReceiver.BROADCAST_ACTION_MESSAGE.equals(intent.getAction()))
			{
				String collapseKey = intent.getStringExtra(SwinChatConstants.ServerContracts.C2DM.COLLAPSE_KEY);
				if(SwinChatConstants.ServerContracts.C2DM.CollapseKeys.LIST_USERS.equals(collapseKey))
				{
					updateOtherUserList();
				}
				else if(SwinChatConstants.ServerContracts.C2DM.CollapseKeys.NEW_MESSAGE.equals(collapseKey))
				{
					newMessage();
				}
			}
		}
		
	}
	public class SwinChatServiceBinder extends Binder
	{
		public Ticket<NotifyHelper> addHandler(Handler handler, int messageWhat, Delegate<Message, Boolean> isHandlingMessage)
		{
			return notifyList.add(new NotifyHelper(handler, messageWhat, isHandlingMessage));
		}
		
		public void removeHandlerTicket(Ticket<NotifyHelper> ticket)
		{
			notifyList.remove(ticket);
		}
		
		public List<User> getOtherUserList()
		{
			return otherUserList;
		}
		
		public void login(String userName, String serverURI)
		{
			SwinChatService.this.login(userName, serverURI);
		}
		
		public void updateOtherUserList()
		{
			SwinChatService.this.updateOtherUserList();
		}
		
		public User findOtherUser(String name)
		{
			return otherUserMapByName.get(name);
		}
		
		public SwinChatService getService()
		{
			return SwinChatService.this;
		}
		
		public void sendMessage(String toUser, String message)
		{
			SwinChatService.this.sendMessage(toUser, message);			
		}
		
		public void logout()
		{
			SwinChatService.this.logout();
		}
	}
	
	private final TicketLinkedList<NotifyHelper> notifyList = new TicketLinkedList<NotifyHelper>();
	
	private C2DMResponseReceiver c2dmReceiver = new C2DMResponseReceiver();
	
	private final SwinChatServiceBinder mBinder = new SwinChatServiceBinder();
	
	private Class<? extends Activity> notificationHandlerActivityClass = GreetActivity.class;
	
	private ArrayList<User> otherUserList = new ArrayList<User>();
	private HashMap<String, User> otherUserMapByName = new HashMap<String, User>();
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(C2DMReceiver.BROADCAST_ACTION_ERROR);
		filter.addAction(C2DMReceiver.BROADCAST_ACTION_REGISTERED);
		filter.addAction(C2DMReceiver.BROADCAST_ACTION_UNREGISTERED);
		filter.addAction(C2DMReceiver.BROADCAST_ACTION_MESSAGE);
		registerReceiver(c2dmReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(c2dmReceiver);
		super.onDestroy();
	}

	
	private void notifyClient(TicketLinkedList<NotifyHelper> clientList, int arg1Code, String action, String notificationTitle, 
			String notificationMessage,
			int notifyRequestId,
			int pendingIntentFlag,
			int notificationId,
			Class<? extends Activity> activityClass,
			Bundle data)
	{

		boolean handled = false;
		
		for(TicketLinkedList.Ticket<NotifyHelper> ticket : clientList)
		{
			Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
			m.arg1 = arg1Code;
			m.setData(data);
			
			if(ticket.getElement().isHandlingMessage.doStep(m))
			{
				handled = true;
				ticket.getElement().handler.sendMessage(m);
			}
			
		}
			

		if(!handled)
		{
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification n = new Notification(R.drawable.icon, notificationTitle, System.currentTimeMillis());
			Intent intentToSent = new Intent(this, activityClass);
			intentToSent.setPackage(this.getPackageName());
			intentToSent.setAction(action);
			intentToSent.putExtras(data);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, notifyRequestId, 
					intentToSent, pendingIntentFlag);
			n.setLatestEventInfo(this, /*context*/
					notificationTitle,
					notificationMessage,
					pendingIntent);
			nm.notify(notificationId, n);
		}
	}
	
	

	private void login(final String userName, final String serverURI) {
		
		final Delegate<Void, Void> doLogin = new Delegate<Void, Void>() {
			@Override
			public Void doStep(Void... params) {
				Server.handshakeInBackground(serverURI,
						new CallBackAsyncTask.CallBack<Void, Void, Boolean>() {

							@Override
							public void onPostExecute(
									Boolean result,
									android.os.AsyncTask<Void, Void, Boolean> asyncTask,
									Exception exception) {
								Log.i("swin.chat", getClass()
										+ " initHandshake execute");
								if (exception != null) {
									Log.e("swin.chat", "Handshake error", exception);
									Bundle errorBundle = new Bundle();
									errorBundle.putString(EXTRA_ERROR_MESSAGE, exception.getMessage());
									errorBundle.putInt(EXTRA_ERROR_CODE, ERRC_HANDSHAKE_ERROR);
									notifyClient(notifyList, MESSAGE_ARG1_CHAT_LOGIN,
											ACTION_LOGIN, 
											"Handshake error!", exception.getMessage(),
											NOTIFY_REQID, PendingIntent.FLAG_CANCEL_CURRENT, 
											SwinChatConstants.Notification.DEFAULT_ID,notificationHandlerActivityClass, 
											errorBundle);
									return;
								}
								// handshake success
								// now logging in
								
								if(SwinChatGlobals.SharedPreferences.getUser(getApplication()) != null && 
										SwinChatGlobals.SharedPreferences.getServerUri(getApplication())!= null)
									Server.logoutInBackground(SwinChatGlobals.SharedPreferences.getUser(getApplication()),
											SwinChatGlobals.SharedPreferences.getServerUri(getApplication()).toString(),null);

								Server.loginInBackground(
										userName,
										serverURI,
										C2DMessaging
												.getRegistrationId(getApplication()),
										new CallBackAsyncTask.CallBack<Void, Void, Boolean>() {
											
											@Override
											public void onPostExecute(
													Boolean result,
													android.os.AsyncTask<Void, Void, Boolean> asyncTask,
													Exception exception) {
												Log.i("swin.chat", getClass()
														+ " finish logging in");
												if (exception != null) {
													Log.e("swin.chat",
															"Login Error",
															exception);
													Bundle errorBundle = new Bundle();
													errorBundle.putString(EXTRA_ERROR_MESSAGE, exception.getMessage());
													errorBundle.putInt(EXTRA_ERROR_CODE, ERRC_LOGIN_ERROR);
													notifyClient(notifyList, MESSAGE_ARG1_CHAT_LOGIN,
															ACTION_LOGIN, 
															"Login error!", exception.getMessage(),
															NOTIFY_REQID, PendingIntent.FLAG_CANCEL_CURRENT, 
															SwinChatConstants.Notification.DEFAULT_ID,notificationHandlerActivityClass, 
															errorBundle);
												}
												else
												{
													try {
														SwinChatGlobals.SharedPreferences.setServerURI(new URI(serverURI), getApplication());
														SwinChatGlobals.SharedPreferences.setUser(userName, getApplication());
														
													} catch (URISyntaxException e) {}
													
													notifyClient(notifyList, MESSAGE_ARG1_CHAT_LOGIN,
															ACTION_LOGIN, 
															"Logged In Success!","You have successfully logged in!",
															NOTIFY_REQID, PendingIntent.FLAG_CANCEL_CURRENT, 
															SwinChatConstants.Notification.DEFAULT_ID,
															notificationHandlerActivityClass, 
															new Bundle());
												}
											}
										});
							}
				});
				return null;
			}
		};
		
		if(C2DMessaging.getRegistrationId(getApplication()).equals(""))
		{
			Handler c2dmHandler = new Handler(){
				Ticket<NotifyHelper> scsTicket = mBinder.addHandler(this, 0, new Delegate<Message, Boolean>() {
					@Override
					public Boolean doStep(Message... params) {
						if(params[0].arg1 == MESSAGE_ARG1_C2DM_REGISTER)
							return true;
						return false;
					}
				});
				
				@Override
				public void handleMessage(Message msg) {
					switch(msg.arg1)
					{
					case MESSAGE_ARG1_C2DM_REGISTER:
						{
							mBinder.removeHandlerTicket(scsTicket);
							
							Bundle data = msg.getData();
							if(data.getInt(SwinChatService.EXTRA_ERROR_CODE, SwinChatService.ERRC_NO_ERROR) != SwinChatService.ERRC_NO_ERROR)
							{
								notifyClient(notifyList, MESSAGE_ARG1_CHAT_LOGIN, ACTION_LOGIN, "Registration error",
										data.getString(EXTRA_ERROR_MESSAGE), NOTIFY_REQID, PendingIntent.FLAG_CANCEL_CURRENT,
										SwinChatConstants.Notification.DEFAULT_ID, notificationHandlerActivityClass, data);
								return;
							}
							doLogin.doStep();
							break;
						}
					}
					
				}
			};
			
			C2DMessaging.register(getApplication(), SwinChatConstants.C2DM_SENDER_ID);
		}
		else
			doLogin.doStep();
	}
	
	private void updateOtherUserList() {
		String userName = SwinChatGlobals.SharedPreferences.getUser(this
				.getApplication());
		URI server = SwinChatGlobals.SharedPreferences.getServerUri(this
				.getApplication());
		 
		Server.getOtherUsersInBackground(userName, server.toString(),
				new CallBackAsyncTask.CallBack<Void, Void, List<User>>() {
					@Override
					public void onPostExecute(List<User> result,
							AsyncTask<Void, Void, List<User>> asyncTask,
							Exception exception) {
						if (result != null) {
							otherUserList.clear();
							otherUserMapByName.clear();
							
							otherUserList.addAll(result);
							for(User user : result)
							{
								otherUserMapByName.put(user.getName(), user);
							}
							
							for(TicketLinkedList.Ticket<NotifyHelper> ticket : notifyList)
							{
								Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
								m.arg1 = MESSAGE_ARG1_LIST_USERS;
								ticket.getElement().handler.sendMessage(m);
							}
							
						} else if (exception != null) {
							Log.e("swin.chat", "exception when updating other user list",
									exception);
							
							for(TicketLinkedList.Ticket<NotifyHelper> ticket : notifyList)
							{
								Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
								m.arg1 = MESSAGE_ARG1_LIST_USERS;
								m.getData().putString(EXTRA_ERROR_MESSAGE, exception.getMessage());
								m.getData().putInt(EXTRA_ERROR_CODE, ERRC_LIST_USER_ERROR);
								ticket.getElement().handler.sendMessage(m);
							}
						}
					}
				});
		
	}
	
	
	public void sendMessage(String toUser,String message)
	{
		MessageDBHelper mdb = SwinChatGlobals.DB.getMessagedbhelper(getApplication());		
		SQLiteDatabase db = mdb.getWritableDatabase();
		ContentValues cv = new ContentValues();
		
		final Date now = new Date();
		long lastId1 = -1;
		
		SimpleDateFormat sdf = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		cv.put(MessageDBHelper.MESSAGEID,-1);
		cv.put(MessageDBHelper.TOUSER, toUser);
		cv.put(MessageDBHelper.FROMUSER,SwinChatGlobals.SharedPreferences.getUser(getApplication()));
		cv.put(MessageDBHelper.MESSAGE, message);
		cv.put(MessageDBHelper.DATETIME, sdf.format(now));
		
		db.beginTransaction();

		try
		{
			lastId1 = db.insert(MessageDBHelper.TB_NAME, "_id", cv);
			db.setTransactionSuccessful();

		}finally
		{
			db.endTransaction();
		}
		
		db.close();
		mdb.close();
		final long lastId = lastId1;
		
		Server.sendMessageInBackground(
				SwinChatGlobals.SharedPreferences.getUser(getApplication()),
				C2DMessaging.getRegistrationId(getApplication()),
				toUser, 
				SwinChatGlobals.SharedPreferences.getServerUri(getApplication()).toString(),
				message, new CallBack<Void, Void, JSONObject>(){
					@Override
					public void onPostExecute(JSONObject result,
							AsyncTask<Void, Void, JSONObject> asyncTask,
							Exception exception) {
						if(result != null)
						{
							MessageDBHelper mdb = SwinChatGlobals.DB.getMessagedbhelper(getApplication());	
							SQLiteDatabase db = mdb.getWritableDatabase();
							
							ContentValues cv = new ContentValues();
							cv.put(MessageDBHelper.MESSAGEID, result.optString(SwinChatConstants.ServerContracts.SendMessage.R_INSERTID));
							
							db.beginTransaction();
							try
							{
								db.update(MessageDBHelper.TB_NAME,cv, "rowid=?",new String[]{String.valueOf(lastId)});
								db.setTransactionSuccessful();
							}
							finally
							{
								db.endTransaction();
							}
							db.close();
							mdb.close();
							notifyClient(notifyList, MESSAGE_ARG1_MESSAGE_SENT, ACTION_MESSAGE_SENT, 
									"Message sent", "message sent",NOTIFY_REQID, 
									PendingIntent.FLAG_CANCEL_CURRENT, SwinChatConstants.Notification.DEFAULT_ID, 
									notificationHandlerActivityClass, new Bundle());
						}
						else if(exception != null)
						{
							Log.e("swin.chat", exception.getMessage(), exception);
							Bundle errorBundle = new Bundle();
							errorBundle.putString(EXTRA_ERROR_MESSAGE, exception.getMessage());
							errorBundle.putInt(EXTRA_ERROR_CODE, ERRC_SEND_MESSAGE);
							notifyClient(notifyList, MESSAGE_ARG1_MESSAGE_SENT,
									ACTION_MESSAGE_SENT, 
									"Message sending error!", exception.getMessage(),
									NOTIFY_REQID, PendingIntent.FLAG_CANCEL_CURRENT, 
									SwinChatConstants.Notification.DEFAULT_ID,notificationHandlerActivityClass, 
									errorBundle);
						}
					}
				});
		
	}
	
	private void newMessage()
	{
		Server.getMessagesInBackground(SwinChatGlobals.SharedPreferences.getUser(getApplication()), 
			SwinChatGlobals.SharedPreferences.getLastMessageDateTime(getApplication()),
			SwinChatGlobals.SharedPreferences.getServerUri(getApplication()).toString(),
			new CallBackAsyncTask.CallBack<Void, Void, List<ChatMessage>>()
			{
				@Override
				public void onPostExecute(java.util.List<ChatMessage> result, android.os.AsyncTask<Void,Void,java.util.List<ChatMessage>> asyncTask, Exception exception) {
					
					if(result != null)
					{
						SQLiteDatabase db = SwinChatGlobals.DB.getMessagedbhelper(getApplication()).getWritableDatabase();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						ChatMessage message = null;
						
						for(ChatMessage msg1: result)
						{
							msg1.getDateTime();
							ContentValues cv = new ContentValues();
							cv.put(MessageDBHelper.MESSAGEID, msg1.getMessageId());
							cv.put(MessageDBHelper.TOUSER, msg1.getToUser());
							cv.put(MessageDBHelper.FROMUSER, msg1.getFromUser());
							cv.put(MessageDBHelper.MESSAGE, msg1.getMessage());
							cv.put(MessageDBHelper.DATETIME, sdf.format(msg1.getDateTime()));
							
							db.beginTransaction();
							try
							{
								db.insert(MessageDBHelper.TB_NAME, "_id", cv);
								db.setTransactionSuccessful();
							}finally
							{
								db.endTransaction();
							}
							message = msg1;
						}
						
						db.close();
						
						if(message != null)
							notifyClient(notifyList, MESSAGE_ARG1_NEW_MESSAGE, ACTION_NEW_MESSAGE, 
								"New Message From " + message.getFromUser(), message.getMessage(),NOTIFY_REQID, 
								PendingIntent.FLAG_CANCEL_CURRENT, SwinChatConstants.Notification.DEFAULT_ID, 
								notificationHandlerActivityClass, new Bundle());
					}
					
				};
			}
			);
	}
	
	private void logout()
	{
		Server.logoutInBackground(
				SwinChatGlobals.SharedPreferences.getUser(this.getApplication()),
				SwinChatGlobals.SharedPreferences.getServerUri(
						this.getApplication()).toString(), null);
		
		SwinChatGlobals.SharedPreferences.setServerURI(null,
				this.getApplication());
		SwinChatGlobals.SharedPreferences.setUser(null, this.getApplication());
		SwinChatGlobals.SharedPreferences.setLastMessageId(null, getApplication());
		getApplication().deleteDatabase(SwinChatConstants.DB.DB_NAME);
	}

}
