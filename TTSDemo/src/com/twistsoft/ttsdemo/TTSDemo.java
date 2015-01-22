package com.twistsoft.ttsdemo;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;


public class TTSDemo extends Activity {
	
	private EditText messageText;
	private Button usButton, ukButton, userButton;
	
	private TextToSpeech tts;
	private static int TTS_DATA_CHECK = 1;
	private boolean isTTSInitialized = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);                                
        
        messageText = (EditText)findViewById(R.id.messageText);

        usButton = (Button) findViewById(R.id.usButton);
        ukButton = (Button) findViewById(R.id.ukButton);
        userButton = (Button) findViewById(R.id.userButton);
                
        usButton.setOnClickListener(new View.OnClickListener() {
		  public void onClick(View view) {			  			  			  			  
			  if(messageText.getText().toString().trim().length() == 0) { 
				  Toast.makeText(getApplicationContext(), "Please enter your message.", Toast.LENGTH_LONG).show();
				  return;
			  }			  
			  
			  speakUSLocale();
		  }
        });
        
        ukButton.setOnClickListener(new View.OnClickListener() {
		  public void onClick(View view) {			  			  			  			  
			  if(messageText.getText().toString().trim().length() == 0) { 
				  Toast.makeText(getApplicationContext(), "Please enter your message.", Toast.LENGTH_LONG).show();
				  return;
			  }
			  
			  speakUKLocale();
		  }
        });	
        
        
        userButton.setOnClickListener(new View.OnClickListener() {
		  public void onClick(View view) {			  			  			  			  
			  if(messageText.getText().toString().trim().length() == 0) { 
				  Toast.makeText(getApplicationContext(), "Please enter your message.", Toast.LENGTH_LONG).show();
				  return;
			  }
			  
			  speakUserLocale();
		  }
        });
        
        confirmTTSData();
    }
    
    private void confirmTTSData()  {
    	Intent intent = new Intent(Engine.ACTION_CHECK_TTS_DATA);
    	startActivityForResult(intent, TTS_DATA_CHECK);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == TTS_DATA_CHECK) {
    		if (resultCode == Engine.CHECK_VOICE_DATA_PASS) {
    			//Voice data exists		
    			initializeTTS();
    		}
    		else {
    			Intent installIntent = new Intent(Engine.ACTION_INSTALL_TTS_DATA);
    			startActivity(installIntent);
    		}
    	}
    }
    
    private void initializeTTS() {
    	tts = new TextToSpeech(this, new OnInitListener() {
    		public void onInit(int status) {
    			if (status == TextToSpeech.SUCCESS) {
    				isTTSInitialized = true;
    			}
    			else {
    				//Handle initialization error here
    				isTTSInitialized = false;
    			}
    		}
    	});
    }
    
    
    private void speakUSLocale() {
    	if(isTTSInitialized) {
    		if (tts.isLanguageAvailable(Locale.US) >= 0) 
    			tts.setLanguage(Locale.US);
    		
    		tts.setPitch(0.8f);
    		tts.setSpeechRate(1.1f);
    		
    		tts.speak(messageText.getText().toString(), TextToSpeech.QUEUE_ADD, null);
    	}
    }
    
    
    private void speakUKLocale() {
    	if(isTTSInitialized) {
    		if (tts.isLanguageAvailable(Locale.UK) >= 0) 
    			tts.setLanguage(Locale.UK);
    		
    		tts.setPitch(0.8f);
    		tts.setSpeechRate(1.1f);
    		
    		tts.speak(messageText.getText().toString(), TextToSpeech.QUEUE_ADD, null);
    	}
    }
    
   
    private void speakUserLocale() {
    	if(isTTSInitialized) {
    		//Determine User's Locale
    		Locale locale = this.getResources().getConfiguration().locale;
    		
    		if (tts.isLanguageAvailable(locale) >= 0) 
    			tts.setLanguage(locale);
    		
    		tts.setPitch(0.8f);
    		tts.setSpeechRate(1.1f);
    		
    		tts.speak(messageText.getText().toString(), TextToSpeech.QUEUE_ADD, null);
    	}
    }
    
    @Override
    public void onDestroy() {
    	if (tts != null) {
    		tts.stop();
    		tts.shutdown();
    	}
    	super.onDestroy();
    }
}
