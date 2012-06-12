package com.example.myfirstapp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LocateNearestActivity extends Activity {
	public static final String EXTRA_APPLICATION_ID = "com.android.browser.application_id";
    private TextView mLatLng;
    private TextView mAddress;
    private LocationManager mLocationManager;
    private Handler mHandler;
    private boolean mGeocoderAvailable;
    private boolean mUseFine;
    private boolean mUseBoth;
    private Button mFineProviderButton;
    private Button mBothProviderButton;


    // UI handler codes.
    private static final int UPDATE_ADDRESS = 1;
    private static final int UPDATE_LATLNG = 2;
    
    
    private static final int TEN_SECONDS = 10000;
    private static final int TEN_METERS = 10;
    
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searched);
        
        mLatLng = (TextView) findViewById(R.id.latlng);
        mAddress = (TextView) findViewById(R.id.address);
        mFineProviderButton = (Button) findViewById(R.id.provider_fine);
        mBothProviderButton = (Button) findViewById(R.id.provider_both);


        mGeocoderAvailable =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent();

        // Handler for updating text fields on the UI like the lat/long and address.
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_ADDRESS:
                        mAddress.setText((String) msg.obj);
                        break;
                    case UPDATE_LATLNG:
                        mLatLng.setText((String) msg.obj);
                        break;
                }
            }
        };
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    
    @Override
    protected void onStart() {
        super.onStart();

        // Check if the GPS setting is currently enabled on the device.
        // This verification should be done during onStart() because the system calls this method
        // when the user returns to the activity, which ensures the desired location provider is
        // enabled each time the activity resumes from the stopped state.
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            // Build an alert dialog here that requests that the user enable
            // the location services, then when the user clicks the "OK" button,
            // call enableLocationSettings()
            new EnableGpsDialogFragment().show(getFragmentManager(), "enableGpsDialog");
        }
    }
     // Method to launch Settings
    private void enableLocationSettings() {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
     }
        
     @Override
    protected void onResume() {
            super.onResume();
            setup();
     }
    public void useFineProvider(View v) {
            mUseFine = true;
            setup();
     }
 // Callback method for the "both providers" button.
    public void useCoarseFineProviders(View v) {
        mUseFine = false;
        mUseBoth = true;
        setup();
    }
        
     // Set up fine and/or coarse location providers depending on whether the fine provider or
        // both providers button is pressed.
     private void setup() {
            Location gpsLocation = null;
            Location networkLocation = null;
            mLocationManager.removeUpdates(listener);
            mLatLng.setText(R.string.unknown);
            mAddress.setText(R.string.unknown);
            // Get fine location updates only.
            if (mUseFine) {
                mFineProviderButton.setBackgroundResource(R.drawable.button_active);
                mBothProviderButton.setBackgroundResource(R.drawable.button_inactive);
                // Request updates from just the fine (gps) provider.
                gpsLocation = requestUpdatesFromProvider(
                        LocationManager.GPS_PROVIDER, R.string.not_support_gps);
                // Update the UI immediately if a location is obtained.
                if (gpsLocation != null) updateUILocation(gpsLocation);
            } else if (mUseBoth) {
                // Get coarse and fine location updates.
                mFineProviderButton.setBackgroundResource(R.drawable.button_inactive);
                mBothProviderButton.setBackgroundResource(R.drawable.button_active);
                // Request updates from both fine (gps) and coarse (network) providers.
                gpsLocation = requestUpdatesFromProvider(
                        LocationManager.GPS_PROVIDER, R.string.not_support_gps);
                networkLocation = requestUpdatesFromProvider(
                        LocationManager.NETWORK_PROVIDER, R.string.not_support_network);

                // If both providers return last known locations, compare the two and use the better
                // one to update the UI.  If only one provider returns a location, use it.
                if (gpsLocation != null && networkLocation != null) {
                    updateUILocation(gpsLocation);
                } else if (gpsLocation != null) {
                    updateUILocation(gpsLocation);
                } else if (networkLocation != null) {
                    updateUILocation(networkLocation);
                }
            }
        }
     private Location requestUpdatesFromProvider(final String provider, final int errorResId) {
            Location location = null;
            if (mLocationManager.isProviderEnabled(provider)) {
                mLocationManager.requestLocationUpdates(provider, TEN_SECONDS, TEN_METERS, listener);
                location = mLocationManager.getLastKnownLocation(provider);
            } else {
                Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
            }
            return location;
        }
        
     private void doReverseGeocoding(Location location) {
            // Since the geocoding API is synchronous and may take a while.  You don't want to lock
            // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
            (new ReverseGeocodingTask(this)).execute(new Location[] {location});
        }

     private void updateUILocation(Location location) {
            // We're sending the update to a handler which then updates the UI with the new
            // location.
            Message.obtain(mHandler,
                    UPDATE_LATLNG,
                    location.getLatitude() + ", " + location.getLongitude()).sendToTarget();

            // Bypass reverse-geocoding only if the Geocoder service is available on the device.
            if (mGeocoderAvailable) doReverseGeocoding(location);
        }
     
      
     LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // A new location update is received.  Do something useful with it.  Update the UI with
                // the location update.
                updateUILocation(location);
            }
            @Override
            public void onProviderDisabled(String provider) {            }

            @Override
            public void onProviderEnabled(String provider) {            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {            }
        };
        
        public void BrowserSearch(View view ) {
        	TextView addressText = (TextView) findViewById(R.id.address);
        	String addressNow = addressText.getText().toString();
        	String strURL = "http://www.healthydiningfinder.com/SearchList.aspx?Code=" + addressNow;
        	Intent browsingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strURL));
        	startActivity(browsingIntent);
        	
        }
  private class EnableGpsDialogFragment extends DialogFragment {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.enable_gps)
                        .setMessage(R.string.enable_gps_dialog)
                        .setPositiveButton(R.string.enable_gps, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                enableLocationSettings();
                            }
                        })
                        .create();
            }
        }
        // AsyncTask encapsulating the reverse-geocoding API.  Since the geocoder API is blocked,
        // we do not want to invoke it from the UI thread.
  private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
            Context mContext;

      public ReverseGeocodingTask(Context context) {
                super();
                mContext = context;
            }

            @Override
            protected Void doInBackground(Location... params) {
                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

                Location loc = params[0];
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Update address field with the exception.
                    Message.obtain(mHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
                }
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    // Format the first line of address (if available), city, and country name.
                    String addressText = String.format("%s, %s, %s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                            address.getLocality(),
                            address.getCountryName());
                    // Update address field on UI.
                    Message.obtain(mHandler, UPDATE_ADDRESS, addressText).sendToTarget();
                }
                return null;
            }
        }
        // Stop receiving location updates whenever the Activity becomes invisible.
        @Override
        protected void onStop() {
            super.onStop();
            mLocationManager.removeUpdates(listener);
        }
        
       
}


        
        
        
/*     private void updateUIrestaurant(Location location) {
   	 //for each item in the array see sort by which is closer??
   	 double tempLat = location.getLatitude();
   	 double tempLong = location.getLongitude();
   		 

   	 
   	 Message.obtain(mHandler,
                UPDATE_RESTAURANT,
                newRestaurant).sendToTarget();
    }
*/  

        