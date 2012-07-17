/*
incomingmessageview.java
 */

package android.wellness;


import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.TextView;

/**
 * This activity is run as the click activity for {@link IncomingMessage}.
 * When it comes up, it also clears the notification, because the "message"
 * has been "read."
 */
public class ServiceprojActivity extends Activity {
    /**
     * Extra that can be supplied to Intent: who the message is from.
     */
    static final public String KEY_FROM = "from";
    /**
     * Extra that can be supplied to Intent: the message that was sent.
     */
    static final public String KEY_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_message_view);

        // Fill in the message content.
        ((TextView)findViewById(R.id.from)).setText(
                getIntent().getCharSequenceExtra(KEY_FROM));
        ((TextView)findViewById(R.id.message)).setText(
                getIntent().getCharSequenceExtra(KEY_MESSAGE));

        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // cancel the notification that we started in IncomingMessage
        nm.cancel(R.string.imcoming_message_ticker_text);
    }
}
