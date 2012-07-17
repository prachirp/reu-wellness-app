package android.wellness;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class NewAcctActivity extends Activity {

	private TextView note;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_acct);
		note = (TextView) findViewById(R.id.new_note);
		note.setVisibility(View.GONE);
	}

	public void newAcctDone(View view){

		EditText fname = (EditText)findViewById(R.id.new_fname);
		String firstname = fname.getText().toString();
		EditText lname = (EditText)findViewById(R.id.new_lname);
		String lastname = lname.getText().toString();
		EditText uname = (EditText)findViewById(R.id.new_uname);
		String username = uname.getText().toString();
		EditText pword = (EditText)findViewById(R.id.new_passwd);
		String password = pword.getText().toString();

		if(username.length() > 20){
			note.setText("Username can be up to 20 characters long.");
			note.setVisibility(View.VISIBLE);
			return;
		}
		else if(password.length() > 20){
			note.setText("Password can be up to 20 characters long.");
			note.setVisibility(View.VISIBLE);
			return;
		}

		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/newLogin.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		List<NameValuePair> nvplist = new ArrayList<NameValuePair>();
		nvplist.add(new BasicNameValuePair("firstname", firstname));
		nvplist.add(new BasicNameValuePair("lastname", lastname));
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
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String response = sb.toString();
		Log.w("Login", response);
		
		if(response.startsWith("0")){
			Log.w("Login", "TRUE");
			runOnUiThread(new Runnable(){
				public void run(){
					Toast.makeText(NewAcctActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
				}
			});
			//startActivity(new Intent(this, MainActivity.class));
			
			//sends a message containing username and uid to the MainActivity
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("android.wellness.MESSAGE", response);
			startActivity(intent);
			finish();
		}
		
		else{
			note.setText(response);
			note.setVisibility(View.VISIBLE);
		}
		
	}

	public void newAcctBack(View view){
		Intent intent = new Intent(this, StartupActivity.class);
		startActivity(intent);
		finish();
	}

}
