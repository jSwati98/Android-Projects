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
import android.os.Handler;
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

public class TestActivity extends Activity {

	protected static final int REQUEST_OK = 1;
	ArrayList<String> a = new ArrayList<String>();
	ArrayList<String> b = new ArrayList<String>();
	ArrayList bst;
	static BST bstObj = new BST();
	static TestActivity activityM;
	TextView summary;
	TextToSpeech ttobj;
	TextToSpeech sayTObj;
	TextToSpeech validateTObj;
	boolean chooseOptions = false;
	Button startButton;
	Button callButton;
	Button Help ;
	 Button close;
	HashMap<String, String> map = new HashMap<String, String>();
	com.android.app.BST.Node current;
	public static TestActivity getInstance() {
		return activityM;
	}

	private String readRawTextFile(int id) {
		InputStream inputStream = getApplicationContext().getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in);
		String line;
		StringBuilder text = new StringBuilder();
		try {
			while (( line = buf.readLine()) != null) 
				text.append(line);
		} catch (IOException e) {
			return null;
		}
		return text.toString();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		activityM = this;
		setContentView(R.layout.test_results);
		summary = (TextView) findViewById (R.id.summary);
		
		startButton = (Button) findViewById(R.id.startButton);
		callButton = (Button) findViewById(R.id.callButton);
		Help = (Button) findViewById(R.id.helpButton);
		close = (Button) findViewById(R.id.closeButton);
		/*startButton.setEnabled(true);
		callButton.setEnabled(true);
		Help.setEnabled(true);
		close.setEnabled(true);*/
		startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("in button click!!!");
				try {
					Resources res = getResources();
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							res.openRawResource(R.raw.file)));
					try {
						a.clear();
						System.out.println("in----------------" + reader);
						String str, str1;
						while ((str = reader.readLine()) != null) {
							a.add(str);

						}

						System.out.println("a--->" + a);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					BST.getTree(a, getApplicationContext());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});

		
		callButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				System.out.println("in call--->");
				String posted_by = "111-333-222-4";

				String uri = "tel:" + posted_by.trim();
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse(uri));
				startActivity(callIntent);
			}

		});
		
		
		
		
        Help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				System.out.println("help cutton----------->");
				Help helpDialog = new Help(TestActivity.this);
				helpDialog.setTitle("Instructions");
				helpDialog.show();           		
			}        
        });
        
       
        close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) { 
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
		        finish();
			}
        });
	}

	public void startRecognition(com.android.app.BST.Node r) {

		Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
		chooseOptions = false;
		try {
			this.current = r;
			startActivityForResult(i, REQUEST_OK);
			System.out.println("777777777");
		} catch (Exception e) {
			Toast.makeText(this, "Error initializing speech to text engine.",
					Toast.LENGTH_LONG).show();
		}
		
		

	}
	String disease = "";
	public void getResult(String result){
		 
		System.out.println("Result ------->"+result);
		//setContentView(R.layout.test_results);
		//super.onCreate(savedInstanceState);
		TextView summary = (TextView) findViewById (R.id.summary);
		TextView prescription = (TextView) findViewById (R.id.prescription);
		
		summary.setText(result);
		
		if(result.equalsIgnoreCase("you might have food poisoning.")){
			disease = "food+poisoning";
			prescription.setText(Html.fromHtml(getString(R.string.food_poisoning_html)));
			 
		}
		else if(result.equalsIgnoreCase("You might be suffering from kidney infection.")){
			disease = "kidney+infection"; 
			prescription.setText(Html.fromHtml(getString(R.string.kidney_html)));
			
		}
		else if(result.equalsIgnoreCase("you might have renal colic caused by kidney disorder.")){
			disease = "renal+colic+infection"; 
			prescription.setText(Html.fromHtml(getString(R.string.renal_infection_html)));
		}
		else if(result.equalsIgnoreCase("you might be suffering from dysmenorrhea.")){
			disease = "dysmenorrhea";
			prescription.setText(Html.fromHtml(getString(R.string.dysmenorrhea_html)));
		}
		else if(result.equalsIgnoreCase("You might be suffering from intestine infection.")){
			disease = "intestine+infection";
			prescription.setText(Html.fromHtml(getString(R.string.intestine_html)));
		}
		else if(result.equalsIgnoreCase("You might be suffering from gall bladder infection.")){
			disease = "gall+bladder+infection";
			prescription.setText(Html.fromHtml(getString(R.string.gall_bladder_infection_html)));
		}
		else if(result.equalsIgnoreCase("You might have nerve infection")){
			disease = "gall+bladder+infection";
			prescription.setText(Html.fromHtml(getString(R.string.nerve_infection_html)));
		}
		else if(result.equalsIgnoreCase("You might be suffering from stomach infection")){
			disease = "gall+bladder+infection";
			prescription.setText(Html.fromHtml(getString(R.string.stomach_infection_html)));
		}
		else if(result.equalsIgnoreCase("You might have urinary tract infection.")){
			disease = "urinary+tract+infection";
			prescription.setText(Html.fromHtml(getString(R.string.urinary_track_infection_html)));
		}
		else if(result.equalsIgnoreCase("You might Have sinus")){
			disease = "sinus";
			prescription.setText(Html.fromHtml(getString(R.string.sinus_html)));
		}
		else if(result.equalsIgnoreCase("you might have cold.")){
			disease = "cold";
			prescription.setText(Html.fromHtml(getString(R.string.cold_html)));
		}
		else if(result.equalsIgnoreCase("You might have migrane.")){
			disease = "migraine";
			prescription.setText(Html.fromHtml(getString(R.string.migrain_html)));
		}
		else if(result.equalsIgnoreCase("You might have allergic rhinitis")){
			disease = "allergic+rhinitis";
			prescription.setText(Html.fromHtml(getString(R.string.allergic_rhinitis_html)));
		}
		else{
			prescription.setText("");
			disease = "";
		}
		prescription.setMovementMethod(new ScrollingMovementMethod());
		
		prescription.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("in button click!!!");
				try {
					Uri uri = Uri.parse( "http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords="+disease+"+medicines" );
					startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		
		 sayTObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
				@Override
				public void onInit(int status) {
					if (status != TextToSpeech.ERROR) {
						/*startButton.setEnabled(false);
						callButton.setEnabled(false);
						Help.setEnabled(false);
						close.setEnabled(false);*/
						System.out.println("In object creation");
						sayTObj.setLanguage(Locale.UK);
						map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
								"UniqueID");
						if(!disease.equals("")){
							sayTObj.speak("Prescription details are shown on the screen. To purchase prescription online, say purchase, To make call to doctor, say call. To read instructions, say instruction, To retake the test, say restart, To close application say close", TextToSpeech.QUEUE_FLUSH, map);	
						}else
						sayTObj.speak("To make call to doctor, say call. To read instructions, say instruction. To retake the test, say restart. To close application, say close", TextToSpeech.QUEUE_FLUSH, map);
						sayTObj.setOnUtteranceProgressListener(new UtteranceProgressListener() {

							@Override
							public void onDone(String utteranceId) {
								// TODO Auto-generated method stub
								System.out.println("handlerrrrr ------->");
						        // Do something after 5s = 5000ms
						    	System.out.println("uttereance complete!!!");
						    	/*startButton.setEnabled(true);
								callButton.setEnabled(true);
								Help.setEnabled(true);
								close.setEnabled(true);*/
								Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
								i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
								/*startButton.setEnabled(true);
								callButton.setEnabled(true);
								Help.setEnabled(true);
								close.setEnabled(true);*/
								try {
									chooseOptions = true;
									startActivityForResult(i, REQUEST_OK);
									
								} catch (Exception e) {
								//	System.out.println("Exception"+e.printStackTrace());
								}								
								

							}

							@Override
							public void onError(String arg0) {
								System.out.println("errror-->");
							}

							@Override
							public void onStart(String arg0) {
								System.out.println("start-->");

							}					
	
					});
						
					}
				}
											
				});
		
		

			/*final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			    	System.out.println("handlerrrrr ------->");
			        // Do something after 5s = 5000ms
			    	System.out.println("uttereance complete!!!");
					Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
					try {
						chooseOptions = true;
						startActivityForResult(i, REQUEST_OK);
					} catch (Exception e) {
					//	System.out.println("Exception"+e.printStackTrace());
					}
			    	
			    }
			}, 18000);*/
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_OK && resultCode == RESULT_OK) {
			/*startButton.setEnabled(true);
			callButton.setEnabled(true);
			Help.setEnabled(true);
			close.setEnabled(true);*/
			ArrayList<String> thingsYouSaid = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			System.out.println("thingsYouSaid ----->"+thingsYouSaid.toString());
			String spokenData = thingsYouSaid.get(0);
			System.out.println("You said-->" + spokenData);

			try {
				System.out.println("data-->" + spokenData);
				if(chooseOptions){
					chooseOptions = false;
					if(spokenData.equalsIgnoreCase("call")){
						String posted_by = "111-333-222-4";
						String uri = "tel:" + posted_by.trim();
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse(uri));
						startActivity(callIntent);
					}else if(spokenData.equalsIgnoreCase("close")){
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.addCategory(Intent.CATEGORY_HOME);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
				        finish();
					}else if(spokenData.equalsIgnoreCase("instruction")){
						System.out.println("help cutton----------->");
						Help helpDialog = new Help(TestActivity.this);
						helpDialog.setTitle("Instructions");
						helpDialog.show();  
					}
					else if(spokenData.equalsIgnoreCase("restart")){
						System.out.println("in button click!!!");
						try {
							Resources res = getResources();
							BufferedReader reader = new BufferedReader(new InputStreamReader(
									res.openRawResource(R.raw.file)));
							try {

								System.out.println("in----------------" + reader);
								String str, str1;
								while ((str = reader.readLine()) != null) {
									a.add(str);

								}

								System.out.println("a--->" + a);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							BST.getTree(a, getApplicationContext());
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}else if(spokenData.equalsIgnoreCase("purchase")){
						try {
							Uri uri = Uri.parse( "http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords="+disease+"+medicines" );
							startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
						else{
					

						 validateTObj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
								@Override
								public void onInit(int status) {
									if (status != TextToSpeech.ERROR) {
										/*startButton.setEnabled(false);
										callButton.setEnabled(false);
										Help.setEnabled(false);
										close.setEnabled(false);*/
										System.out.println("In object creation");
										sayTObj.setLanguage(Locale.UK);
										map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
												"UniqueID");
										sayTObj.speak("This is not a valid input. Valid entries are purchase, call, restart, instruction and close. Please try again.", TextToSpeech.QUEUE_FLUSH, map);
										sayTObj.setOnUtteranceProgressListener(new UtteranceProgressListener() {

											@Override
											public void onDone(String utteranceId) {
												// TODO Auto-generated method stub
												System.out.println("uttereance complete!!!");
												Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
												i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
												/*startButton.setEnabled(true);
												callButton.setEnabled(true);
												Help.setEnabled(true);
												close.setEnabled(true);*/
												try {
													chooseOptions = true;
													startActivityForResult(i, REQUEST_OK);
												} catch (Exception e) {
												//	System.out.println("Exception"+e.printStackTrace());
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
				}else
				bstObj.navigate(spokenData, this.current);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	
}

