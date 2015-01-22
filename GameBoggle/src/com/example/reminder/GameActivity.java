package com.example.reminder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity implements ChannelListener, com.example.reminder.DeviceListFragment.DeviceActionListener{

    private TableLayout iGrid;
    private GridView iWordList;
    private ArrayAdapter<String> iWordListAdapter;
    ArrayList<String> myArray= new ArrayList<String>();
    int correctAnswers= 0;
    private Vector<View> iLetterViews;
    
    private BoggleGame iGame;
    private WordBuilder iWordBuilder;
    
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private IntentFilter intentFilter;
    private Channel channel;
    private BroadcastReceiver receiver = null;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // add necessary intent values to be matched.
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
       // intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
       // intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        setContentView(R.layout.main);
       // startGame();
        
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
    
    private void startGame(){
    	setContentView(R.layout.activity_main);
        
        //GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.GestureOverlay);
        //gestures.addOnGestureListener( this );
        
        int size = 4;
        GridCreator g = new GridCreator();
        String[] letters = g.createGrid();
        
        
        for(int i = 0; i< letters.length; i++){
        	System.out.println("letters -->"+letters[i]);
        }
        //String[] letters = { "z", "o", "E", "b", "r", "s", "a", "m", "p" };
        createGame( letters, size );
    }
    private void createGame( String[] letters, int squareSize ) {
        iGame = new BoggleGame( letters, squareSize );
        iWordBuilder = new WordBuilder( iGame );
        iLetterViews = new Vector<View>();
        
        iGrid = (TableLayout) findViewById( R.id.boggleGrid );
        for( int i = 0; i < squareSize; i++ ) {
            TableRow row = new TableRow( this );
            for( int j = 0; j < squareSize; j++ ) {
                View letter = createLetterView( letters[i*squareSize + j].toUpperCase() );
               // Add the new view to the list of letter views. This is for an index/view mapping.
               iLetterViews.add( letter ); 
               row.addView( letter );
            }
            iGrid.addView( row );
        }

        iWordListAdapter = new ArrayAdapter<String>( this, R.layout.word_view, iGame.getWords() );
        iWordList = (GridView)findViewById( R.id.wordList );
        iWordList.setAdapter( iWordListAdapter );
        //iWordList.setNumColumns( 3 );
        
        // Hook up the submit button
        final Button submitButton = (Button)findViewById( R.id.submitButton );
        submitButton.setOnClickListener( new OnClickListener(){
            public void onClick( View v ) {
                submitCurrentWord();
            }} );
        
        // Hook up the cancel button
        final Button cancelButton = (Button)findViewById( R.id.cancelButton );
        cancelButton.setOnClickListener( new OnClickListener(){
            public void onClick( View v ) {
                cancelCurrentWord();
            }} );
        
     // Hook up the take new game button
        final Button startButton = (Button)findViewById( R.id.startGame );
        startButton.setVisibility(View.INVISIBLE);
        startButton.setOnClickListener( new OnClickListener(){
            public void onClick( View v ) {
            	startGame();
            }} );
        
        final Button timerButton = (Button)findViewById( R.id.timerButton);
        new CountDownTimer(30000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 timerButton.setText(""+millisUntilFinished / 1000);
		     }

		     public void onFinish() {
		    	
		    	 System.out.println("iGame---------------->"+iGame.getWords());
		    	
				    try {
				       
				    	Resources res = getResources();
				    	BufferedReader reader = new BufferedReader(
				                 new InputStreamReader(res.openRawResource(R.raw.dict)));
				    	
				    	System.out.println("in----------------"+reader);
				        String str;
				        while ((str = reader.readLine()) != null) {
				        	myArray.add(str);
				      
				    }
				        } catch (Exception e) {
				         e.printStackTrace();
				        
				    }
				    
		    	 //startActivity(intent);
				    System.out.println("myArray sizeee :"+myArray.size());
		    	
		    	    for(int i=0; i< iGame.getWords().size(); i++){
		    	    	System.out.println("in loooooooopp");
		    	    	String str = iGame.getWords().get(i);
		    	    	System.out.println("str:::"+str);
		    	    	
		    	    	int index = find(str ,myArray);
		    	    	System.out.println("index:::"+index);
		    	    	if(index != -1){
		    	    		correctAnswers++;
		    	    	}
		    	    	
		    	    }
		    	    System.out.println("correctAnswers: "+correctAnswers);
		    	    //timerButton.setText("Time is up!");
		    	    timerButton.setVisibility(View.INVISIBLE);
		    	    cancelButton.setVisibility(View.INVISIBLE);
		    	    submitButton.setVisibility(View.INVISIBLE);
		    	    iWordList.setVisibility(View.INVISIBLE);
		    	    iGrid.setVisibility(View.INVISIBLE);
		    	    
		    	    TextView scoreView = createScoreView(correctAnswers);
		    	    LinearLayout linearlayout;
		    	    linearlayout = (LinearLayout) findViewById(R.id. scoreLayout);
		    	    //iGrid = (TableLayout) findViewById( R.id.boggleGrid );
		    	    // iGrid.addView(scoreView);
		    	    linearlayout.addView(scoreView);
		    	    startButton.setVisibility(View.VISIBLE);
		     }
		  }.start();
    }

   
    public void onGestureCancelled( GestureOverlayView arg0, MotionEvent arg1 ) {
        Log.v( "Tag", "onGestureCancelled" );
        
        iWordBuilder.cancelCurrentWord();
    }

    public void onGestureEnded( GestureOverlayView arg0, MotionEvent arg1 ) {
        Log.v( "BoggleGame", "onGestureEnded, currentWord = " + iWordBuilder.getCurrentWord() );
        if( iWordBuilder.isThereACurrentWord() ) {
            submitCurrentWord();
        } 
        
        cancelCurrentWord();
    }

    private void highlightLetter( int letterIndex ) {
        changeLetterBackgroundColour( letterIndex, Color.WHITE );
    }
    
    @SuppressWarnings( "unused" )
    private void unhighlightLetter( int letterIndex ) {
        changeLetterBackgroundColour( letterIndex, Color.TRANSPARENT );
    }

    @SuppressWarnings( "unused" )
    private void highlightLetter( View letterView ) {
        changeLetterBackgroundColour( letterView, Color.WHITE );
    }
    
    private void unhighlightLetter( View letterView ) {
        changeLetterBackgroundColour( letterView, Color.RED );
    }
    
    private void unhighlightAllLetters() {
        for( View letter : iLetterViews ) {
            unhighlightLetter( letter );
        }
    }
    
    private void changeLetterBackgroundColour( int letterIndex, int colour ) {
    	System.out.println("colour-->"+colour);
        changeLetterBackgroundColour( iLetterViews.get( letterIndex ), colour );
    }

    private void changeLetterBackgroundColour( View letterView, int colour ) {
        if( letterView != null ) {
            letterView.setBackgroundColor( colour );
        }
    }
    
   
    
    private void submitCurrentWord() {
        iGame.submitWord( iWordBuilder.getCurrentWord() );
        iWordListAdapter.notifyDataSetChanged();
        //cancelCurrentWord();
        unhighlightAllLetters();
    }
    
    private void cancelCurrentWord() {
        iWordBuilder.cancelCurrentWord();
        unhighlightAllLetters();
    }
    
    private void addLetter( int letterIndex ) {
        if( iWordBuilder.isValidNextLetter( letterIndex ) && iWordBuilder.pickLetter( letterIndex ) ) {
            highlightLetter( letterIndex );
        }
    }
    
    private View createLetterView( String letterString ) {
        TextView letter = new TextView( this );

        letter.setText( letterString );
        letter.setHeight( 50 );
        letter.setWidth( 50 );
        letter.setPadding( 10, 10, 10, 10 );
        letter.setBackgroundColor( Color.RED );
        letter.setGravity( Gravity.CENTER );
        letter.setOnClickListener( new OnClickListener( ){

         public void onClick( View v ) {
             System.out.println( "Clicked on letter " + iLetterViews.indexOf( v ) );
             addLetter( iLetterViews.indexOf( v ) );
         }} );
        
        return letter;
    }
    
    private TextView createScoreView( int scores ) {
        TextView score = new TextView( this );
        score.setText( "Score : "+Integer.toString(scores) );
        score.setHeight( 150 );
        score.setWidth( 250 );
        score.setPadding( 10, 10, 10, 10 );
        score.setBackgroundColor( Color.GREEN );
        score.setGravity( Gravity.CENTER );
        
        return score;
    }
    public static int find(String str, ArrayList<String> myArray2) {
        int lo = 0;
        int hi = myArray2.size() - 1;
        str = str.toUpperCase();
      while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            int mid = lo + (hi - lo) / 2;
            String s= (String) myArray2.get(mid);
            s = s.toUpperCase();
            if      (str.compareTo(s) < 0 ) {
            	hi = mid - 1;
            	System.out.println("< 0 :::"+hi);
            	}
            else if (str.compareTo(s) > 0 ){ 
            	lo = mid + 1;
            	System.out.println("> 0"+lo);
            }
            else return mid;
        }
        return -1;
    }
    public static void search(String input, Set<String> dictionary,
            Stack<String> words, List<List<String>> results) {

    	System.out.println("in search!!!!!!!!!!!");
        for (int i = 0; i < input.length(); i++) {
            // take the first i characters of the input and see if it is a word
            String substring = input.substring(0, i + 1);

            if (dictionary.contains(substring)) {
                // the beginning of the input matches a word, store on stack
                words.push(substring);

                if (i == input.length() - 1) {
                    // there's no input left, copy the words stack to results
                    results.add(new ArrayList<String>(words));
                } else {
                    // there's more input left, search the remaining part
                    search(input.substring(i + 1), dictionary, words, results);
                }

                // pop the matched word back off so we can move onto the next i
                words.pop();
            }
        }
    }
    
    
    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	System.out.println("item.getItemId() -- >"+item.getItemId());
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    System.out.println("channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
            	
            	System.out.println("isWifiP2pEnabled-->"+isWifiP2pEnabled);
                if (!isWifiP2pEnabled) {
                    Toast.makeText(GameActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                System.out.println("DeviceListFragment -->"+R.id.frag_list);
                DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                System.out.println("111111111111-->"+fragment);
                fragment.onInitiateDiscovery();
                System.out.println("22222-->"+isWifiP2pEnabled);
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(GameActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(GameActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
            	System.out.println("HERE!!!!!!!!!!!!!!!!!");
                return super.onOptionsItemSelected(item);
        }
    }
	
    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(GameActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                System.out.println("Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(GameActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(GameActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }
    
}
