package com.itstermoh.hook;
 
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ComponentName;
import android.annotation.NonNull;
import android.os.Message;
import android.os.Handler;
import java.util.ArrayList;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private Blaster blaster;
    private Button test;
    public TextView bulk;
    
    public MainActivity(){};
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        blaster = new Blaster(getApplicationContext(), usbHandler);
        blaster.initializeCommunication();
        
        test = findViewById(R.id.test);
        bulk = findViewById(R.id.bulk);
        
        
        test.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Byte> arrayList = new ArrayList<>();
                
                arrayList.add(((byte) -4));
                arrayList.add(((byte) -4));
                arrayList.add(((byte) -4));
                arrayList.add(((byte) -4));
                
                blaster.test(arrayList);
                //blaster.sendData(new byte[]{});
                if (blaster.sent > 0) {
                    Toast.makeText(getApplicationContext(), "IR mandato | Size="+blaster.sent, Toast.LENGTH_SHORT).show();
                    blaster.sent = 0;
                } else {
                    Toast.makeText(getApplicationContext(), "Errore", Toast.LENGTH_SHORT).show();
                }
            }
        });
   }
    
    private final Handler usbHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
        
        return true;
        }
    });
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (blaster != null) {
            blaster.closeCommunication();
        }
    }
    
    public void bulk(String txt) {
        bulk.setText(txt);
    }
}
