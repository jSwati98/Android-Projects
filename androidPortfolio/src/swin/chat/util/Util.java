package swin.chat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Message;

public class Util {
	
	public static String readStringFromInputStream(InputStream is) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is),1024);
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[1024];
		for(int read = br.read(buffer);read != -1; read = br.read(buffer))
		{			
			builder.append(buffer,0, read);
		}
		return builder.toString();
	}
	
}
