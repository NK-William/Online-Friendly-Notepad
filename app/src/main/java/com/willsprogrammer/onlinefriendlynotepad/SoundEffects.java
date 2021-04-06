package com.willsprogrammer.onlinefriendlynotepad;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.View;

public class SoundEffects {

    private SoundPool soundPool;
    private int button_click_effect, favourite_bubble_effect, recyclerView_bubble_effect;

    //    private Context context;
    public SoundEffects(Context context) {
        // if the current phone used is on api >= 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        }

        button_click_effect = soundPool.load(context, R.raw.buttonclickeffect, 1);
        favourite_bubble_effect = soundPool.load(context, R.raw.favouritebubbleeffect, 1);
        recyclerView_bubble_effect = soundPool.load(context, R.raw.recyclerviewbubbleeffect, 1);
    }

    public void playSound(int resource_id) {
        switch (resource_id) {
            case R.id.login_button:
                // play button click
                soundPool.play(button_click_effect, 1, 1, 0, 0, 1);
//                soundPool.autoPause();
                break;

            case R.id.card_view:
                // play recyclerView bubble
                soundPool.play(recyclerView_bubble_effect, 1, 1, 0, 0, 1);
//                soundPool.autoPause();
                return;

            case R.id.item_favourite_image:
                // play favourite bubble()
                soundPool.play(favourite_bubble_effect, 1, 1, 0, 0, 1);
//                soundPool.autoPause();
        }
    }

    public void releaseSoundPool(){
        soundPool.release();
        soundPool = null;
    }
}
