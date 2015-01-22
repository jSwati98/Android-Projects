
package com.android.timebuddy;


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

public class TimeManagementActivity extends Activity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private RemindersDbAdapter mDbHelper;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timemanagement);
       
        Button list = (Button) findViewById(R.id.list);
        Button talk = (Button) findViewById(R.id.talk);
        Button chat = (Button) findViewById(R.id.chat);
        Button share = (Button) findViewById(R.id.share);
        Button challenge = (Button) findViewById(R.id.challenge);
        
        
        list.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			/*Intent myIntent = new Intent(StartActivity.this, PriorityActivity.class);
    			StartActivity.this.startActivity(myIntent);*/
    			setContentView(R.layout.list);
    			
    			
    			
    		}
    	});
        
        talk.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			/*Intent myIntent = new Intent(StartActivity.this, PriorityActivity.class);
    			StartActivity.this.startActivity(myIntent);*/
    			setContentView(R.layout.talktoexpert);
    			
    			
    		}
    	});
        
        challenge.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			/*Intent myIntent = new Intent(StartActivity.this, PriorityActivity.class);
    			StartActivity.this.startActivity(myIntent);*/
    			setContentView(R.layout.challengeme);
    			
    			
    		}
    	});
        
        chat.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			Intent myIntent = new Intent(TimeManagementActivity.this, ChatActivity.class);
    			TimeManagementActivity.this.startActivity(myIntent);
    			//setContentView(R.layout.chat);
    			
    			
    		}
    	});
        
       

    		 share.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in button click!!!");
    			Intent myIntent = new Intent(TimeManagementActivity.this, Main.class);
    			myIntent.putExtra("from", "manage");
    			TimeManagementActivity.this.startActivity(myIntent);
    		}
    	});
    			
    			
    		}
    	
    
    
   
/*
	private void fillData() {
        Cursor remindersCursor = mDbHelper.fetchAllReminders();
        startManagingCursor(remindersCursor);
        
        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{RemindersDbAdapter.KEY_TITLE};
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter reminders = 
        	    new SimpleCursorAdapter(this, R.layout.reminder_row, remindersCursor, from, to);
        System.out.println("reminders--->"+reminders);
        setListAdapter(reminders);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.list_menu, menu); 
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_insert: 
            createReminder();
            return true; 
        case R.id.menu_settings: 
        	Intent i = new Intent(this, TaskPreferences.class); 
        	startActivity(i); 
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = getMenuInflater(); 
		mi.inflate(R.menu.list_menu_item_longpress, menu); 
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case R.id.menu_delete:
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        mDbHelper.deleteReminder(info.id);
	        fillData();
	        return true;
		}
		return super.onContextItemSelected(item);
	}
	
    private void createReminder() {
        Intent i = new Intent(this, ReminderEditActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ReminderEditActivity.class);
        System.out.println("on list click------->");
        i.putExtra(RemindersDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT); 
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }*/
}