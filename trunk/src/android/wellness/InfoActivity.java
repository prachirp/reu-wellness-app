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
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;


public class InfoActivity extends Activity {
	
	private CheckBox withings;
	private TextView wUnameText;
	private EditText wUname;
	private TextView wPwordText;
	private EditText wPword;
	
	private OnClickListener wListener;
	
	private CheckBox fitbit;
	private TextView fUnameText;
	private EditText fUname;
	private TextView fPwordText;
	private EditText fPword;
	
	private OnClickListener fListener;
	
	public String username;
	public String wun;
	public String wpw;
	public String fun;
	public String fpw;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);
		
		Intent intent = getIntent();
		username = intent.getStringExtra("android.wellness.USERNAME");
		wun = intent.getStringExtra("android.wellness.WUNAME");
		wpw = intent.getStringExtra("android.wellness.WPWORD");
		fun = intent.getStringExtra("android.wellness.FUNAME");
		fpw = intent.getStringExtra("android.wellness.FPWORD");
		
		
		withings = (CheckBox)findViewById(R.id.info_wCheck);
		wUnameText = (TextView)findViewById(R.id.info_wUname_text);
		wUname = (EditText)findViewById(R.id.info_wUname);
		wPwordText = (TextView)findViewById(R.id.info_wPword_text);
		wPword = (EditText)findViewById(R.id.info_wPword);
		wUnameText.setVisibility(View.GONE);
		wUname.setVisibility(View.GONE);
		wPwordText.setVisibility(View.GONE);
		wPword.setVisibility(View.GONE);
		wUname.setText(wun);
		wPword.setText(wpw);
		
		wListener = new OnClickListener(){
			public void onClick(View v){
				if(!withings.isChecked()){
					wUnameText.setVisibility(View.GONE);
					wUname.setVisibility(View.GONE);
					wPwordText.setVisibility(View.GONE);
					wPword.setVisibility(View.GONE);
				}
				else{
					wUnameText.setVisibility(View.VISIBLE);
					wUname.setVisibility(View.VISIBLE);
					wPwordText.setVisibility(View.VISIBLE);
					wPword.setVisibility(View.VISIBLE);
				}
			}
		};
		
		fitbit = (CheckBox)findViewById(R.id.info_fCheck);
		fUnameText = (TextView)findViewById(R.id.info_fUname_text);
		fUname = (EditText)findViewById(R.id.info_fUname);
		fPwordText = (TextView)findViewById(R.id.info_fPword_text);
		fPword = (EditText)findViewById(R.id.info_fPword);
		fUnameText.setVisibility(View.GONE);
		fUname.setVisibility(View.GONE);
		fPwordText.setVisibility(View.GONE);
		fPword.setVisibility(View.GONE);
		fUname.setText(fun);
		fPword.setText(fpw);
		
		fListener = new OnClickListener(){
			public void onClick(View v){
				if(!fitbit.isChecked()){
					fUnameText.setVisibility(View.GONE);
					fUname.setVisibility(View.GONE);
					fPwordText.setVisibility(View.GONE);
					fPword.setVisibility(View.GONE);
				}
				else{
					fUnameText.setVisibility(View.VISIBLE);
					fUname.setVisibility(View.VISIBLE);
					fPwordText.setVisibility(View.VISIBLE);
					fPword.setVisibility(View.VISIBLE);
				}
			}
		};
		
		withings.setOnClickListener(wListener);
		fitbit.setOnClickListener(fListener);
		
	}
	
	public void infoDone(View view){
		String wu = null;
		String wp = null;
		String fu = null;
		String fp = null;
		if(withings.isChecked()){
			wu = wUname.getText().toString();
			wp = wPword.getText().toString();
		}
		if(fitbit.isChecked()){
			fu = fUname.getText().toString();
			fp = fPword.getText().toString();
		}

		HttpClient hc = new DefaultHttpClient();
		HttpPost hp = new HttpPost("http://129.252.131.156/info.php");
		HttpResponse hr = null;
		HttpEntity he = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		List<NameValuePair> nvplist = new ArrayList<NameValuePair>();
		nvplist.add(new BasicNameValuePair("uname", username));
		nvplist.add(new BasicNameValuePair("wuname", wu));
		nvplist.add(new BasicNameValuePair("wpword", wp));
		nvplist.add(new BasicNameValuePair("funame", fu));
		nvplist.add(new BasicNameValuePair("fpword", fp));


		try {
			hp.setEntity(new UrlEncodedFormEntity(nvplist));
			Log.w("Info", "Execute HTTP Post Request");
			hr = hc.execute(hp);
			he = hr.getEntity();
			is = he.getContent();
			Log.w("Info", "Response Handling");
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
		Log.w("Info", response);

		if(Integer.parseInt(response.substring(0, 1)) == 0){
			finish();
		}
		else{
			runOnUiThread(new Runnable(){
				public void run(){
					Toast.makeText(InfoActivity.this, response, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

}
