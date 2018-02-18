package io.particle.cloudsdk.example_app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class ValueActivity extends AppCompatActivity {

    private static final String ARG_VALUE = "ARG_VALUE";
    private static final String ARG_DEVICEID = "ARG_DEVICEID";
    private long motionSubscription;
    public TextView tv;
    public TextView temp;
    public TextView dist;
    public TextView accel;
    public TextView vibrate;

    public String data = "no value";
    public String event;
    public String temperature;
    public String vibration;
    public String distance;
    public List<String> graphPlot;
    //public boolean playNoiseOnce = false;

    public void launchGraph() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.vipondhackathon.hackathon");
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    public void callHelpOnEmergency() {
        String cell = "";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + cell));
        startActivity(callIntent);
    }

    public void notifyDanger() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
       // MediaPlayer mPlayer = MediaPlayer.create(this, R.au.alert);
     //   mPlayer.start();

    }

    public void setText(String sensorName, String senesorData) {
        Log.i("setText", "setTextCalled");
        tv.setText(senesorData);
        if (sensorName == "Temperature")
            temp.setText(senesorData);
        else if (sensorName == "Acceleration")
            tv.setText(data);
        else if (sensorName == "Distance")
            dist.setText(senesorData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_layout);
        tv = findViewById(R.id.value);
        temp = findViewById(R.id.temp);
        dist = findViewById(R.id.distance);
        accel = findViewById(R.id.acceleration);
        vibrate = findViewById(R.id.vibration);
        graphPlot = new ArrayList<String>();
        // tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));
        tv.setText(String.valueOf(getIntent().getIntExtra(ARG_VALUE, 0)));
        //Danger();

        Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

            private ParticleDevice mDevice;

            @Override
            public Object callApi(@NonNull ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                mDevice = sparkCloud.getDevice("3f0062000f51353433323633");//1f0034000747343232361234
                Object obj;
                sparkCloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                    @Override
                    public void onEvent(String eventName, ParticleEvent particleEvent) {
                        Log.i("BANANA", "onEvent: " + eventName + particleEvent);
                        // tv.setText(particleEvent.dataPayload);
                        // setText(eventName, particleEvent.dataPayload);
                        data = particleEvent.dataPayload;
                        event = eventName;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.i("setText", event);
                                tv.setText(data);
                                //dist.setText(event);
                                if (event == " Temperature" || event == "Temperature" || event == "Temperature " || event.contains("Temperature")) {
                                    temp.setText(data);
                                    Log.i("setText", "if temp called");
                                }
                               /* else if(event.contains("Speed"))
                                    tv.setText(data);*/
                                else if (event.contains("Distance") || event.contains("distance")) {
                                    dist.setText(data);
                                    if (data.contains("close"))
                                        notifyDanger();
                                } else if (event.contains("Speed") || event.contains("speed")) {
                                    accel.setText(data);
                                    String speed = event.substring(7);
                                    //Double sp = Double.parseDouble(speed);
                                   graphPlot.add(speed);
                                } else if (event.contains("Vibration")) {
                                    // callHelpOnEmergency();
                                }
                            }

                        });
                       /* tv.setText(data);
                        if(event == "Temperature")
                            temp.setText(data);
                        else if(event == "Acceleration")
                            tv.setText(data);
                        else if(event == "Distance")
                            dist.setText(data);*/
                     /*   Log.i("acceleration", eventName + "///// " );
                        Log.i("varnames", data + "   " + event);
                        Log.i("event",event);*/
                        //tv.setText(particleEvent.dataPayload);

                    }

                    @Override
                    public void onEventError(Exception e) {
                        Log.e("BANANA", "OH NOES, onEventError: ", e);
                    }
                });


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Async.executeAsync(sparkCloud, new Async.ApiProcedure<ParticleCloud>() {
                            @Override
                            public Void callApi(ParticleCloud particleCloud)
                                    throws ParticleCloudException, IOException {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.i("BANANA", "callApi: PUBLISH");
                                particleCloud.publishEvent("BANANA_EVENT", "banana_time",
                                        ParticleEventVisibility.PRIVATE, 300);

                                return null;
                            }

                            @Override
                            public void onFailure(ParticleCloudException exception) {

                            }
                        });
                    }
                });

                return -1;

            }

            @Override
            public void onSuccess(Object o) {
            }

            @Override
            public void onFailure(ParticleCloudException exception) {

            }

            // tv.setText(LoginActivity.);

       /* findViewById(R.id.refresh_button).setOnClickListener(v -> {
            //...
            // Do network work on background thread
            Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {
                @Override
                public Object callApi(@NonNull ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {
                    ParticleDevice device = ParticleCloud.getDevice(getIntent().getStringExtra(ARG_DEVICEID));
                    Object variable;
                    try {
                        variable = device.getVariable("motion");

                    } catch (ParticleDevice.VariableDoesNotExistException e) {
                        Toaster.l(ValueActivity.this, e.getMessage());
                        variable = -1;
                    }
                    return variable;
                }

                @Override
                public void onSuccess(@NonNull Object i) { // this goes on the main thread
                    tv.setText(i.toString());
                }

                @Override
                public void onFailure(@NonNull ParticleCloudException e) {
                    e.printStackTrace();
                }
            });
        });*/
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.menu_value, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            Intent intent = DeviceInfoActivity.buildIntent(ValueActivity.this, getIntent().getStringExtra(ARG_DEVICEID));
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    public static Intent buildIntent(Context ctx, Integer value, String deviceId) {
        Intent intent = new Intent(ctx, ValueActivity.class);
        intent.putExtra(ARG_VALUE, value);
        intent.putExtra(ARG_DEVICEID, deviceId);

        return intent;
    }


    public void setupGraph(View view) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.vipondhackathon.hackathon");
        if (launchIntent != null) {
            launchIntent.putStringArrayListExtra("data", (ArrayList<String>) graphPlot);

            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }
}
