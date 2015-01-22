package swin.chat.util;

import java.text.SimpleDateFormat;

import android.content.Context;

public class SwinChatConstants {
	
	public static class ServerContracts
	{
		public static class Handshake
		{
			public static final String REL_URI = "./handshake.php";
		}
		
		public static class Login
		{
			public static final String REL_URI = "./login.php";
			public static final String USER = "user";
			public static final String REGID = "regId";
			
			public static final String R_STATUS = "status";
		}
		
		public static class Logout
		{
			public static final String REL_URI = "./logout.php";
			public static final String USER = "user";
			
			public static final String R_STATUS = "status";
		}
		
		public static class ListUsers
		{
			public static final String REL_URI = "./listUsers.php";
			
			public final static String USER = "user";
			
			public final static String R_USERS = "users";
		}
		
		public static class SendMessage
		{
			public static final String REL_URI = "./sendMessage.php";
			
			public final static String USER = "user";
			public final static String TOUSER = "toUser";
			public final static String MESSAGE = "message";
			
			public final static String R_STATUS = "status";

			public static final String R_INSERTID = "insertId";			
		}
		
		public static class GetMessages
		{
			public static final String REL_URI = "./getMessages.php";
			
			public final static String USER = "user";
			public final static String LASTDATETIME = "lastDateTime";
			
			public final static String R_TOUSER = "toUser";
			public final static String R_FROMUSER = "fromUser";
			public final static String R_MESSAGEID = "messageId";
			public final static String R_DATETIME = "dateTime";
			public final static String R_MESSAGE = "message";

			public static final String R_MESSAGES = "messages";
			
		}
		
		public static class C2DM
		{
			public final static String COLLAPSE_KEY = "collapse_key";
			
			public static class CollapseKeys
			{
				public final static String LIST_USERS = "list_users";
				public final static String NEW_MESSAGE = "new_message";
			}
		}
	}
	
	public static class SharedPreferences
	{
		public static final String FILE_NAME = "swin.chat";
		public static final int MODE = Context.MODE_PRIVATE;
		public static final String VERSION = "VERSION";
		public static final int CURRENT_VERSION = 1;
		
		public static final String SERVER = "SERVER";
		public static final String USER = "USER";
		
		public static final String IS_REGISTERING_C2DM = "IS_REGISTERING_C2DM";
		public static final String LAST_MESSAGE_DATETIME = "LAST_MESSAGE_DATETIME";
	}
	
	public static class Notification
	{
		public static final int DEFAULT_ID = 0;
	}
	
	public static class DB
	{
		public static final String DB_NAME = "swinchat";
		public static final int VERSION = 1;
		
	}
	
	public static final String C2DM_SENDER_ID = "firmanazis@gmail.com";
	
	public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
	
}
