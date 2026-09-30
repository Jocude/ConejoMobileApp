package com.example.conejojorge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Spike {
    Bitmap spike[] = new Bitmap[4];
    int spikeFrame = 0;
    // Tiempo (en segundos) que tarda un pincho en recorrer toda la pantalla
    static final float MIN_FALL_SECONDS = 1.5f;
    static final float MAX_FALL_SECONDS = 3f;
    int spikeX;
    float spikeY;
    float spikeVelocity; // píxeles por segundo
    Random random;
    public Spike(Context context){
        spike [0] = BitmapFactory.decodeResource(context.getResources(),R.drawable.spike0);
        spike [1] = BitmapFactory.decodeResource(context.getResources(),R.drawable.spike1);
        spike [2] = BitmapFactory.decodeResource(context.getResources(),R.drawable.spike2);
        spike [3] = BitmapFactory.decodeResource(context.getResources(),R.drawable.spike1);
        random = new Random();
        resetPosition();
    }
    public Bitmap getSpike(int spikeFrame){
        return  spike[spikeFrame];
    }
    public int getSpikeWidth(){
        return spike[0].getWidth();
    }
    public void resetPosition(){
        spikeX = random.nextInt(GameView.dWidth - getSpikeWidth());
        spikeY = -100 + random.nextInt(200)* -1;
        float fallSeconds = MIN_FALL_SECONDS + random.nextFloat() * (MAX_FALL_SECONDS - MIN_FALL_SECONDS);
        spikeVelocity = GameView.dHeight / fallSeconds;

    }

    public int getSpikeHeight() {
        return spike[0].getHeight();
    }
}
