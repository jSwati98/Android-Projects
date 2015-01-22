package com.android.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import com.authorwjf.talk2me.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final int REQUEST_OK = 1;
	ArrayList<String> a = new ArrayList<String>();
	ArrayList<String> b = new ArrayList<String>();
	ArrayList bst;
	String instrText;
	static BST bstObj = new BST();
	static MainActivity activityM;
	TextToSpeech startTObj;
	TextToSpeech sayTObj;
	boolean startFlag = false;
	HashMap<String, String> map = new HashMap<String, String>();
	com.android.app.BST.Node current;
	public static MainActivity getInstance() {
		return activityM;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		activityM = this;
		//setContentView(R.layout.activity_main);
		setContentView(R.layout.activity_main);
		
		
	}

	
}

