package swin.chat.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

public class MessageDBHelper extends SQLiteOpenHelper{
	
	public static final String TB_NAME = "message";
	public static final String MESSAGEID = SwinChatConstants.ServerContracts.GetMessages.R_MESSAGEID;
	public static final String FROMUSER = SwinChatConstants.ServerContracts.GetMessages.R_FROMUSER;
	public static final String TOUSER = SwinChatConstants.ServerContracts.GetMessages.R_TOUSER;
	public static final String DATETIME = SwinChatConstants.ServerContracts.GetMessages.R_DATETIME;
	public static final String MESSAGE = SwinChatConstants.ServerContracts.GetMessages.R_MESSAGE;
	
	
	public MessageDBHelper(Context context) {
		super(context, SwinChatConstants.DB.DB_NAME, null, SwinChatConstants.DB.VERSION);
		
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String createStmt = "CREATE TABLE "+TB_NAME +"(" + 
							"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
							MESSAGEID + " INTEGER,"+ 
							TOUSER + " TEXT," +
							FROMUSER + " TEXT," +
							DATETIME + " TEXT,"+
							MESSAGE + " TEXT);";
		
		db.execSQL(createStmt);
		
		createStmt = "CREATE INDEX INDEX_"+TB_NAME +" ON " + TB_NAME +" (" +  
		FROMUSER + "," + TOUSER + "," + DATETIME + ");";
		
		db.execSQL(createStmt);
	}
	
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TB_NAME );
		db.execSQL("DROP INDEX IF EXISTS INDEX_" + TB_NAME);
		onCreate(db);
	}
	
	
	
	
}
