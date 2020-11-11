package com.example.piano.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.midi.MidiOutputPort;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.piano.model.Key;

import java.util.ArrayList;

public class PianoView extends View {
    private static final int NUMBER_OF_KEYS = 14;
    private Paint white, black, yellow;
    private ArrayList<Key> whites, blacks;
    private int keyWidth, keyHeight;
    private com.ssaurel.piano.AudioSoundPlayer soundPlayer;

    public PianoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        black = new Paint();
        black.setColor(Color.BLACK);
        black.setStyle(Paint.Style.FILL);

        white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);

        yellow = new Paint();
        yellow.setColor(Color.YELLOW);
        yellow.setStyle(Paint.Style.FILL);
        soundPlayer = new com.ssaurel.piano.AudioSoundPlayer(context);

        whites = new ArrayList<Key>();
        blacks = new ArrayList<Key>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        keyWidth = w / NUMBER_OF_KEYS;
        keyHeight = h;

        int countBlackKey = 15;
        //Khoi tao mang key
        for(int i = 0; i < NUMBER_OF_KEYS; i++) {
            int left = i*keyWidth;
            int right = left + keyWidth;

            //neu la phim trang cuoi cung
            if(i == NUMBER_OF_KEYS - 1) {
                right = w;
            }
            RectF rect = new RectF(left, 0, right, h);
            whites.add(new Key(i+1, rect, false));

            //Khoi tao mang phim den: black key
            if(i!=0 && i!=3 && i!=7 && i!=10) {
                rect = new RectF((float)(i-1)*keyWidth + keyWidth*0.75f, 0, (float)i*keyWidth + keyWidth*0.25f, 0.67f*keyHeight);
                blacks.add(new Key(countBlackKey++, rect, false));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Ve 14 phim trang
        for(Key key : whites) {
            canvas.drawRect(key.rect, key.down ? yellow : white);
        }

        //Ve 13 duong chia cac phim trang
        for(int i = 1; i < NUMBER_OF_KEYS; i++) {
            canvas.drawLine(i*keyWidth, 0, i*keyWidth, keyHeight, black);
        }

        for(Key key : blacks) {
            canvas.drawRect(key.rect, key.down?yellow:black);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean isDownAction = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE;
        for(int touchIndex = 0; touchIndex < event.getPointerCount(); touchIndex++) {
            float x = event.getX(touchIndex);
            float y = event.getY(touchIndex);

            Key k = keyForCoords(x, y);

            if(k != null) {
                k.down = isDownAction;
            }

            ArrayList<Key> tmp = new ArrayList<>(whites);
            tmp.addAll(blacks);

            for(Key key : tmp) {
                if(key.down) {
                    if(!soundPlayer.isNotePlaying(key.sound)) {
                        soundPlayer.playNote(key.sound);
                        invalidate();
                    } else {
                        releaseKey(key);
                    }
                } else  {
                    soundPlayer.stopNote(key.sound);
                    releaseKey(key);
                }
            }

        }
        return true;
    }

    private Key keyForCoords(float x, float y) {
        for(Key k : blacks) {
            if(k.rect.contains(x, y)) {
                return k;
            }
        }
        for(Key k : whites) {
            if(k.rect.contains(x, y)) {
                return k;
            }
        }
        return null;
    }

    private void releaseKey(final Key k) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                k.down = false;
                handler.sendEmptyMessage(0);
            }
        }, 100);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            invalidate();
        }
    };
}
