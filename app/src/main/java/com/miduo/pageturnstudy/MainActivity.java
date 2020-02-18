package com.miduo.pageturnstudy;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private  PageTurnView pageView;
    private List<Bitmap> lists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageView=findViewById(R.id.pageView);
        lists=new ArrayList<>();
        initBitmap();
        pageView.post(new Runnable() {
            @Override
            public void run() {
                pageView.setBitmaps(lists);
            }
        });

    }

    private void initBitmap()
    {
        lists.add(BitmapFactory.decodeResource(getResources(),R.mipmap.a1));
        lists.add(BitmapFactory.decodeResource(getResources(),R.mipmap.a2));
        lists.add(BitmapFactory.decodeResource(getResources(),R.mipmap.a3));
        lists.add(BitmapFactory.decodeResource(getResources(),R.mipmap.a4));
        lists.add(BitmapFactory.decodeResource(getResources(),R.mipmap.a5));
    }
}
