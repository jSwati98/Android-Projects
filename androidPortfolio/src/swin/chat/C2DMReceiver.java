package swin.chat;

import swin.chat.util.Delegate;
import swin.chat.util.NotifyHelper;
import swin.chat.util.SwinChatConstants;
import swin.chat.util.TicketLinkedList;
import swin.chat.util.TicketLinkedList.Ticket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.android.c2dm.C2DMBaseReceiver;

public class C2DMReceiver extends C2DMBaseReceiver {
	
	public final static String BROADCAST_ACTION_MESSAGE = "swin.chat.C2DMReceiver.BROADCAST_ACTION_MESSAGE";
	public final static String BROADCAST_ACTION_ERROR = "swin.chat.C2DMReceiver.BROADCAST_ACTION_ERROR";
	public final static String BROADCAST_ACTION_REGISTERED = "swin.chat.C2DMReceiver.BROADCAST_ACTION_REGISTERED";
	public final static String BROADCAST_ACTION_UNREGISTERED = "swin.chat.C2DMReceiver.BROADCAST_CATEGORY_REGISTERED";
	
	public final static String EXTRA_ERROR_MESSAGE = "swin.chat.C2DMReceiver.EXTRA_ERROR_MESSAGE";
	public final static String EXTRA_REGISTRATION_ID = "swin.chat.C2DMReceiver.EXTRA_REGISTERED_ID";
	
	public final static IntentFilter RESPONSE_INTENT_FILTER;
	
	static
	{
		RESPONSE_INTENT_FILTER = new IntentFilter();
		RESPONSE_INTENT_FILTER.addAction(BROADCAST_ACTION_MESSAGE);
		RESPONSE_INTENT_FILTER.addAction(BROADCAST_ACTION_ERROR);
		RESPONSE_INTENT_FILTER.addAction(BROADCAST_ACTION_REGISTERED);
		RESPONSE_INTENT_FILTER.addAction(BROADCAST_ACTION_UNREGISTERED);
		RESPONSE_INTENT_FILTER.setPriority(1);
	}
	
	public static final int MESSAGE_ARG1_MESSAGE = 1;
	public static final int MESSAGE_ARG1_ERROR = 2;
	public static final int MESSAGE_ARG1_REGISTERED = 3;
	public static final int MESSAGE_ARG1_UNREGISTERED = 4;
	
	private final TicketLinkedList<NotifyHelper> notifyList = new TicketLinkedList<NotifyHelper>();
	
	public class C2DMReceiverBinder extends Binder
	{
		
		public TicketLinkedList.Ticket<NotifyHelper> addHandler(Handler handler, int what, Delegate<Message, Boolean> isHandlingMessage)
		{
			synchronized (this) {
				return notifyList.add(new NotifyHelper(handler, what, isHandlingMessage));
			}
		}
		
		public void removeHandler(Ticket<NotifyHelper> ticket)
		{
			synchronized (this) {
				ticket.remove();
			}
		}
		
		public C2DMReceiver getService()
		{
			return C2DMReceiver.this;
		}
	}
	
	private final C2DMReceiverBinder mBinder = new C2DMReceiverBinder();
	
	@Override
		public IBinder onBind(Intent intent) {
			return mBinder;
		}
	
	public C2DMReceiver() {
		super(SwinChatConstants.C2DM_SENDER_ID);
	}
	
	

	@Override
	protected void onMessage(Context context, Intent intent) {
		synchronized (this) {
			for(TicketLinkedList.Ticket<NotifyHelper> ticket : notifyList)
			{
				Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
				m.arg1 = MESSAGE_ARG1_MESSAGE;
				m.setData(intent.getExtras());
				ticket.getElement().handler.sendMessage(m);
			}
		}
		
		Intent msgIntent = new Intent(BROADCAST_ACTION_MESSAGE);
		msgIntent.putExtras(intent.getExtras());
		msgIntent.setPackage(context.getPackageName());
		sendOrderedBroadcast(msgIntent, Manifest.permission.LOCAL_PERMISSION);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.e("swin.chat", "C2DM registration Id error " + errorId);
		
		String errorMessage = null;
		
		if(errorId.equals(ERR_ACCOUNT_MISSING))
			errorMessage = "You don't have Google account on your phone. This is required for push.";
		else if(errorId.equals(ERR_AUTHENTICATION_FAILED))
			errorMessage =  "Authentication with Google failed";
		else if(errorId.equals(ERR_TOO_MANY_REGISTRATIONS))
			errorMessage = "Too many registrations";
		else
			errorMessage = errorId;
		
		Bundle data = new Bundle();
		data.putString(EXTRA_ERROR_MESSAGE, errorMessage);
		
		synchronized (this) {
			for(TicketLinkedList.Ticket<NotifyHelper> ticket : notifyList)
			{
				Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
				m.arg1 = MESSAGE_ARG1_ERROR;
				m.setData(data);
				ticket.getElement().handler.sendMessage(m);
			}
		}
		
		Intent errorIntent = new Intent(BROADCAST_ACTION_ERROR);
		errorIntent.putExtras(data);
		errorIntent.setPackage(context.getPackageName());
		sendOrderedBroadcast(errorIntent, Manifest.permission.LOCAL_PERMISSION);
	}
	
	@Override
	public void onRegistered(Context context, String registrationId)
	{
		Bundle data = new Bundle();
		data.putString(EXTRA_REGISTRATION_ID, registrationId);
		
		synchronized (this) {
			for(TicketLinkedList.Ticket<NotifyHelper> ticket : notifyList)
			{
				Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
				m.arg1 = MESSAGE_ARG1_REGISTERED;
				m.setData(data);
				ticket.getElement().handler.sendMessage(m);
			}
		}
		
		
		Intent intent = new Intent(BROADCAST_ACTION_REGISTERED);
		intent.putExtras(data);
		intent.setPackage(context.getPackageName());
		sendOrderedBroadcast(intent, Manifest.permission.LOCAL_PERMISSION);
	}
	
	@Override
	public void onUnregistered(Context context) {
		synchronized (this) {
			for(TicketLinkedList.Ticket<NotifyHelper> ticket : notifyList)
			{
				Message m = ticket.getElement().handler.obtainMessage(ticket.getElement().messageWhat);
				m.arg1 = MESSAGE_ARG1_UNREGISTERED;
				ticket.getElement().handler.sendMessage(m);
			}
		}
		
		Intent intent = new Intent(BROADCAST_ACTION_UNREGISTERED);
		intent.setPackage(context.getPackageName());
		sendOrderedBroadcast(intent, Manifest.permission.LOCAL_PERMISSION);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
