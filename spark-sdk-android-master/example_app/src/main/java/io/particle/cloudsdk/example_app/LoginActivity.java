package io.particle.cloudsdk.example_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class LoginActivity extends AppCompatActivity {
    public String acceleration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParticleCloudSDK.init(this);
        setContentView(R.layout.activity_login);

        findViewById(R.id.login_button).setOnClickListener(
                v -> {
                    final String email = ((EditText) findViewById(R.id.email)).getText().toString();
                    final String password = ((EditText) findViewById(R.id.password)).getText().toString();

                    // Don't:
                    @SuppressLint("StaticFieldLeak")
                    AsyncTask task = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            try {
                                ParticleCloudSDK.getCloud().logIn(email, password);

                            } catch (final ParticleCloudException e) {
                                Runnable mainThread = () -> {
                                    Toaster.l(LoginActivity.this, e.getBestMessage());
                                    e.printStackTrace();
                                    Log.d("info", e.getBestMessage());
//                                            Log.d("info", e.getCause().toString());
                                };
                                runOnUiThread(mainThread);

                            }

                            return null;
                        }

                    };
//                        task.execute();

                    //-------

                    // DO!:
                    Async.executeAsync(ParticleCloudSDK.getCloud(), new Async.ApiWork<ParticleCloud, Object>() {

                        private ParticleDevice mDevice;

                        @Override
                        public Object callApi(@NonNull ParticleCloud sparkCloud) throws ParticleCloudException, IOException {
                            sparkCloud.logIn(email, password);
                            sparkCloud.getDevices();
                            mDevice = sparkCloud.getDevice("3f0062000f51353433323633");//1f0034000747343232361234
                            Object obj;
                           /* sparkCloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                                @Override
                                public void onEvent(String eventName, ParticleEvent particleEvent) {
                                    Log.i("BANANA", "onEvent: " + eventName + particleEvent);
                                    acceleration = particleEvent.dataPayload;
                                    Log.i("acceleration", acceleration);

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
                            });*/

                            try {
                                obj = mDevice.getVariable("analogvalue");
                                Log.d("BANANA", "analogvalue: " + obj);
                            } catch (ParticleDevice.VariableDoesNotExistException e) {
                                Toaster.s(LoginActivity.this, "Error reading variable");
                            }

                            try {
                                String strVariable = mDevice.getStringVariable("stringvalue");
                                Log.d("BANANA", "stringvalue: " + strVariable);
                            } catch (ParticleDevice.VariableDoesNotExistException e) {
                                Toaster.s(LoginActivity.this, "Error reading variable");
                            }

                            try {
                                double dVariable = mDevice.getDoubleVariable("doublevalue");
                                Log.d("BANANA", "doublevalue: " + dVariable);
                            } catch (ParticleDevice.VariableDoesNotExistException e) {
                                Toaster.s(LoginActivity.this, "Error reading variable");
                            }

                            try {
                                int intVariable = mDevice.getIntVariable("analogvalue");
                                Log.d("BANANA", "int analogvalue: " + intVariable);
                            } catch (ParticleDevice.VariableDoesNotExistException e) {
                                Toaster.s(LoginActivity.this, "Error reading variable");
                            }

                            return -1;

                        }

                        @Override
                        public void onSuccess(@NonNull Object value) {
                            Toaster.l(LoginActivity.this, "Logged in");
                            Intent intent = ValueActivity.buildIntent(LoginActivity.this, 123, mDevice.getID());
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(@NonNull ParticleCloudException e) {
                            Toaster.l(LoginActivity.this, e.getBestMessage());
                            e.printStackTrace();
                            Log.d("info", e.getBestMessage());
                        }
                    });
                }
        );
    }

}
