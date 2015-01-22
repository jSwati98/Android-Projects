
package com.android.timebuddy;


import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

public class ChatActivity extends Activity {
    
    private RemindersDbAdapter mDbHelper;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
       
        Button chat = (Button) findViewById(R.id.chat_button);
     
        chat.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			
    			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    			final List pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities( mainIntent, 0);
    			
    			/* Create the Intent */
        		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        		System.out.println("in emaillllllllllll-------------------->");
        		/* Fill it with Data */
        		emailIntent.setType("plain/text");
        		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
        		System.out.println("in emaillllllllllll-------------------->1111");
        		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject");
        		System.out.println("in emaillllllllllll-------------------->2222");
        		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Text");
        		System.out.println("in emaillllllllllll-------------------->333");
        		/* Send it off to the Activity-Chooser */
        		ChatActivity.this.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    			
    		}
    	});
        
    }
}
