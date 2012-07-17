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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MsgAllActivity extends Activity {

	private EditText message;
	private String fromUname;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msg_all);
		
		Intent intent = getIntent();
		fromUname = intent.getStringExtra("android.wellness.USERNAME");
		
		message = (EditText)findViewById(R.id.msg_all_msg);
	}
	
	public void msgAllSend(View view){
		
		String msg = message.getText().toString();

		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/msgAll.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		List<NameValuePair> nvplist = new ArrayList<NameValuePair>();
		nvplist.add(new BasicNameValuePair("fromName", fromUname));
		nvplist.add(new BasicNameValuePair("msg", msg));
		
		try {
			hp.setEntity(new UrlEncodedFormEntity(nvplist));
			Log.w("MsgAll", "Execute HTTP Post Request");
			hr = hc.execute(hp);
			he = hr.getEntity();
			is = he.getContent();
			Log.w("MsgAll", "Response Handling");
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
		Log.w("MsgAll", response);
		if(Integer.parseInt(response.substring(0, 1)) == 0){
			finish();
		}
		else{
			runOnUiThread(new Runnable(){
				public void run(){
					Toast.makeText(MsgAllActivity.this, response, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
