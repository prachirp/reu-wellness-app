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

import android.app.*;
import android.app.AlertDialog.*;
import android.content.*;
import android.location.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StartupActivity extends Activity {

	public static final String EXTRA_MESSAGE = "android.wellness.MESSAGE";
	private TextView note;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		note = (TextView) findViewById(R.id.existing_note);
		note.setVisibility(View.GONE);
	}
	
	public void onResume(){
		super.onResume();
		
		
		//no internet prompt
		ConnectivityManager cm	= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		final boolean is3g = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		final boolean isWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		final boolean network = is3g|isWifi;
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		final boolean isConnected = activeNetwork!=null && activeNetwork.isAvailable() && activeNetwork.isConnected();
		
		if(!network){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.startup_internet_prompt);
			builder.setCancelable(false);
			builder.setTitle(R.string.startup_internet_title);
			builder.setPositiveButton("Alright", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int id){
    				dialog.dismiss();
    				enableNetworkSettings();
    			}
    		});
			final AlertDialog alert = builder.create();
			alert.show();
			
			//used to enable wifi inside application, but we don't know if to enable wifi or 3g network
			//WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			//wm.setWifiEnabled(true);
		}
		
		
		
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(!gpsEnabled){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.startup_gps_prompt);
			builder.setCancelable(false);
			builder.setTitle(R.string.startup_gps_title);
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int id){
    				dialog.dismiss();
    				enableLocationSettings();
    			}
    		});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialog, int id){
    				dialog.dismiss();
    			}
    		});
			final AlertDialog alert = builder.create();
			alert.show();
		}
		
		LocationProvider provider = lm.getProvider(LocationManager.GPS_PROVIDER);
	}
	
	private void enableNetworkSettings(){
		Intent wirelessIntent = new Intent(Settings.ACTION_SETTINGS);
		startActivity(wirelessIntent);
	}
	
	private void enableLocationSettings(){
		Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(locationIntent);
	}
	
	public void startupNewAcct(View view){
		Intent intent = new Intent(this, NewAcctActivity.class);
		startActivity(intent);
	}
	
public void existingAcctLogin(View view){
		
		EditText uname = (EditText)findViewById(R.id.existing_uname);
		String username = uname.getText().toString();
		EditText passwd = (EditText)findViewById(R.id.existing_passwd);
		String password = passwd.getText().toString();
		
		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/existLogin.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		List<NameValuePair> nvplist = new ArrayList<NameValuePair>(2);
		nvplist.add(new BasicNameValuePair("username", username));
		nvplist.add(new BasicNameValuePair("password", password));
		
		try {
			hp.setEntity(new UrlEncodedFormEntity(nvplist));
			Log.w("Login", "Execute HTTP Post Request");
			hr = hc.execute(hp);
			he = hr.getEntity();
			is = he.getContent();
			Log.w("Login", "Response Handling");
			
			isr = new InputStreamReader(is, "iso-8859-1");
			br = new BufferedReader(isr, 8);
			
			String line = null;
			line = br.readLine();
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
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String response = sb.toString();
		Log.w("Login", response);
		
		//correct uname, passwd combo
		if(response.startsWith("1")){
			Log.w("Login", "TRUE");
			runOnUiThread(new Runnable(){
				public void run(){
					Toast.makeText(StartupActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
				}
			});
			//sends a message containing username and uid to the MainActivity
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(EXTRA_MESSAGE, response);
			startActivity(intent);
			finish();
		}
		
		//incorrect passwd
		else if(response.startsWith("2")){
			Log.w("Login", "FALSE");
			
			note.setText("Incorrect password");
			note.setVisibility(View.VISIBLE);
		}
		
		//uname does not exist
		else if(response.startsWith("3")){
			Log.w("Login", "FALSE");
			
			note.setText("Unknown username");
			note.setVisibility(View.VISIBLE);
		}
		
		//something went wrong, please try again
		else if(response.startsWith("4")){
			Log.w("Login", "FALSE");
			
			note.setText("Something went wrong, please check your input.");
			note.setVisibility(View.VISIBLE);
		}
		
		//unable to query database
		else if(response.startsWith("5")){
			Log.w("Login", "FALSE");
			
			note.setText("Sorry, unable to query database.");
			note.setVisibility(View.VISIBLE);
		}
		
		//we received garbage
		else{
			Log.w("Login", "FALSE");
			
			note.setText("Error, unable to login.");
			note.setVisibility(View.VISIBLE);
		}
		
	}
	/*
	public void startupExistingAcct(View view){
		Intent intent = new Intent(this, ExistingAcctActivity.class);
		startActivity(intent);
	}
	*/
}
