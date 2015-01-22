package com.example.reminder;


import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.util.Log;

public class GridCreator {
	

public String gridLetter[] = new String[16];
String vowel[] = {"a","i","e","o","u"};
String con1[] = {"b","d","h","n","r","s","t"};
String con2[] = {"f","c","g","l","p","k","m","y","w","v"};
String con3[] = {"j","q","x","z"};


     public  String[] createGrid(){
        System.out.println("first---");
        
        
         generate(vowel, 5);
         generate(con1, 5);
         generate(con2, 4);
         generate(con3, 2);
          for (int i = 0; i < gridLetter.length; i++) {
            System.out.print(""+gridLetter[i]+" ");
          }
          j= 0;
          System.out.println();
          System.out.print("final---\n");
          String[] finalArray = shuffle(gridLetter);
          for (int i = 0; i < finalArray.length; i++) {
            System.out.print(""+finalArray[i]+" ");
          }
          
          return finalArray;
        
     }
      private int[] pool;           // The pool of numbers.
    private int size;             // The current "size".
    private Random rnd;           // A random number         generator.
 public  int j = 0;
    // Constructor: just initilise the pool.

 	public GridCreator (){}
    public GridCreator (int sz) {
        pool = new int[sz];
        size = sz;
        rnd = new Random();
        for (int i = 0; i < size; i++) pool[i] = i;
    }
    
    public  void generate(String array[],int frequency){
        GridCreator tp = new GridCreator (array.length);
        System.out.println("pass --");
        for (int i = 0; i < frequency; i++) {
            int index = tp.next();
            if(index != -1){
           gridLetter[j] = array[index];
           System.out.println(" "+array[index]+" ");
           j++;
           
            }

        }
    }
    public  String[] shuffle(String ar[]){
        Random rnd = new Random();
    for (int i = ar.length - 1; i > 0; i--)
    {
      int index = (int)rnd.nextInt(i + 1);
      // Simple swap
      String a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    }
        return ar;
    }

    // Get next random number in pool (or -1 if exhausted).

    public int next() {
        if (size < 1) return -1;
        int idx = rnd.nextInt(size--);
        int rval = pool[idx];
        pool[idx] = pool[size];
        return rval;
    }

}
