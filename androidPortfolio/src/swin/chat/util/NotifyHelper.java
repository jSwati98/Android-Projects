package swin.chat.util;

import android.os.Handler;
import android.os.Message;

public class NotifyHelper {
	
	public Handler handler;
	public int messageWhat;
	public Delegate<Message,Boolean> isHandlingMessage;
	
	public NotifyHelper(Handler handler, int messageWhat, Delegate<Message, Boolean> isHandlingMessage)
	{
		this.handler = handler;
		this.messageWhat = messageWhat;
		this.isHandlingMessage = isHandlingMessage;
	}
	
	
	
	
}
