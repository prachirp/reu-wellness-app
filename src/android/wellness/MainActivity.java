package android.wellness;

import java.io.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.json.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
	
	public String message; //Example message: 0[{"id":"10","uName":"uname","passWd":"pword","firstName":"fname","lastName":"lname"}]
	public int acctType;
	public int uid;
	public String username;
	public String firstname;
	public String lastname;
	public String withings_username;
	public String withings_password;
	public String fitbit_username;
	public String fitbit_password;
	public String messageFrom;
	public String messageBody;
	TextView welcome;
	public static final String USERNAME_MESSAGE = "android.wellness.USERNAME";
	public static final String WITHINGS_UNAME = "android.wellness.WUNAME";
	public static final String WITHINGS_PWORD = "android.wellness.WPWORD";
	public static final String FITBIT_UNAME = "android.wellness.FUNAME";
	public static final String FITBIT_PWORD = "android.wellness.FPWORD";

	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent sIntent = new Intent(MainActivity.this, alarmservice.class);
		sIntent.setAction(username);
		startService(sIntent);

		
		welcome = (TextView)findViewById(R.id.main_hello);
		Intent intent = getIntent();
		message = intent.getStringExtra("android.wellness.MESSAGE");
		decodeMessage();
		
		String welcomeMsg = " " + firstname;
		if(acctType == 0){
			welcomeMsg = " " + firstname + "! As a new user, you should update your information. Click the button below.";
		}
		welcome.append(welcomeMsg);
	}
	
	public void decodeMessage(){
		acctType = Integer.parseInt(message.substring(0, 1));
		JSONArray array = null;
		JSONObject obj = null;
		try {
			array = new JSONArray(message.substring(1));
			obj = array.getJSONObject(0);			
			uid = obj.getInt("id");
			username = obj.getString("uName");
			firstname = obj.getString("firstName");
			lastname = obj.getString("lastName");
			withings_username = obj.getString("wName");
			withings_password = obj.getString("wPword");
			fitbit_username = obj.getString("fName");
			fitbit_password = obj.getString("fPword");
			messageFrom = obj.getString("messageFrom");
			messageBody = obj.getString("message");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void onResume(){
		super.onResume();

		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/getMessage.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		List<NameValuePair> nvplist = new ArrayList<NameValuePair>();
		nvplist.add(new BasicNameValuePair("username", username));
		
		try {
			hp.setEntity(new UrlEncodedFormEntity(nvplist));
			Log.w("Message on Resume", "Execute HTTP Post Request");
			hr = hc.execute(hp);
			he = hr.getEntity();
			is = he.getContent();
			Log.w("Message on Resume", "Response Handling");
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
		Log.w("Message on Resume", response);
		if(Integer.parseInt(response.substring(0, 1)) == 0){
			JSONArray array = null;
			JSONObject obj = null;
			try {
				array = new JSONArray(message.substring(1));
				obj = array.getJSONObject(0);
				messageFrom = obj.getString("messageFrom");
				messageBody = obj.getString("message");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(messageBody.length()>1){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				String promptMessage = "From: " + messageFrom + "\n" + messageBody;
				builder.setMessage(promptMessage);
				builder.setCancelable(false);
				builder.setTitle(R.string.main_prompt_title);
				builder.setPositiveButton("Reply", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent itn = new Intent(MainActivity.this, MsgUserActivity.class);
						itn.putExtra(USERNAME_MESSAGE, username);
						startActivity(itn);
					}
				});
				builder.setNegativeButton("Okay", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						dialog.dismiss();
					}
				});
				final AlertDialog alert = builder.create();
				alert.show();
			}
		}
		else{
			runOnUiThread(new Runnable(){
				public void run(){
					Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	public void mainInfo(View view){
		Intent inn = new Intent(this, InfoActivity.class);
		inn.putExtra(USERNAME_MESSAGE, username);
		inn.putExtra(WITHINGS_UNAME, withings_username);
		inn.putExtra(WITHINGS_PWORD, withings_password);
		inn.putExtra(FITBIT_UNAME, fitbit_username);
		inn.putExtra(FITBIT_PWORD, fitbit_password);
		startActivity(inn);
	}
	 
	public void mainWithings(View view){
		Intent itt = new Intent(this, withings.apiaccess.SampleAPIMain.class);
		itt.putExtra(WITHINGS_UNAME, withings_username);
		itt.putExtra(WITHINGS_PWORD, withings_password);
		startActivity(itt);
	}
	
	public void mainFitbit(View view){
		//TODO
	}
	
	public void mainKcal(View view){
		//TODO
	}
	
	public void mainMsgUser(View view){
		Intent itn = new Intent(this, MsgUserActivity.class);
		itn.putExtra(USERNAME_MESSAGE, username);
		startActivity(itn);
	}
	
	public void mainMsgAll(View view){
		Intent ine = new Intent(this, MsgAllActivity.class);
		ine.putExtra(USERNAME_MESSAGE, username);
		startActivity(ine);
	}
	
	public void mainDining(View view) {
		Intent iDine = new Intent(this, gpsStuff.class);
		startActivity(iDine);
	}
	
	public void mainLogout(View view){
		stopService(new Intent(MainActivity.this, alarmservice.class));
		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/logout.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		List<NameValuePair> nvplist = new ArrayList<NameValuePair>();
		nvplist.add(new BasicNameValuePair("username", username));
		
		try {
			hp.setEntity(new UrlEncodedFormEntity(nvplist));
			Log.w("Logout", "Execute HTTP Post Request");
			hr = hc.execute(hp);
			he = hr.getEntity();
			is = he.getContent();
			Log.w("Logout", "Response Handling");
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
		Log.w("Logout", response);
		if(Integer.parseInt(response.substring(0, 1)) == 0){
			finish();
		}
		else{
			runOnUiThread(new Runnable(){
				public void run(){
					Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}	

}

