package android.wellness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class WakefulIntentService extends AsyncTask<Context, String, Void> {
	Context context2;
	String message;
	String username;
	
	public WakefulIntentService(Context context) {
		super();
		context2 = context;
		message = "";
	}
	
	@Override
	public Void doInBackground(Context... params) {
		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/showMsg.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String user = "mgreene";

		List<NameValuePair> nvplist = new ArrayList<NameValuePair>();
		nvplist.add(new BasicNameValuePair("username", user));

		try {
			hp.setEntity(new UrlEncodedFormEntity(nvplist));
			Log.w("AysncTask", "Execute HTTP Post Request");
			hr = hc.execute(hp);
			he = hr.getEntity();
			is = he.getContent();
			Log.w("AsyncTask", "Response Handling");
			isr = new InputStreamReader(is, "iso-8859-1");
			br = new BufferedReader(isr, 8);
			String line = br.readLine();
			while(line != null){
				sb.append(line + "\n");
				line = br.readLine();
			}
			is.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String response = sb.toString();
		Log.w("AsyncTask", response);
		if(Integer.parseInt(response.substring(0, 1)) == 0){
				message = response.substring(1);
				Log.w("Notification", message);
			if (message.length() > 1) {
				Log.w("Notification", "Sending Notification");
				publishProgress(message);
			}
			else{
			}
		}
		return null;
	}


	public void doToast() {
		Toast.makeText(context2, "no msg", Toast.LENGTH_SHORT).show();
	}

	public void onProgressUpdate(String...params) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager nm = (NotificationManager)context2.getSystemService(ns);
		
		final CharSequence from = "Server";
		
        Intent i = new Intent(context2, MsgUserActivity.class);
        i.putExtra(ServiceprojActivity.KEY_FROM, from);
        i.putExtra("username", username);
        i.putExtra(ServiceprojActivity.KEY_MESSAGE, message);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context2, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);


		// Vibrate the mobile phone
		Vibrator vibrator = (Vibrator)context2.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(2000);
		// construct the Notification object.
		Notification notif = new Notification(R.drawable.frog, message,
				System.currentTimeMillis());

		// Set the info for the views that show in the notification panel.
		notif.setLatestEventInfo(context2, from, message, contentIntent);
		notif.defaults = Notification.DEFAULT_ALL;
		nm.notify(R.string.imcoming_message_ticker_text, notif);
	}
}



