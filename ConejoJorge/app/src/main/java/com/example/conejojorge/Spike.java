package com.example.conejojorge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

public class Spike {
    Bitmap spike[] = new Bitmap[4];
    int spikeFrame = 0;
    int spikeX, spikeY, spikeVelocity;
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
        spikeY = -200 + random.nextInt(600)* -1;
        spikeVelocity = 5 *random.nextInt(16);

    }

    public int getSpikeHeight() {
        return spike[0].getHeight();
    }
}
