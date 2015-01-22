package swin.chat.util;

import android.os.AsyncTask;

public class CallBackAsyncTask<PARAM,PROGRESS,RESULT> extends AsyncTask<PARAM,PROGRESS, RESULT> {
	
	public static class CallBack<PARAM,PROGRESS,RESULT>
	{
		public void onPreExecute(AsyncTask<PARAM,PROGRESS,RESULT> asyncTask){}
		
		public void onProgressUpdate(PROGRESS[] progress,AsyncTask<PARAM,PROGRESS,RESULT> asyncTask){}
		
		public void onPostExecute(RESULT result,AsyncTask<PARAM,PROGRESS,RESULT> asyncTask, Exception exception){}

		public void onCancelled(AsyncTask<PARAM,PROGRESS,RESULT> asyncTask) {}
	}
	
	public static abstract class BackgroundCallBack<PARAM,PROGRESS,RESULT>
	{
		public abstract RESULT doInBackground(AsyncTask<PARAM,PROGRESS, RESULT> asyncTask, PARAM[] params) throws Exception;
	}
	
	private CallBack<PARAM, PROGRESS, RESULT> callBack;
	private BackgroundCallBack<PARAM, PROGRESS, RESULT> backgroundCallBack;
	private Exception exception;
	
	private final static CallBack defaultCallback = new CallBack();
	
	public CallBackAsyncTask(BackgroundCallBack<PARAM, PROGRESS, RESULT> backgroundCallBack)
	{
		this(null, backgroundCallBack);
	}
	
	public CallBackAsyncTask(CallBack<PARAM, PROGRESS, RESULT> callback, BackgroundCallBack<PARAM, PROGRESS, RESULT> backgroundCallBack) {
		this.callBack = callback;
		if(callBack == null)
			callBack = defaultCallback;
		this.backgroundCallBack = backgroundCallBack;
	}
	
	@Override
	protected RESULT doInBackground(PARAM... params) {
		RESULT result = null;
		try
		{
			result = this.backgroundCallBack.doInBackground(this, params);
		}catch(Exception e)
		{
			exception = e;
		}
		return result;
	}
	
	@Override
	protected void onPreExecute() {
		callBack.onPreExecute(this);
	}
	
	@Override
	protected void onProgressUpdate(PROGRESS ... values) {
		callBack.onProgressUpdate(values, this);
	};
	
	@Override
	protected void onPostExecute(RESULT result) {
		callBack.onPostExecute(result, this, exception);
	};
	
	@Override
	protected void onCancelled() {
		callBack.onCancelled(this);
	}

}
