package swin.chat.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import swin.chat.C2DMReceiver;
import swin.chat.model.ChatMessage;
import swin.chat.model.User;
import swin.chat.util.CallBackAsyncTask.CallBack;
import swin.chat.util.SwinChatGlobals.SharedPreferences;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.c2dm.C2DMessaging;

public class Server {
	
	
	public static JSONObject executeRequestReturnJSON(HttpUriRequest request) throws JSONException, IOException, HttpException
	{
		HttpResponse response = null;
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams param = new BasicHttpParams();
		ConnManagerParams.setTimeout(param, 30000);
		client.setParams(param);
		response = client.execute(request);
		
		
		StatusLine status = response.getStatusLine();
		if(status.getStatusCode() == HttpStatus.SC_OK)
		{
			try
			{
				String responseContent = Util.readStringFromInputStream(response.getEntity().getContent());
				Log.i("swin.chat", "Server.executeRequestReturnJSON " + responseContent);
				return new JSONObject(responseContent);
			}
			finally
			{
				response.getEntity().consumeContent();
			}
		}
		
		throw new HttpException("HTTP status " + status.getStatusCode() + " " + status.getReasonPhrase());
	}
	
	public static List<User> getOtherUsers(String userName, String serverURI) throws JSONException, IOException, HttpException
	{
		HttpPost post = new HttpPost(serverURI + "/" + SwinChatConstants.ServerContracts.ListUsers.REL_URI);
		
		JSONObject requestJSON = new JSONObject();
		requestJSON.put(SwinChatConstants.ServerContracts.ListUsers.USER,
				userName);
		
		post.setEntity(new StringEntity(requestJSON.toString()));
		
		JSONObject responseJSON = Server.executeRequestReturnJSON(post);

		JSONArray userJSONArray = responseJSON.getJSONArray(SwinChatConstants.ServerContracts.ListUsers.R_USERS);
		
		List<User> users = new ArrayList<User>(userJSONArray.length());
		
		for(int i=0; i < userJSONArray.length(); i++)
		{
			users.add((new User()).setName(userJSONArray.getString(i)));
		}
		
		return users;
	}
	
	public static CallBackAsyncTask<Void,Void,List<User>> getOtherUsersInBackground(
			final String userName,
			final String serverURI,
			final CallBackAsyncTask.CallBack<Void, Void, List<User>> callback)
	{
		CallBackAsyncTask<Void,Void,List<User>> backgroundTask = new CallBackAsyncTask<Void, Void, List<User>>(
				callback,
				new CallBackAsyncTask.BackgroundCallBack<Void, Void, List<User>>() {

					@Override
					public List<User> doInBackground(
							AsyncTask<Void, Void, List<User>> asyncTask,
							Void[] params) 
							throws Exception {
						return getOtherUsers(userName, serverURI);
						
					}
					
				}
				);
		
		backgroundTask.execute(null);
		return backgroundTask;
	}
	
	public static  boolean login(String userName, String serverURI, String regId) throws IOException, JSONException, Exception
	{
		if(userName == null || serverURI == null || regId == null)
			throw new NullPointerException();
		
		HttpPost httpPost = new HttpPost(serverURI + "/" + SwinChatConstants.ServerContracts.Login.REL_URI);
		JSONObject param = new JSONObject();
		param.put(SwinChatConstants.ServerContracts.Login.USER, userName);
		param.put(SwinChatConstants.ServerContracts.Login.REGID, regId);
		httpPost.setEntity(new StringEntity(param.toString()));
			
		JSONObject responseObject = executeRequestReturnJSON(httpPost);
		if(!responseObject.getBoolean(SwinChatConstants.ServerContracts.Login.R_STATUS))
		{
			throw new Exception("remote server return false");
		}
		
		return true;
		
	}
	
	public static CallBackAsyncTask<Void, Void, Boolean> loginInBackground(final String userName, 
			final String serverURI,
			final String regId,
			final CallBackAsyncTask.CallBack<Void, Void, Boolean> callback)
	{
		CallBackAsyncTask<Void, Void, Boolean> task = new CallBackAsyncTask<Void, Void, Boolean>(
				callback,
				new CallBackAsyncTask.BackgroundCallBack<Void, Void, Boolean>() {
					@Override
					public Boolean doInBackground(
							AsyncTask<Void, Void, Boolean> asyncTask,
							Void[] params) throws Exception {
						return login(userName,serverURI, regId);
					}
				}
		);
		
		task.execute(null);
		return task;
	}
	
	public static boolean handshake(String serverURI) throws ClientProtocolException, IOException, HttpException, Exception
	{
		//handshaking
		HttpPost httpPost = new HttpPost(serverURI + "/" +
				SwinChatConstants.ServerContracts.Handshake.REL_URI);
		Log.i("swin.chat", "Handshaking with " + httpPost.getURI().toString());
		
		BasicHttpEntity handshake = new BasicHttpEntity();
		
		byte[] challenge = new byte[16]; // 128 bits
		ByteBuffer challengeBuffer = ByteBuffer.wrap(challenge);
		
		for(int i = 0; i < 2; i++) //double needs 2 iteration to produce 128 bits
		{
			challengeBuffer.putDouble(Math.random());
		}
		
		BasicHttpEntity challengeEntity = new BasicHttpEntity();
		challengeEntity.setContent(new ByteArrayInputStream(challenge));
		challengeEntity.setContentLength(challenge.length);
		httpPost.setEntity(challengeEntity);
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpParams param = new BasicHttpParams();
		ConnManagerParams.setTimeout(param, 30000);
		client.setParams(param);
		HttpResponse response =  client.execute(httpPost);
		StatusLine status = response.getStatusLine();
		try
		{
				
			if(status.getStatusCode() != HttpStatus.SC_OK)
			{
				response.getEntity().consumeContent();
				throw new HttpException("HTTP status " + status.getStatusCode() + " " + status.getReasonPhrase());
			}
			
			byte[] challengeResponse = new byte[16];

			InputStream is = response.getEntity().getContent();
			is.read(challengeResponse);
			is.close();
		
			if(response.getEntity().getContentLength() != 16)
			{
				throw new Exception("challenge response is invalid");
			}
			
			//no security :P
			if(!Arrays.equals(challenge, challengeResponse))
			{
				throw new Exception("challenge response is invalid");
			}
		}
		finally
		{
			response.getEntity().consumeContent();
		}
		return true;
	}
	
	public static CallBackAsyncTask<Void, Void, Boolean> handshakeInBackground(final String serverURI,
			CallBackAsyncTask.CallBack<Void, Void, Boolean> callback)
	{
		CallBackAsyncTask<Void, Void, Boolean> backgroundTask = 
			new CallBackAsyncTask<Void, Void, Boolean>(callback,
			new CallBackAsyncTask.BackgroundCallBack<Void, Void, Boolean>() {
				@Override
				public Boolean doInBackground(
						AsyncTask<Void, Void, Boolean> asyncTask, Void[] params)
						throws Exception {
					return handshake(serverURI);
				}
			}
			);
		backgroundTask.execute(null);
		return backgroundTask;
	}
	
	public static  boolean logout(String oldUser, String oldServer) throws IOException, JSONException, Exception
	{
		
		Log.i("swin.chat", "log out user " + oldUser + " from " + oldServer);
		HttpPost httpPost = new HttpPost(URI.create(oldServer + "/" + SwinChatConstants.ServerContracts.Logout.REL_URI));
		JSONObject param = new JSONObject();
		param.put(SwinChatConstants.ServerContracts.Logout.USER, oldUser);
		httpPost.setEntity(new StringEntity(param.toString()));
		
		JSONObject responseObject = executeRequestReturnJSON(httpPost);
		
		if(!responseObject.getBoolean(SwinChatConstants.ServerContracts.Login.R_STATUS))
		{
			throw new Exception("remote server return false");
		}
		return true;
	}
	
	public static CallBackAsyncTask<Void, Void, Boolean> logoutInBackground( 
			final String userName, final String oldServer, final CallBackAsyncTask.CallBack<Void, Void, Boolean> callback)
	{
		
		CallBackAsyncTask<Void, Void, Boolean> task = new CallBackAsyncTask<Void, Void, Boolean>(
				callback,
				new CallBackAsyncTask.BackgroundCallBack<Void, Void, Boolean>() {
					@Override
					public Boolean doInBackground(
							AsyncTask<Void, Void, Boolean> asyncTask,
							Void[] params) throws Exception {
						Log.i("swin.chat", "log out user " + userName + " from " + oldServer + "in background");
						return logout(userName, oldServer);
					}
				}
		);
		
		task.execute(null);
		return task;
	}
	
	public static JSONObject sendMessage(String user, String regId, String toUser,String serverURI, String message) throws JSONException, IOException, HttpException, Exception
	{
		HttpPost httpPost = new HttpPost(serverURI + "/" + SwinChatConstants.ServerContracts.SendMessage.REL_URI);
		JSONObject param = new JSONObject();
		param.put(SwinChatConstants.ServerContracts.SendMessage.USER, user);
		param.put(SwinChatConstants.ServerContracts.SendMessage.TOUSER, toUser);
		param.put(SwinChatConstants.ServerContracts.SendMessage.MESSAGE, message);
		
		httpPost.setEntity(new StringEntity(param.toString()));
		Log.i("swin.chat", "send " + param.toString());
		
		JSONObject responseObject = executeRequestReturnJSON(httpPost);
		if(!responseObject.getBoolean(SwinChatConstants.ServerContracts.Login.R_STATUS))
		{
			throw new Exception("remote server return false");
		}
		return responseObject;
	}
	
	public static CallBackAsyncTask<Void, Void, JSONObject> sendMessageInBackground( 
			final String user, final String regId, final String toUser,final String serverURI, final String message, final CallBack<Void, Void, JSONObject> callback)
	{
		
		CallBackAsyncTask<Void, Void, JSONObject> task = new CallBackAsyncTask<Void, Void, JSONObject>(
				callback,
				new CallBackAsyncTask.BackgroundCallBack<Void,Void,JSONObject>() {
					@Override
					public JSONObject doInBackground(
							AsyncTask<Void, Void, JSONObject> asyncTask,
							Void[] params) throws Exception {
						return sendMessage(user, regId, toUser, serverURI, message);
					}
				}
		);
		
		task.execute(null);
		return task;
	}
	
	public static List<ChatMessage> getMessages(String user, Date lastDateTime, String serverURI) throws JSONException, IOException, HttpException,Exception
	{
		HttpPost httpPost = new HttpPost(serverURI + "/" + SwinChatConstants.ServerContracts.GetMessages.REL_URI);
		JSONObject param = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		param.put(SwinChatConstants.ServerContracts.GetMessages.USER, user);
		param.put(SwinChatConstants.ServerContracts.GetMessages.LASTDATETIME,sdf.format(lastDateTime) );
		httpPost.setEntity(new StringEntity(param.toString()));
		
		JSONObject responseObject = executeRequestReturnJSON(httpPost);
		
		List<ChatMessage>  messageList= new ArrayList<ChatMessage>(100);
		
		
		JSONArray messages = responseObject.getJSONArray(SwinChatConstants.ServerContracts.GetMessages.R_MESSAGES);
		
		for(int i= 0; i < messages.length(); i++)
		{
			JSONObject messageJSON = messages.getJSONObject(i);
			messageList.add(new ChatMessage()
							.setToUser(messageJSON.getString(SwinChatConstants.ServerContracts.GetMessages.R_TOUSER))
							.setFromUser(messageJSON.getString(SwinChatConstants.ServerContracts.GetMessages.R_FROMUSER))
							.setMessageId(messageJSON.getString(SwinChatConstants.ServerContracts.GetMessages.R_MESSAGEID))
							.setMessage(messageJSON.getString(SwinChatConstants.ServerContracts.GetMessages.R_MESSAGE))
							.setDateTime(sdf.parse(
									messageJSON.getString(
											SwinChatConstants.ServerContracts.GetMessages.R_DATETIME
										)
									)));
		}
		
		return messageList;
	}
	
	public static CallBackAsyncTask<Void,Void,List<ChatMessage>> 
	getMessagesInBackground(final String user, 
			final Date lastDateTime, 
			final String serverURI, 
			CallBackAsyncTask.CallBack<Void, Void, List<ChatMessage>> callback)
	{
		CallBackAsyncTask<Void, Void, List<ChatMessage>> task = new CallBackAsyncTask<Void, Void, List<ChatMessage>>(
				callback,
				new CallBackAsyncTask.BackgroundCallBack<Void, Void, List<ChatMessage>>() {
					@Override
					public List<ChatMessage> doInBackground(
							AsyncTask<Void, Void, List<ChatMessage>> asyncTask,
							Void[] params) throws Exception {
						return getMessages(user,lastDateTime, serverURI);
					}
				}
		);
		
		task.execute(null);
		return task;
	}
	
}
