package pt.ruiandrade.watering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PlantStatusActivity extends AppCompatActivity {
    static DataModel selectedPlant = null;
    static ProgressBar progHumidity = null;
    static ProgressBar progLuminosity = null;
    static ProgressBar progTemperature = null;
    static TextView psName = null;
    static TextView psID = null;
    static TextView tvLumen = null;
    static TextView tvHumid = null;
    static TextView tvTemp = null;
    static ToggleButton tgLumi = null;
    static ToggleButton tgHumi = null;
    static ToggleButton tgTemp = null;
    static EditText tvDLumi = null;
    static Button btnEnvLumi = null;
    static RadioButton rdHeating = null;
    static RadioButton rdCooling = null;
    static RadioButton rdIdle = null;
    static EditText tvTempMax = null;
    static EditText tvTempMin = null;
    static Button btnEnvTemp = null;
    static EditText tvTmpRega = null;
    static EditText tvTmpEntreRega = null;
    static EditText tvDHumi = null;
    static CheckBox chkRega = null;
    static Button btnEnvHumi = null;
    private static final int ANIMATION_DURATION = 750;
    public static String clientID = "0";
    public static String mqttServer = "tcp://m20.cloudmqtt.com:19438";
    public static String mqttUser = "kmtohweo";
    public static String mqttPassword = "8f6BWdD525pW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_status);
        this.selectedPlant = MainActivity.selectedPlant;
        progHumidity = (ProgressBar) findViewById(R.id.progHumidity);
        progLuminosity = (ProgressBar) findViewById(R.id.progLuminosity);
        progTemperature = (ProgressBar) findViewById(R.id.progTemperature);
        tvLumen = (TextView) (findViewById(R.id.tvLumen));
        tvHumid = (TextView) (findViewById(R.id.tvHumi));
        tvTemp = (TextView) (findViewById(R.id.tvTemp));
        psName = (TextView) findViewById(R.id.psName);
        psID = (TextView) findViewById(R.id.psID);
        tgLumi = (ToggleButton) findViewById(R.id.toggleLumen);
        tgHumi = (ToggleButton) findViewById(R.id.toggleHumid);
        tgTemp = (ToggleButton) findViewById(R.id.toggleTemp);
        tvDLumi = (EditText) findViewById(R.id.tvDLumi);
        btnEnvLumi = (Button) findViewById(R.id.btnEnvLumi);
        rdHeating = (RadioButton) findViewById(R.id.rdHeating);
        rdCooling = (RadioButton) findViewById(R.id.rdCooling);
        rdIdle = (RadioButton) findViewById(R.id.rdIdle);
        tvTempMax = (EditText) findViewById(R.id.tvTempMax);
        tvTempMin = (EditText) findViewById(R.id.tvTempMin);
        btnEnvTemp = (Button) findViewById(R.id.btnEnvTemp);
        tvTmpRega = (EditText) findViewById(R.id.tvTmpRega);
        tvTmpEntreRega = (EditText) findViewById(R.id.tvTmpEntreRega);
        tvDHumi = (EditText) findViewById(R.id.tvDHumi);
        chkRega = (CheckBox) findViewById(R.id.chkRega);
        btnEnvHumi = (Button) findViewById(R.id.btnEnvHumi);
        psName.setText(selectedPlant.getName());
        psID.setText("(" + selectedPlant.getId() + ")");
        updateStatus();

        tgLumi.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String state = (tgLumi.isChecked())? "auto":"manual";
                publishMessage("plants/" + selectedPlant.id + "/set/light/mode", state, true);
                selectedPlant.autoLumi = tgLumi.isChecked();
            }
        });
        tgHumi.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String state = (tgHumi.isChecked())? "auto":"manual";
                publishMessage("plants/" + selectedPlant.id + "/set/humidity/mode", state, true);
                selectedPlant.autoHumi = tgHumi.isChecked();
            }
        });
        tgTemp.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String state = (tgTemp.isChecked())? "auto":"manual";
                publishMessage("plants/" + selectedPlant.id + "/set/temperature/mode", state, true);
                selectedPlant.autoTemp = tgTemp.isChecked();
            }
        });
        btnEnvLumi.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String state = tvDLumi.getText().toString();
                if (!selectedPlant.autoLumi) {
                    publishMessage("plants/" + selectedPlant.id + "/set/lightcontrol/state", state, true);
                }
                else {
                    publishMessage("plants/" + selectedPlant.id + "/set/light", state, true);
                }
            }
        });
        btnEnvTemp.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (!selectedPlant.autoTemp) {
                    String state = rdHeating.isChecked() ? "heating" : rdCooling.isChecked() ? "cooling" : "idle";
                    publishMessage("plants/" + selectedPlant.id + "/set/hvac/state", state, true);
                }
                else {
                    String state = tvTempMin.getText().toString() + ";" + tvTempMax.getText().toString();
                    publishMessage("plants/" + selectedPlant.id + "/set/temperature", state, true);
                }
            }
        });
        btnEnvHumi.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (selectedPlant.autoHumi) {
                    String state = chkRega.isChecked() ? "on" : "off";
                    publishMessage("plants/" + selectedPlant.id + "/set/watering/state", state, true);
                }
                else {
                    String state = tvTmpRega.getText().toString() + ";" + tvTmpEntreRega.getText().toString() + ";" + tvDHumi.getText().toString();
                    publishMessage("plants/" + selectedPlant.id + "/set/temperature", state, true);
                }
            }
        });
    }

    public static void updateStatus() {
        ObjectAnimator animationTemp = ObjectAnimator.ofInt (progTemperature, "progress", progTemperature.getProgress(), selectedPlant.getTemperature()); // see this max value coming back here, we animale towards that value
        animationTemp.setDuration (ANIMATION_DURATION); //in milliseconds
        animationTemp.setInterpolator (new DecelerateInterpolator());
        animationTemp.start ();
        animationTemp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator animationHum = ObjectAnimator.ofInt (progHumidity, "progress", progHumidity.getProgress(), selectedPlant.getHumidity()); // see this max value coming back here, we animale towards that value
                animationHum.setDuration (ANIMATION_DURATION); //in milliseconds
                animationHum.setInterpolator (new DecelerateInterpolator());
                animationHum.start ();
                animationHum.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ObjectAnimator animationLum = ObjectAnimator.ofInt (progLuminosity, "progress", progLuminosity.getProgress(), selectedPlant.getLuminosity()); // see this max value coming back here, we animale towards that value
                        animationLum.setDuration (ANIMATION_DURATION); //in milliseconds
                        animationLum.setInterpolator (new DecelerateInterpolator());
                        animationLum.start ();
                    }
                });
            }
        });
        tvLumen.setText(selectedPlant.getLuminosity() + "");
        tvHumid.setText(selectedPlant.getHumidity() + "");
        tvTemp.setText(selectedPlant.getTemperature() + "");
        tgLumi.setChecked(selectedPlant.autoLumi);
        tgHumi.setChecked(selectedPlant.autoHumi);
        tgTemp.setChecked(selectedPlant.autoTemp);

        if (selectedPlant.autoTemp) {
            rdCooling.setVisibility(View.INVISIBLE);
            rdHeating.setVisibility(View.INVISIBLE);
            rdIdle.setVisibility(View.INVISIBLE);
            tvTempMax.setVisibility(View.VISIBLE);
            tvTempMin.setVisibility(View.VISIBLE);
        }
        else {
            rdCooling.setVisibility(View.VISIBLE);
            rdHeating.setVisibility(View.VISIBLE);
            rdIdle.setVisibility(View.VISIBLE);
            tvTempMax.setVisibility(View.INVISIBLE);
            tvTempMin.setVisibility(View.INVISIBLE);
        }

        if(selectedPlant.autoHumi) {
            tvDHumi.setVisibility(View.INVISIBLE);
            tvTmpRega.setVisibility(View.INVISIBLE);
            tvTmpEntreRega.setVisibility(View.INVISIBLE);
            chkRega.setVisibility(View.VISIBLE);
        }
        else {
            tvDHumi.setVisibility(View.VISIBLE);
            tvTmpRega.setVisibility(View.VISIBLE);
            tvTmpEntreRega.setVisibility(View.VISIBLE);
            chkRega.setVisibility(View.INVISIBLE);
        }
    }

    public void publishMessage(final String topic, final String message, final boolean retained) {
        clientID = MqttClient.generateClientId();
        final MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), mqttServer, clientID);
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(mqttUser);
            options.setPassword(mqttPassword.toCharArray());
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.w("WATERING", "onSuccess");
                    MqttMessage msg = new MqttMessage();
                    msg.setPayload(message.getBytes());
                    msg.setRetained(retained);
                    try {
                        client.publish(topic, msg);
                        client.disconnect();
                    } catch (Exception e) {
                        Log.w("WATERING", "onFailure");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.w("WATERING", "onFailure");
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
