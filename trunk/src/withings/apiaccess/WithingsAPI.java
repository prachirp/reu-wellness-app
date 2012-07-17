/*
 *   Copyright 2011 Daniel Cachapa, Withings
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package withings.apiaccess;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Vector;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Encapsulates external API access implementation
 * It is a Singleton (see getInstance)
 */
public class WithingsAPI {
	private static final String PREFERENCES_FILE = "withings";
	
	private static WithingsAPI instance = null;
	
	private Context context;
	private String email;
	private String hash;
	private WithingsUser user;
	private long callbackId;
	private long lastUpdate;
	
	private WithingsAPI(Context context) {
		this.context = context;
		loadUser();
		// Load the last update
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
		lastUpdate = preferences.getLong("lastUpdate", WeightValue.DATE_NOT_RECORDED);
	}
	
	public static WithingsAPI getInstance(Context context) {
		if (instance == null) {
			instance = new WithingsAPI(context);
		}
		return instance;
	}
	
	public void login(String email, String password) throws Exception {
		this.email = email;
		// In order to use the service, we need to generate a hash of the password
		// The hash is generated in four steps described in http://www.withings.com/en/api/bodyscale#crypto
		
		// Step one: Get a one-time "magic string"
		String magicString = getMagicString();
		
		// Step two: Get the MD5 hash of the password
		String passwordHash = md5Hash(password);
		
		// Step three: Concatenate the email, password hash and magic string, separated by a colon
		String concat = email + ":" + passwordHash + ":" + magicString;
		
		// Step four: Get the MD5 hash of the above string
		hash = md5Hash(concat);
	}
	
	public void logout() {
		user = null;
		saveUser();
	}
	
	public Vector<WithingsUser> getUsersList() throws Exception {
		String request = "http://wbsapi.withings.net/account?action=getuserslist&email=" + email + "&hash=" + hash;
		String response = performRequest(request);
		
		// Decode JSon object
		JSONObject json = new JSONObject(response);
		JSONArray JSONusers = json.getJSONObject("body").getJSONArray("users");
		JSONObject JSONuser;
		Vector<WithingsUser> users = new Vector<WithingsUser>();
		String name;
		int size = JSONusers.length();
		for (int i = 0; i < size; i++) {
			JSONuser = JSONusers.getJSONObject(i);
			name = JSONuser.getString("firstname") + " " + JSONuser.getString("lastname");
			users.add(new WithingsUser(JSONuser.getLong("id"), name, JSONuser.getString("publickey"), JSONuser.getInt("ispublic")));
		}
		return users;
	}
	
	public void selectUser(WithingsUser user) throws Exception {
		this.user = user;
		saveUser();
		// Authorize the user
		if ((user.isPublic & 1) != 1) {
			authorize();
		}
	}
	
	public WithingsUser getCurrentUser(){
		return this.user;
	}
	
	public Vector<WeightValue> getAllValues() throws Exception {
		return getValues(WeightValue.DATE_NOT_RECORDED);
	}
	
	public Vector<WeightValue> getNewValues() throws Exception {
		return getValues(lastUpdate);
	}
	
	public Vector<WeightValue> getValues(long startDate) throws Exception {
		String startDateString = "";
		if (startDate != WeightValue.DATE_NOT_RECORDED) {
			startDateString = "&startdate=" + Long.toString(startDate);
		}
		
		String request = "http://wbsapi.withings.net/measure?action=getmeas&userid=" + user.id +
			"&publickey=" + user.publicKey +
			startDateString +
			"&devtype=1";			// Only values from the scale
		String response = performRequest(request);
		
		// Decode JSon object
		JSONObject json = new JSONObject(response);
		JSONArray JSONvalues = json.getJSONObject("body").getJSONArray("measuregrps");
		JSONObject JSONvalue;
		JSONArray JSONtypes;
		JSONObject JSONtype;
		float weight;
		long date;
		Vector<WeightValue> weightValues = new Vector<WeightValue>();
		int size = JSONvalues.length();
		for (int i = 0; i < size; i++) {
			JSONvalue = JSONvalues.getJSONObject(i);
			if (JSONvalue.getInt("category") != 1) {	// Make sure it's a measurement (=1)
				continue;
			}
			date = JSONvalue.getLong("date");
			
			JSONtypes = JSONvalue.getJSONArray("measures");
			for (int j = 0; j < JSONtypes.length(); j++) {
				JSONtype = JSONtypes.getJSONObject(j);
				if (JSONtype.getInt("type") == 1) {		// Weight value
					weight = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
					weightValues.add(new WeightValue(date, weight));
				}
			}
		}
		if (weightValues.size() > 0) {
			lastUpdate = weightValues.firstElement().date;
			Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
			editor.putLong("lastUpdate", lastUpdate);
			editor.commit();
		}
		return weightValues;
	}
	
	public void subscribe3rdParty(String registration) throws Exception {
		String request = "http://yourdomain.net/withings/register.php" +
			"?user_id=" + user.id +
			"&public_key=" + user.publicKey +
			"&reg_id=" + registration;
		
		String response = performRequest(request);
		long callbackId = new JSONObject(response).getLong("id");
		String callbackUrl = "http://yourdomain.net/withings/callback.php?id=" + callbackId;
		try {
			subscribe(callbackUrl);
			this.callbackId = callbackId;
			saveUser();
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void subscribe(String callbackUrl) throws Exception {
		String request = "http://wbsapi.withings.net/notify?action=subscribe" +
			"&userid=" + user.id +
			"&publickey=" + user.publicKey +
			"&callbackurl=" + URLEncoder.encode(callbackUrl) +
			"&devtype=1" +			// Only values from the scale
			"&comment=" + URLEncoder.encode("Your APP's name or message");
		Log.d("cloud", request);
		performRequest(request);
	}
	
	public void unsubscribe() throws Exception {
		String request = "http://yourdomain.net/withings/unregister.php" +
			"?id=" + callbackId;
		Log.d("cloud", "Unregistering: " + request);
		String result = performRequest(request);
		Log.d("cloud", "Result: " + result);
		// We ignore the return code because it doesn't matter.
		// Even if the unsubscription failed for some reason, the user will be unsubscribed
		// the next time he tries to weigh himself.
		
		// Finally reset the callbackId to -1 to mark the user as unsubscribed
		callbackId = -1;
	}
	
	public boolean isLoggedIn() {
		return user != null;
	}
	
	public boolean isSubscribed() {
		if (!isLoggedIn()) {
			return false;
		}
		return callbackId != -1;
	}
	
	public String getUserName() {
		return user.name;
	}
	
	
	private String getMagicString() throws Exception {
		String request = "http://wbsapi.withings.net/once?action=get";
		String response = performRequest(request);
		
		// Decode the magic string from the JSon response
		JSONObject json = new JSONObject(response);
		return json.getJSONObject("body").getString("once");
	}
	
	private void authorize() throws Exception {
		String request = "http://wbsapi.withings.net/user?action=update&userid=" + user.id +
			"&publickey=" + user.publicKey +
			"&ispublic=" + (user.isPublic | 1);
		performRequest(request);
	}
	
	private String md5Hash(String string) throws Exception {
		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		digest.update(string.getBytes());
		byte messageDigest[] = digest.digest();

		// Get the Hex String
		BigInteger bigInt = new BigInteger(1, messageDigest);
		return bigInt.toString(16);
	}
	
	private String performRequest(String url) throws Exception {
		// Perform the request
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);
		
		// Translate the response into a string
		String result = "";
		try {
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				str.append(line + "\n");
			}
			in.close();
			result = str.toString();
		} catch (Exception ex) {
			result = "Error";
		}
		
		// Decode JSon object
		JSONObject json = new JSONObject(result);
		// Make sure that the request was successful
		if (json.getInt("status") != 0) {
			throw new Exception("Withings error: " + json.getInt("status"));
		}
		
		return result;
	}
	
	private void saveUser() {
		Editor editor = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
		if(user == null) {
			editor.clear();
		}
		else {
			editor.putLong("id", user.id);
			editor.putString("name", user.name);
			editor.putString("publicKey", user.publicKey);
			editor.putLong("callbackId", callbackId);
		}
		editor.commit();
	}
	
	private void loadUser() {
		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
		if (!preferences.contains("name")) {
			user = null;
			return;
		}
		
		long id = preferences.getLong("id", 0);
		String name = preferences.getString("name", null);
		String publicKey = preferences.getString("publicKey", null);
		callbackId = preferences.getLong("callbackId", -1);
		user = new WithingsUser(id, name, publicKey, 0);
	}
}
