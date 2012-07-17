package android.wellness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

public class alarmservice extends Service{
	private static final int PERIOD = 60000;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		//set alarm for checking server
		sendBroadcast(new Intent(this, MyBroadcastReceiver.class));
		AlarmManager mgr=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    Intent i=new Intent(this, MyBroadcastReceiver.class);
	    PendingIntent pi = PendingIntent.getBroadcast(this, 0,
	                                              i, 0);
	    
	    mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                      SystemClock.elapsedRealtime()+60000,
	                      PERIOD,
	                      pi);
	}

}
