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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Timer;

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private ImageView chatHead;


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

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.ic_launcher);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        //get width
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        final int width = size.x;
        final int height = size.y;


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
                        }


                        if (params.y < (height - (height * 10 / 100))) {
                            //if x co-ordinates of floaty is greater than half the width
                            if (params.x > width / 2) {
                                //send it to the corner
                                params.x = width;
                            } else {
                                //or send it to the origin
                                params.x = 0;
                            }
                            windowManager.updateViewLayout(chatHead, params);
                        } else {
                            //if taken to the bottom, then destroy service
                            stopService(new Intent(chatHead.getContext(), ChatHeadService.class));
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


                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                    floatIntent.putExtra("yCoords", params.y);
                    floatIntent.putExtra("xCoords", params.x);
                    //THIS IS DEPRECATED AND MUST BE CHANGED
                    floatIntent.putExtra("word", clipboard.getText().toString());

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


}