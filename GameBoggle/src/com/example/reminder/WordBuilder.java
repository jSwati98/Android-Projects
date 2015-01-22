package com.example.reminder;

import java.util.Vector;

import android.util.Log;

public class WordBuilder {

    private BoggleGame iGame;
    
    // Current word stuff
    private Vector<Integer> iCurrWord = new Vector<Integer>(); // A list of indices into the boggle grid that represents the current word

    public WordBuilder( BoggleGame game ) {
        iGame = game;
    }

    public boolean isValidNextLetter( int newLetterIndex ) {
        // Check that the letter is within bounds
        if( !iGame.isValidIndex( newLetterIndex ) ) {
            return false;
        }
        
        // If the letter isn't in the list already
        for( Integer letter : iCurrWord ) {
            if( letter.intValue() == newLetterIndex ) {
                return false;
            }
        }

        int squareSize = iGame.getGridSquareSize();
        
        // Is the new letter adjacent?
        if( iCurrWord.size() > 0 ) {
            Integer lastLetter = iCurrWord.get( iCurrWord.size() - 1 );
            int lastLetterIndex = lastLetter.intValue();
            int rowDiff = Math.abs( ( lastLetterIndex / squareSize ) - ( newLetterIndex / squareSize ) );
            int colDiff = Math.abs( ( lastLetterIndex % squareSize ) - ( newLetterIndex % squareSize ) );
            if( rowDiff > 1 || colDiff > 1 || ( rowDiff == 0 && colDiff == 0 ) ) {
                return false;
            }
        }

        // We cool
        return true;
    }

    /**
     * Adds another letter to the current word
     * Returns true if the letter is acceptable, false otherwise.
     */
    public boolean pickLetter( int newLetterIndex ) {
        Log.v( "BoggleGame", "Pick letter starting" );
        if( !isValidNextLetter( newLetterIndex ) ) {
            return false;
        }

        Log.v( "BoggleGame", "Valid letter" );
        // The letter is valid
        iCurrWord.add( new Integer( newLetterIndex ) );
        return true;
    }

    public void cancelCurrentWord() {
        iCurrWord.clear();
    }

    public boolean isThereACurrentWord() {
        return( !iCurrWord.isEmpty() );
    }

    public String getCurrentWord() {
        String currentWord = "";
        String[] letters = iGame.getLetters();
        for( Integer letter : iCurrWord ) {
            currentWord += letters[letter];
        }
        return currentWord;
    }
    
}
