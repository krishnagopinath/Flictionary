package chathead.stade.com.chatheadmodule;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TransparentActivity extends Activity {

    private BroadcastReceiver receiver;
    TextView txt;




    protected void onCreate(Bundle savedInstanceState) {
        ChatVisibleModule.activityResumed();

        IntentFilter filter = new IntentFilter();
        filter.addAction("stop");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //do something based on the intent's action
                if (intent.getAction() == "stop") {
                    finish();
                }
            }
        };
        registerReceiver(receiver, filter);

        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        txt = (TextView) findViewById(R.id.textView2);

        int yCo = intent.getIntExtra("yCoords", 0);
        String word = intent.getStringExtra("word");

        //check for null
        if (word != null && word.length() != 0) {
            //check for special characters and numbers
            Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(word);

            if (m.find()) {
                txt.setText(word);
            }
        }

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = yCo + 50;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatVisibleModule.activityPaused();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatVisibleModule.activityPaused();
    }
}
