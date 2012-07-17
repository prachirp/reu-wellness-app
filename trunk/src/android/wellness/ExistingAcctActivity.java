package android.wellness;

import java.io.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class ExistingAcctActivity extends Activity {
	
	private TextView note;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.existing_acct);
		note = (TextView) findViewById(R.id.existing_note);
		note.setVisibility(View.GONE);
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
					Toast.makeText(ExistingAcctActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
				}
			});
			//sends a message containing username and uid to the MainActivity
			//startActivity(new Intent(this, MainActivity.class));
			
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("android.wellness.MESSAGE", response);
			startActivity(intent);			
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
	
	public void existingAcctBack(View view){
		Intent intent = new Intent(this, StartupActivity.class);
		startActivity(intent);
	}
}
