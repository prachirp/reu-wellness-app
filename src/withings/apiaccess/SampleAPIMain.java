/*
 *   Copyright 2011 Withings
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

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;


/**
 * Sample Activity providing basic access to Withings User data through the external API
 * UI is very simplistic & done fast (Don't be too hard on me)
 * Users info & measures are not stored on disk (you should consider doing this for real world implementations)
 * Users list is retrieved when the activity , current user measures are retrieved on demand
 * You should choose the data you want to be displayed on the spinner, then click on Update View button
 * You can also change the current user (if you have several on your account) using Change User button 
 * 
 */
public class SampleAPIMain extends Activity {
		
	private Spinner mSpinner;
	private ListView mListView;
	private ArrayList<String> mStringArray;
		
    private static final String sViewNames[] = {
    	"Users list", "Current user attributes", "Current user measures"
    };
    
    private static final int USERS_LIST_VIEW_POS = 0;
    private static final int CURRENT_USER_ATTRIB_VIEW_POS = 1;
    private static final int CURRENT_USER_MEAS_VIEW_POS = 2;	
	
	public static final String LOG_TAG = "WithingsSampleAPI";
	
	/*These are not valid account credentials. You have to set this values to a valid Withings account login & passwd*/
	private static String WITHINGS_ACCOUNT_LOGIN = "";
	private static String WITHINGS_ACCOUNT_PWD = "";

		
	/*Users list stored in this sample app only in volatile memory when the app goes foreground*/
	private Vector<WithingsUser> mUserList;
		
		
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/** UI specific - will (should) change depending on your implementation**/
		
		setContentView(android.wellness.R.layout.sample_basic_ui);
		
		Intent intent = getIntent();
		WITHINGS_ACCOUNT_LOGIN = intent.getStringExtra("android.wellness.WUNAME");
		WITHINGS_ACCOUNT_PWD = intent.getStringExtra("android.wellness.WPWORD");
		
		mSpinner = (Spinner) findViewById(android.wellness.R.id.spinnerChooseView);
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, sViewNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        
        mListView = (ListView) findViewById(android.wellness.R.id.listView);
        
        final Button updateButton = (Button)findViewById(android.wellness.R.id.updateViewButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
			
			//TODO Override
			public void onClick(View v) {
				if (isThereValidAccount())
					updateView(v.getContext(), mSpinner.getSelectedItemPosition());
				else
					generateWarningDialog(v.getContext(), "Invalid Account. Check your login and pwd").show();
			}
		}); 
        
        final Button changeUserButton = (Button)findViewById(android.wellness.R.id.changeUserButton);
        changeUserButton.setOnClickListener(new View.OnClickListener() {
			
			//TODO Override
			public void onClick(View v) {
				if (isThereValidAccount())
					launchUserChangeDialog();
				else
					generateWarningDialog(v.getContext(), "Invalid Account. Check your login and pwd").show();
			}
		}); 
        
        mStringArray = new ArrayList<String>();
        		
		/** End of UI - specific**/
		
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		
        /*API initialisation*/
		WithingsAPI api = WithingsAPI.getInstance(this); //singleton
		
		/*Login*/
		try {
			api.login(WITHINGS_ACCOUNT_LOGIN, WITHINGS_ACCOUNT_PWD);
		} catch (Exception e) {
			Log.w(LOG_TAG, "NO current user in withings app");
			e.printStackTrace();
			generateWarningDialog(this, "Could not login to " + WITHINGS_ACCOUNT_LOGIN).show();
		}
		
		/*Download users list*/
		new CloudManager.UsersDownloader(this, new CloudManager.UsersDownloader.DownloadCompletedUICallback() {
			
			//TODO Override
			public void onDownloadCompleted(Context context, Vector<WithingsUser> users) {
				mUserList = users;
				if (mUserList != null && mUserList.size() > 0){
					try {
						/*We need to fix an initial user*/
						WithingsAPI.getInstance(context).selectUser(mUserList.firstElement());
					} catch (Exception e) {
						Log.w(LOG_TAG, "Could not select user with id " + mUserList.firstElement().id);
						generateWarningDialog(context,"Could not select user with id " + mUserList.firstElement().id).show();
						e.printStackTrace();
					}
				} else {
					Log.w(LOG_TAG, "No users on account");
					generateWarningDialog(context, "No users on account").show();
				}
			}
		}).execute(0l);
		
	}
	
	//TODO Override
	protected void onPause() {
		super.onPause();
		
		WithingsAPI api = WithingsAPI.getInstance(this); //singleton
		api.logout();
	}
	
	private boolean isThereValidAccount(){
		return (mUserList != null && mUserList.size() > 0);
	}
	
	private void launchUserChangeDialog(){
		Dialog d = new UserChangeDialog(this, mUserList);
		d.show();
	}
	
	private void updateView(final Context context, int viewType){
		switch (viewType) {
		case USERS_LIST_VIEW_POS:
			if(mUserList != null){
				Log.d(LOG_TAG, "Get ALL Users query returned " + mUserList.size() + " users");
				buildStringArrayForUsers(context, mUserList);
			} else {
				Log.w(LOG_TAG, "Couldn't retrieve users");
				generateWarningDialog(context, "Couldn't retrieve users").show();
			}
			
			break;
		case CURRENT_USER_ATTRIB_VIEW_POS:
		{	
			WithingsUser currentUser = WithingsAPI.getInstance(context).getCurrentUser();  //Important API  call ;
			if (currentUser != null){
				Log.d(LOG_TAG, "Got current user id: " + currentUser.id);
				Vector<WithingsUser> oneItemArray = new Vector<WithingsUser>();
				oneItemArray.add(currentUser);
				buildStringArrayForUsers(context, oneItemArray);
			} else {
				Log.w(LOG_TAG, "NO current user in withings app");
				generateWarningDialog(context, "NO current user in withings app").show();
			}
			
			break;
		}
		case CURRENT_USER_MEAS_VIEW_POS:
		{	
			
			new CloudManager.BodyEntriesDownloader(context, new CloudManager.BodyEntriesDownloader.DownloadCompletedUICallback() {
				
				//TODO Override
				public void onDownloadCompleted(Vector<WeightValue> measures) {
					Log.w(LOG_TAG, "Get ALL Measures for current user query returned " + measures.size() + " measures");
					for (WeightValue weightMeasure : measures) {
						Log.d(LOG_TAG, "Got Measure with date " + weightMeasure.date + " weight:" + weightMeasure.weight);
					}
					buildStringArrayForWeightMeasures(context, measures);
				}
			}).execute(0l);
			
			break;
		}
		default:
			break;
		}
		
	}
	
	/*
	 * Sample (and simple & ugly) UI methods
	 * You should provide your own
	 */
	
	private AlertDialog generateWarningDialog(Context context, String message){
		return new AlertDialog.Builder(context)
		.setMessage(message)
		.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			//TODO Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		})
		.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			
			//TODO Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		})
		.create();
	}
	
	private void buildStringArrayForUsers(Context context, Vector<WithingsUser> usersVector){
		mStringArray.clear();
		StringBuilder builder = new StringBuilder(256);
		for (WithingsUser user : usersVector) {
			builder.delete(0, builder.capacity());
			builder.append("Id: ").append(user.id).append('\n');
			builder.append("Name: ").append(user.name).append('\n');
			builder.append("Is Public: ").append(user.isPublic).append('\n');
			
			mStringArray.add(builder.toString());
		}
		
		mListView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mStringArray));
	}
	
	private void buildStringArrayForWeightMeasures(Context context, Vector<WeightValue> weightMeasVector){
		mStringArray.clear();
		StringBuilder builder = new StringBuilder(256);
		for (WeightValue measure : weightMeasVector) {
			builder.delete(0, builder.capacity());
			builder.append("Date: ").append(measure.date).append('\n');
			builder.append("Weight: ").append(measure.weight).append('\n');
			
			mStringArray.add(builder.toString());
		}
		
		mListView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mStringArray));
	}
}