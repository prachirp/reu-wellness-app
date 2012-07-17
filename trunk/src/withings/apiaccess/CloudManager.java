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

import java.util.Vector;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class CloudManager extends BroadcastReceiver {

	public static void registerC2DM(Context context) {
		Log.d("cloud", "Trying to register C2DM");
		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0)); 
		registrationIntent.putExtra("sender", "your c2dm account login");
		context.startService(registrationIntent);
	}

	public static void unregisterC2DM(Context context) {
		Log.d("cloud", "Trying to unregister from C2DM");
		Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0)); 
		context.startService(unregIntent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			handleMessage(context, intent);
		}
	}

	private void handleRegistration(Context context, Intent intent) {
		String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {
			Log.d("cloud", "Registration failed");
		} else if (intent.getStringExtra("unregistered") != null) {
			Log.d("cloud", "Unregistered from C2DM");
			try {
				WithingsAPI.getInstance(context).unsubscribe();
			} catch (Exception e) {
				Log.d("cloud", "Error unregistering: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		} else if (registration != null) {
			Log.d("cloud", "Registred C2DM with id: " + registration);
			try {
				WithingsAPI.getInstance(context).subscribe3rdParty(registration);
			} catch (Exception e) {
				Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
				return;
			}
		}
	}

	private void handleMessage(Context context, Intent intent) {
		Log.d("cloud", "Got a message from C2DM");
		String startdateS = (String) intent.getStringExtra("startdate");
		long startdate = -1;
		if (startdateS != null && !startdateS.equals("")) {
			startdate = Long.valueOf(startdateS).longValue();
		}
		Log.d("cloud", "Startdate is " + startdate + " which means " + startdate);
		new BodyEntriesDownloader(context, null).execute(startdate);
	}
	
	
	protected static class BodyEntriesDownloader extends
			AsyncTask<Long, Integer, String> {
		
		public static interface DownloadCompletedUICallback{
			public void onDownloadCompleted(Vector<WeightValue> measures);
		}
		
		private Context mContext;
		Vector<WeightValue> mResultMeasures;
		DownloadCompletedUICallback mCb;
		ProgressDialog mProgressDialog;

		public BodyEntriesDownloader(Context context,  DownloadCompletedUICallback cb) {
			mContext = context;
			mCb = cb;
			mProgressDialog = new ProgressDialog(context);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		protected String doInBackground(Long... params) {
			long startdate = params[0];
			try {
				WithingsAPI withingsAPI = WithingsAPI.getInstance(mContext);
				if (startdate != -1) {
					mResultMeasures = withingsAPI.getValues(startdate);
				}
				else {
					mResultMeasures = withingsAPI.getNewValues();
				}

			} catch (Exception e) {
				// Don't show a notification if nothing has been inserted
				e.printStackTrace();
				return null;
			}

			String description;
			switch (mResultMeasures.size()) {
			case 0:
				// Don't show a notification if nothing has been inserted
				return null;
			case 1:
				description = String.format("last weigh-in: %f kg", mResultMeasures.lastElement().weight);
				break;
			default:
				description = String.format("retrieved %d weight measures", mResultMeasures.size());
			}

			return description;
		}

		protected void onPostExecute(String description) {
			if (description == null) {
				// Don't show a notification if nothing has been inserted
				return;
			}
			//Icon to be replaced by your own
			Notification notification = new Notification(android.R.drawable.ic_notification_overlay,
				description, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			Intent notificationIntent = new Intent(mContext, SampleAPIMain.class);
			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
					notificationIntent, 0);

			notification.setLatestEventInfo(mContext, "Withings", description, contentIntent);

			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(ns);
			notificationManager.notify(0, notification);
			
			mProgressDialog.dismiss();
			
			if (mCb != null){
				mCb.onDownloadCompleted(mResultMeasures);
			}
		}
	}
	
	protected static class UsersDownloader extends
	AsyncTask<Long, Integer, Boolean> {
		
		public static interface DownloadCompletedUICallback{
			public void onDownloadCompleted(Context context, Vector<WithingsUser> users);
		}

		private Context mContext;
		Vector<WithingsUser> mResultUsersList;
		ProgressDialog mProgressDialog;
		DownloadCompletedUICallback mCb;
	
		public UsersDownloader(Context context, DownloadCompletedUICallback cb) {
			mContext = context;
			mResultUsersList = null;
			mProgressDialog = new ProgressDialog(context);
			mCb = cb;
		}
	
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}
		
		protected Boolean doInBackground(Long... params) {
			try {
				WithingsAPI withingsAPI = WithingsAPI.getInstance(mContext);
				mResultUsersList = withingsAPI.getUsersList();
		
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		
			return true;
		}
		
		protected void onPostExecute(Boolean b) {
	
			mProgressDialog.dismiss();
			
			if (mCb != null) mCb.onDownloadCompleted(mContext, mResultUsersList);
			
		}
	}
}
