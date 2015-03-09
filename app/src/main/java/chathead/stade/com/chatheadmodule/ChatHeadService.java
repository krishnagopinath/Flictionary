package chathead.stade.com.chatheadmodule;

import android.app.Dialog;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.rebound.*;


public class ChatHeadService extends Service implements SpringListener {

    //clipboard init
    ClipboardManager clipboard;

    private WindowManager windowManager;
    private LinearLayout chatHead;
    private WindowManager.LayoutParams params;

    private static double TENSION = 100;
    private static double FRICTION = 20;

    private Spring springObj;
    private SpringSystem springSystem;


    //TOUCH HANDLERS
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    private int[] mPos = {10, 300};

    private int width;
    private int height;


    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        springSystem = SpringSystem.create();
        springObj = springSystem.createSpring();

        springObj.addListener(this);
        springObj.setSpringConfig(new SpringConfig(TENSION, FRICTION));

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        chatHead = (LinearLayout) inflater.inflate(R.layout.flickie, null, false);


        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = mPos[0];
        params.y = mPos[1];

        //get width
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        width = size.x;
        height = size.y;


        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();


                        return true;
                    case MotionEvent.ACTION_UP:
                        //for simulating "click"
                        //NEEDS A BETTER WAY
                        if (Math.abs(initialTouchX - event.getRawX()) < 5) {
                            chatHead.performClick();
                        } else {


                            if (params.y < (height - (height * 10 / 100))) {
                                chatHead.getLocationOnScreen(mPos);
                                springObj.setEndValue(1);
                            } else {
                                //if taken to the bottom, then destroy service
                                stopSelf();
                            }
                        }

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        windowManager.updateViewLayout(chatHead, params);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);

                        if (ChatVisibleModule.isActivityVisible()) {
                            Intent i = new Intent("stop");
                            sendBroadcast(i);
                        }
                        return true;
                }
                return false;
            }
        });


        chatHead.setClickable(true);
        chatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent floatIntent = new Intent(ChatHeadService.this, TransparentActivity.class);


                if (!ChatVisibleModule.isActivityVisible()) {

                    //THIS IS DEPRECATED AND MUST BE CHANGED
                    //MAYBE LATER
                    floatIntent.putExtra("word", clipboard.getText());
                    floatIntent.putExtra("yCoords", params.y);
                    floatIntent.putExtra("xCoords", params.x);
                    floatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(floatIntent);
                } else {
                    Intent i = new Intent("stop");
                    sendBroadcast(i);
                }


            }
        });

        windowManager.addView(chatHead, params);
    }


    @Override
    public void onSpringUpdate(Spring spring) {
        double value = spring.getCurrentValue();

        //init condition variables
        double xStart = 0;
        double xEnd = 0;
        double yStart = 0;
        double yEnd = 0;

        xStart = mPos[0];
        if (mPos[0] <= width / 2) {
            xEnd = 0.0;
        } else {
            xEnd = width;
        }

        yStart = mPos[1];
        yEnd = mPos[1];
        params.x = (int) (SpringUtil.mapValueFromRangeToRange(value, 0.0, 1.0, xStart, xEnd));
        params.y = (int) (SpringUtil.mapValueFromRangeToRange(value, 0.0, 1.0, yStart, yEnd));

        windowManager.updateViewLayout(chatHead, params);

        //BAD
        //VERY BAD
        //MUST FIND A BETTER WAY
        if (params.x == 0.0 || params.x == width) {
            springSystem = SpringSystem.create();
            springObj = springSystem.createSpring();
            springObj.setSpringConfig(new SpringConfig(TENSION, FRICTION));
            springObj.addListener(this);
        }

    }

    @Override
    public void onSpringAtRest(Spring spring) {

    }

    @Override
    public void onSpringActivate(Spring spring) {

    }

    @Override
    public void onSpringEndStateChange(Spring spring) {

    }
}