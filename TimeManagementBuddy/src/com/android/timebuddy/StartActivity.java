
package com.android.timebuddy;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.googlecode.chartdroid.core.IntentConstants;
import com.googlecode.chartdroid.example.minimal.Market;
import com.googlecode.chartdroid.example.minimal.Data;
import com.googlecode.chartdroid.example.minimal.provider.DataContentProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class StartActivity extends Activity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    protected static final int REQUEST_OK = 1;
    TextToSpeech startTObj;
    HashMap<String, String> map = new HashMap<String, String>();
    static final String TAG = Market.TAG;

    final int DIALOG_CHARTDROID_DOWNLOAD = 1;
    
    
    private RemindersDbAdapter mDbHelper;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        Button create = (Button) findViewById(R.id.createTaskButton);
        Button show = (Button) findViewById(R.id.showTaskButton);
        Button prioratize = (Button) findViewById(R.id.prioritizeButton);
        Button suggest = (Button) findViewById(R.id.suggestionButton);
        Button chat = (Button) findViewById(R.id.timeButton);
        Button tracker = (Button) findViewById(R.id.trackerButton);
        
        Button voicecontrols = (Button) findViewById(R.id.voicecontrols);
        voicecontrols.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {   
       startTObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status != TextToSpeech.ERROR) {
					
					System.out.println("In object creation");
					startTObj.setLanguage(Locale.UK);
					map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
							"UniqueID");
					startTObj.speak("To create an event say Calendar . To see a list of reminders say tasks, to prioratize tasks say prioratize, to see suggestions say suggestion, to track tasks say tracker", TextToSpeech.QUEUE_FLUSH, map);
					startTObj.setOnUtteranceProgressListener(new UtteranceProgressListener() {

						@Override
						public void onDone(String utteranceId) {
							// TODO Auto-generated method stub
							Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
							i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
							try {
								startActivityForResult(i, REQUEST_OK);
								System.out.println("777777777");
							} catch (Exception e) {
								System.out.println("In here");
							}
							

						}

						@Override
						public void onError(String arg0) {
							// TODO Auto-generated method stub
							System.out.println("errror-->");
						}

						@Override
						public void onStart(String arg0) {
							System.out.println("start-->");
							// TODO Auto-generated method stub

						}
					});
				}
			}
		});
	
        
    		}
    		});
    	
    	
        create.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in button click!!!");
    			Intent myIntent = new Intent(StartActivity.this, Main.class);
    			myIntent.putExtra("from", "cal");
    			StartActivity.this.startActivity(myIntent);
    		}
    	});
        
        show.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			Intent myIntent = new Intent(StartActivity.this, ReminderListActivity.class);
    			StartActivity.this.startActivity(myIntent);
    		}
    	});
        
        suggest.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			Intent myIntent = new Intent(StartActivity.this, SuggestActivity.class);
    			StartActivity.this.startActivity(myIntent);
    		}
    	});
       
        prioratize.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			Intent myIntent = new Intent(StartActivity.this, PriorityActivity.class);
    			StartActivity.this.startActivity(myIntent);
    			
    		}
    	});
        
        chat.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
    			System.out.println("in show button click!!!");
    			Intent myIntent = new Intent(StartActivity.this, TimeManagementActivity.class);
    			StartActivity.this.startActivity(myIntent);
    			
    		}
    	});

       //Tracking graph 
        
       
        tracker.setOnClickListener(new View.OnClickListener() {

    		@Override
    		public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, DataContentProvider.PROVIDER_URI);
                i.putExtra(Intent.EXTRA_TITLE, Data.DEMO_CHART_TITLE);
                            i.putExtra(IntentConstants.Meta.Axes.EXTRA_FORMAT_STRING_Y, "");

                            if (Market.isIntentAvailable(getApplicationContext(), i)) {
                                    startActivity(i);
                            } else {
                                    showDialog(DIALOG_CHARTDROID_DOWNLOAD);
                            }

                          //  break;
                    }
    	});
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {

            Log.d(TAG, "Called onCreateDialog()");
            
            switch (id) {
            case DIALOG_CHARTDROID_DOWNLOAD:
                    return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Download ChartDroid")
                    .setMessage("You need to download ChartDroid to display this data.")
                    .setPositiveButton("Market download", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                    startActivity(Market.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));
                            }
                    })
                    .setNeutralButton("Web download", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Market.APK_DOWNLOAD_URI_CHARTDROID));
                            }
                    })
                    .create();
            }
            return null;
    }
    
    // ========================================================================
    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
            super.onPrepareDialog(id, dialog);
            
            Log.d(TAG, "Called onPrepareDialog()");
    
            switch (id) {
            case DIALOG_CHARTDROID_DOWNLOAD:
            {
                    boolean has_android_market = Market.isIntentAvailable(this,
                                    Market.getMarketDownloadIntent(Market.CHARTDROID_PACKAGE_NAME));

                    Log.d(TAG, "has android market? " + has_android_market);
                    
                    dialog.findViewById(android.R.id.button1).setVisibility(
                                    has_android_market ? View.VISIBLE : View.GONE);
                    break;
            }
            default:
                    break;
            }
    }
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
			
			ArrayList<String> thingsYouSaid = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			System.out.println("thingsYouSaid ----->"+thingsYouSaid.toString());
			String spokenData = thingsYouSaid.get(0);
			System.out.println("You said-->" + spokenData);

			
				System.out.println("data-->" + spokenData);
				
					if(spokenData.equalsIgnoreCase("Calendar")){
						Intent myIntent = new Intent(StartActivity.this, Main.class);
		    			StartActivity.this.startActivity(myIntent);
					
					}
					if(spokenData.equalsIgnoreCase("Tasks")){
						Intent myIntent = new Intent(StartActivity.this, ReminderListActivity.class);
		    			StartActivity.this.startActivity(myIntent);
					
					}
					if(spokenData.equalsIgnoreCase("Prioratize")){
						Intent myIntent = new Intent(StartActivity.this, PriorityActivity.class);
		    			StartActivity.this.startActivity(myIntent);
					
					}
					if(spokenData.equalsIgnoreCase("Suggestion")){
						Intent myIntent = new Intent(StartActivity.this, SuggestActivity.class);
		    			StartActivity.this.startActivity(myIntent);
					
					}
					if(spokenData.equalsIgnoreCase("Tracker")){
						
		                Intent i = new Intent(Intent.ACTION_VIEW, DataContentProvider.PROVIDER_URI);
		                i.putExtra(Intent.EXTRA_TITLE, Data.DEMO_CHART_TITLE);
		                            i.putExtra(IntentConstants.Meta.Axes.EXTRA_FORMAT_STRING_Y, "%.1f�C");

		                            if (Market.isIntentAvailable(getApplicationContext(), i)) {
		                                    startActivity(i);
		                            } else {
		                                    showDialog(DIALOG_CHARTDROID_DOWNLOAD);
		                            }

		                          
		                    
					}
					
			}
    }
			
 
  

}
