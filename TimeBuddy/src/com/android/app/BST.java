package com.android.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.speech.RecognitionService.Callback;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

public class BST<T extends Comparable<T>> implements Iterable<T> {
	protected static final int REQUEST_OK = 1;
	static String spokenData;
	static TextToSpeech ttobj;
	static Context context;
	Node<T> current;
	Context c ;
	HashMap<String, String> map = new HashMap<String, String>();

	public static void getTree(ArrayList<String> a, Context c)
			throws FileNotFoundException {

		ArrayList<element> Elements = new ArrayList<element>();
		context = c;

		BST bst = new BST();
		int values[] = { 32, 26, 4, 2, 1, 3, 6, 5, 10, 8, 7, 9, 18, 14, 12, 11,
				13, 16, 15, 17, 20, 19, 22, 21, 24, 23, 25, 30, 28, 27, 29, 31,
				44, 40, 36, 34, 33, 35, 38, 37, 39, 42, 41, 43, 48, 46, 45, 47,
				52, 50, 49, 51, 53 };

		int index = 0;
		for (index = 0; index < a.size(); index++) {

			int current = values[index] - 1;
			System.out.println(a.get(current) + "   ");
			element e = new element(a.get(current), values[index]);
			Elements.add(e);

		}

		for (element n : Elements) {

			bst.insert(n);
		}

		bst.preOrderTraversal();

	}

	private Node<T> root;
	private element<T> elNode;
	private Comparator<T> comparator;

	public BST() {
		root = null;
		comparator = null;
		elNode = null;
	}

	public BST(Comparator<T> comp) {
		root = null;
		comparator = comp;
	}

	private int compare(T x, T y) {
		if (comparator == null)
			return x.compareTo(y);
		else
			return comparator.compare(x, y);
	}

	public void insert(element<T> data) {
		root = insert(root, data.data, data.index);
	}

	private Node<T> insert(Node<T> p, T toInsert, T toVal) {

		System.out.println("P is---------->" + toInsert + "toVal-->" + toVal);
		if (p == null) {
			System.out.println("P firstttttttt---------->" + toInsert
					+ "toVal-->" + toVal);
			return new Node<T>(toInsert, toVal);
		}

		if (compare(toVal, p.val) == 0) {
			return p;
		}

		if (compare(toVal, p.val) < 0) {
			System.out.println("in leftf---->" + p);
			p.left = insert(p.left, toInsert, toVal);
		} else {
			System.out.println("in right---->" + p);
			p.right = insert(p.right, toInsert, toVal);
		}

		return p;
	}

	public boolean search(T toSearch) {
		return search(root, toSearch);
	}

	private boolean search(Node<T> p, T toSearch) {
		if (p == null)
			return false;
		else if (compare(toSearch, p.data) == 0) {
			System.out.println();
			p = p.left;
			System.out.println("left--->" + p.data);
			return true;

		} else if (compare(toSearch, p.data) < 0)
			return search(p.left, toSearch);
		else
			return search(p.right, toSearch);
	}

	public void delete(T toDelete) {
		root = delete(root, toDelete);
	}

	private Node<T> delete(Node<T> p, T toDelete) {
		if (p == null)
			throw new RuntimeException("cannot delete.");
		else if (compare(toDelete, p.data) < 0)
			p.left = delete(p.left, toDelete);
		else if (compare(toDelete, p.data) > 0)
			p.right = delete(p.right, toDelete);
		else {
			if (p.left == null)
				return p.right;
			else if (p.right == null)
				return p.left;
			else {
				// get data from the rightmost node in the left subtree
				p.data = retrieveData(p.left);
				// delete the rightmost node in the left subtree
				p.left = delete(p.left, p.data);
			}
		}
		return p;
	}

	private T retrieveData(Node<T> p) {
		while (p.right != null)
			p = p.right;

		return p.data;
	}

	/*************************************************
	 * 
	 * toString
	 * 
	 **************************************************/

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (T data : this)
			sb.append(data.toString());

		return sb.toString();
	}

	/*************************************************
	 * 
	 * TRAVERSAL
	 * 
	 **************************************************/
	public void speakText(final String s, final Node r) {

		ttobj = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status != TextToSpeech.ERROR) {
					System.out.println("In object creation");
					ttobj.setLanguage(Locale.UK);
					map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
							"UniqueID");
					ttobj.speak(s, TextToSpeech.QUEUE_FLUSH, map);
					ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {

						@Override
						public void onDone(String utteranceId) {
							// TODO Auto-generated method stub
							System.out.println("uttereance complete!!!");
							System.out.println("m ------>" + r.left + " "
									+ r.right);
							if (r.left != null && r.right != null)
								TestActivity.getInstance().startRecognition(r);
							else{

								TestActivity.getInstance().runOnUiThread(new Runnable() {

						                @Override
						                public void run() {
						                	TestActivity.getInstance().getResult(r.data.toString());
						                	
						                }
						            });
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

	public void navigate(String spokenData, Node current2) {
		this.spokenData = spokenData;
		this.c = c;
		System.out.println("spokenData---->" + this.spokenData);
		// if yes go to right
		System.out.println("current navigate---->" + current2);
		System.out.println("this.current.right----->" + current2.right);
		System.out.println("this.current.left----->" + current2.left);
		if (spokenData.equalsIgnoreCase("yes")) {
			System.out.println("YESSSSSSSSSSS");
			preOrderHelper(current2.left, true);
		} else if (spokenData.equalsIgnoreCase("no")
				|| spokenData.equalsIgnoreCase("skip")) {
			System.out.println("Noooooooooo");
			preOrderHelper(current2.right, true);
		} else {

			preOrderHelper(current2, false);
		}
	}

	ArrayList preBst = new ArrayList();
	ArrayList preBstFinal = new ArrayList();

	public void preOrderTraversal() {
		System.out.println("root--->" + root);
		preOrderHelper(root, true);
	}

	private void preOrderHelper(Node r, boolean isValid) {
		System.out.println("r--->" + r);
		System.out.println("r left--->" + r.left);
		System.out.println("r right--->" + r.right);
		if (r != null && isValid) {
			this.current = r;
			System.out.println("current in pre------>" + this.current);
			System.out.print("here------------" + r + " ");
			System.out.println("r.data.toString()---->" + r.data.toString());

			speakText(r.data.toString(), r);

		} else {
			speakText(
					"Valid entries for answers are yes, no or skip! Please try again."
							+ r.data.toString(), r);
		}
	}

	public void inOrderTraversal() {
		inOrderHelper(root);
	}

	private void inOrderHelper(Node r) {
		if (r != null) {
			inOrderHelper(r.left);
			System.out.print(r + " ");
			inOrderHelper(r.right);
		}
	}

	public int height() {
		return height(root);
	}

	private int height(Node<T> p) {
		if (p == null)
			return -1;
		else
			return 1 + Math.max(height(p.left), height(p.right));
	}

	public int countLeaves() {
		return countLeaves(root);
	}

	private int countLeaves(Node<T> p) {
		if (p == null)
			return 0;
		else if (p.left == null && p.right == null)
			return 1;
		else
			return countLeaves(p.left) + countLeaves(p.right);
	}

	public int width() {
		int max = 0;
		for (int k = 0; k <= height(); k++) {
			int tmp = width(root, k);
			if (tmp > max)
				max = tmp;
		}
		return max;
	}

	// rerturns the number of node on a given level
	public int width(Node<T> p, int depth) {
		if (p == null)
			return 0;
		else if (depth == 0)
			return 1;
		else
			return width(p.left, depth - 1) + width(p.right, depth - 1);
	}

	// The diameter of a tree is the number of nodes
	// on the longest path between two leaves in the tree.
	public int diameter() {
		return diameter(root);
	}

	private int diameter(Node<T> p) {
		if (p == null)
			return 0;

		// the path goes through the root
		int len1 = height(p.left) + height(p.right) + 3;

		// the path does not pass the root
		int len2 = Math.max(diameter(p.left), diameter(p.right));

		return Math.max(len1, len2);
	}

	public Iterator<T> iterator() {
		return new MyIterator();
	}

	// pre-order
	private class MyIterator implements Iterator<T> {
		Stack<Node<T>> stk = new Stack<Node<T>>();

		public MyIterator() {
			if (root != null)
				stk.push(root);
		}

		public boolean hasNext() {
			return !stk.isEmpty();
		}

		public T next() {
			Node<T> cur = stk.peek();
			if (cur.left != null) {
				stk.push(cur.left);
			} else {
				Node<T> tmp = stk.pop();
				while (tmp.right == null) {
					if (stk.isEmpty())
						return cur.data;
					tmp = stk.pop();
				}
				stk.push(tmp.right);
			}

			return cur.data;
		}// end of next()

		public void remove() {

		}
	}

	static class Node<T> {
		private T data;
		private T val;
		private Node<T> left, right;

		public Node(T data, T val, Node<T> l, Node<T> r) {
			left = l;
			right = r;
			this.data = data;
			this.val = val;
		}

		public Node(T data, T val) {
			this(data, val, null, null);
		}

		public String toString() {
			return data.toString();
		}
	} // end of Node

	public static class element<T> {
		public T index;
		public T data;

		public element(T data, T index) {

			this.data = data;
			this.index = index;
		}

		public String toString() {
			return data.toString();
		}

	}

}// end of BST

class MyComp1 implements Comparator<Integer> {
	public int compare(Integer x, Integer y) {
		return y.compareTo(x);
	}
}