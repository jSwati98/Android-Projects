package com.example.reminder;


import java.util.List;
import java.util.Vector;

import android.util.Log;

public class BoggleGame {

    private String[] iLetters;
    private int iSquareSize;

    // Word list stuff
    private Vector<String> iWords = new Vector<String>();

    public BoggleGame( String[] letters, int squareSize ) {

        if( letters == null || letters.length != ( squareSize * squareSize ) ) {
            throw new IllegalArgumentException( "Bad data to newGame. size = " + squareSize );
        }
        iLetters = letters;
        iSquareSize = squareSize;
    }
    
    public List<String> getWords() {
        return iWords;
    }
    
    /**
     * Submit the current word, returning true if it's valid.
     */
    public boolean submitWord( String word ) {

        Log.v( "BoggleGame", "submitCurrentWord " + word );
        
        if( isValidWord( word ) ) {
            iWords.add( word );
            return true;
        } else {
            return false;
        }
            
    }
    
    public boolean isValidWord( String newWord ) {
        Log.v( "BoggleGame", "isValidWord() " + (!iWords.contains( newWord ) ) );  
        return( !iWords.contains( newWord ) );
    }
    
    public boolean isValidIndex( int letterIndex ) {
        return( letterIndex >= 0 && letterIndex < iLetters.length );
    }

    public int getGridSquareSize() {
        return iSquareSize;
    }

    public String[] getLetters() {
        return iLetters;
    }
}
