package swin.chat.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import swin.chat.MainActivity;
import swin.chat.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

/**
 * Use for storing global variables. The availability is not guaranted. Used for performance.
 * 
 * @author firmanazis
 *
 */
public class SwinChatGlobals {
	
	
	public static class SharedPreferences
	{
		private static android.content.SharedPreferences sharedPreferences = null;
		
		private static URI serverUri = null;
		private static String user = null;
		
//		private static Boolean isRegisteringC2DM = null;
		private static Date lastMessageDateTime = null;
		
		
		public static URI getServerUri(Context context)
		{
			if(serverUri == null)
			{
				//load shared Preference if not available
				android.content.SharedPreferences sp = getSharedPreferences(context);
				String uriStr = sp.getString(SwinChatConstants.SharedPreferences.SERVER, null);
				
				if(uriStr != null)
				{	try {
						serverUri = new URI(uriStr);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
			return serverUri;
		}
		
		public static void setServerURI(URI uri, Context context)
		{
			android.content.SharedPreferences sp = getSharedPreferences(context);
			android.content.SharedPreferences.Editor editor = sp.edit();
			if(uri != null)
			{

				editor.putString(SwinChatConstants.SharedPreferences.SERVER, uri.toString());
			}
			else
			{
				editor.remove(SwinChatConstants.SharedPreferences.SERVER);
			}
			editor.commit();
			serverUri = uri;
		} 
		
		public static String getUser(Context context) {
			if(user == null)
			{
				android.content.SharedPreferences sp = getSharedPreferences(context);
				user = sp.getString(SwinChatConstants.SharedPreferences.USER, null);
			}
			return user;
		}
		
		public static void setUser(String user, Context context) {
			android.content.SharedPreferences sp = getSharedPreferences(context);
			android.content.SharedPreferences.Editor editor = sp.edit();
			if(user != null)
			{
				editor.putString(SwinChatConstants.SharedPreferences.USER, user);
			}
			else
			{
				editor.remove(SwinChatConstants.SharedPreferences.USER);
			}
			editor.commit();
			SharedPreferences.user = user;
		}
		
//		public static boolean isRegisteringC2DM(Context context) {
//			if(isRegisteringC2DM == null)
//			{
//				android.content.SharedPreferences sp = getSharedPreferences(context);
//				isRegisteringC2DM = sp.getBoolean(SwinChatConstants.SharedPreferences.IS_REGISTERING_C2DM, false);
//			}
//			return isRegisteringC2DM;
//		}
//		
//		public static void setIsRegisteringC2DM(boolean isRegisteringC2DM, Context context) {
//			android.content.SharedPreferences sp = getSharedPreferences(context);
//			android.content.SharedPreferences.Editor editor = sp.edit();
//
//			editor.putBoolean(SwinChatConstants.SharedPreferences.IS_REGISTERING_C2DM, isRegisteringC2DM);
//			editor.commit();
//			SharedPreferences.isRegisteringC2DM = isRegisteringC2DM;
//		}
		
		public static void setLastMessageId(Date dateTime, Context context)
		{
			android.content.SharedPreferences sp = getSharedPreferences(context);
			android.content.SharedPreferences.Editor editor = sp.edit();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			if(dateTime == null)
			{
				editor.remove(SwinChatConstants.SharedPreferences.LAST_MESSAGE_DATETIME);
			}
			else
			{
				editor.putString(SwinChatConstants.SharedPreferences.LAST_MESSAGE_DATETIME, sdf.format(dateTime));
			}
			editor.commit();
			lastMessageDateTime = dateTime;
		}
		
		public static Date getLastMessageDateTime(Context context)
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(lastMessageDateTime == null)
			{
				android.content.SharedPreferences sp = getSharedPreferences(context);
				String lastMessageDateTimeStr = sp.getString(SwinChatConstants.SharedPreferences.LAST_MESSAGE_DATETIME,null);
				if(lastMessageDateTimeStr != null)
				{
					try {
						lastMessageDateTime = sdf.parse(lastMessageDateTimeStr);
					} catch (ParseException e) {
						throw new IllegalStateException(e);
					}
				}else
				{
					setLastMessageId(new Date(), context);
				}
				
			}
			return lastMessageDateTime;
		}
		
		
		public static android.content.SharedPreferences getSharedPreferences(Context context)
		{
			if(sharedPreferences == null)
			{
				sharedPreferences = context.getSharedPreferences(SwinChatConstants.SharedPreferences.FILE_NAME,
						SwinChatConstants.SharedPreferences.MODE);
				
			}
			return sharedPreferences;
		}
	}
	
	public static class ActivityUtil
	{
		static Activity currentActivity = null;
		final static Object staticLock = new Object();
		
		
		public static Activity getCurrentActivity()
		{
			synchronized (staticLock) {
				return currentActivity;
			}
		}
		
		public static void setCurrentActivity(Activity currentActivity)
		{
			synchronized (staticLock) {
				ActivityUtil.currentActivity = currentActivity;
			}
		}
		
		public static void showAlertOrNotify(Activity context, String title, String message)
		{
			boolean alertDialog = true;
			synchronized (staticLock) {
				if(currentActivity == null)
				{
					alertDialog = false;
				}
			}
			if (alertDialog) {

				(new AlertDialog.Builder(context))
						.setTitle(title)
						.setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).create().show();
			} else {
				NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
				Notification n = new Notification(R.drawable.icon,
						message, System.currentTimeMillis());
				n.contentIntent = PendingIntent.getActivity(
						context, 0, new Intent(
								context,
								context.getClass()), 0);
				nm.notify(1, n);
			}
		}
		
	}
	
	public static class DB
	{
		private static  MessageDBHelper messageDBHelper;
		
		public static MessageDBHelper getMessagedbhelper(Context context) {
			if(messageDBHelper == null)
			{
				messageDBHelper = new MessageDBHelper(context);
			}
			
			return messageDBHelper;
		}
		
	}
	
//	public static final HttpClient httpClient;
//	static
//	{
//        // Create and initialize HTTP parameters
//        HttpParams params = new BasicHttpParams();
//        ConnManagerParams.setMaxTotalConnections(params, 100);
//        ConnManagerParams.setTimeout(params, 3000);
//        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//        
//        // Create and initialize scheme registry 
//        SchemeRegistry schemeRegistry = new SchemeRegistry();
//        schemeRegistry.register(
//                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//        
//        // Create an HttpClient with the ThreadSafeClientConnManager.
//        // This connection manager must be used if more than one thread will
//        // be using the HttpClient.
//        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
//        httpClient = new DefaultHttpClient(cm, params);
//	}
	
}
