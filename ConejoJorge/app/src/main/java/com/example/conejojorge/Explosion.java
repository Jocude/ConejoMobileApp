package com.example.conejojorge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Explosion {
    Bitmap[] explosion = new Bitmap[3];
    int explosionFrame = 0;
    int explosionX, explosionY;

    public Explosion(Context context){
        explosion[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode0);
        explosion[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode1);
        explosion[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.explode2);

    }
    public Bitmap getExplosion(int explosionFrame) {
        if (explosionFrame < 0 || explosionFrame >= explosion.length) {
            explosionFrame = explosion.length - 1; // Establece el último cuadro en caso de índice fuera de rango
        }
        return explosion[explosionFrame];
    }


}
