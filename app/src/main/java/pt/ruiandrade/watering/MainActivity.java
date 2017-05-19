package pt.ruiandrade.watering;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MqttCallback {
    static ArrayList<DataModel> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    private static final String prefsFile = "WATERING_TWITTER";
    public static String clientID = "0";
    public static String mqttServer = "tcp://192.168.42.237:1883";
    public static String mqttUser = "kmtohweo";
    public static String mqttPassword = "se2017";
    public static DataModel selectedPlant = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView=(ListView)findViewById(R.id.list);
        refreshList(true, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            savePlant();
        }
        else if (id == R.id.clear_prefs) {
            this.getSharedPreferences(prefsFile, 0).edit().clear().commit();
            refreshList(true, true);
        }

        return super.onOptionsItemSelected(item);
    }

    public void savePlant () {
        final SharedPreferences.Editor editor = getSharedPreferences(prefsFile, MODE_PRIVATE).edit();
        final EditText txtName = new EditText(this);
        txtName.setHint("Name");
        final EditText txtId = new EditText(this);
        txtId.setHint("ID");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(txtName);
        layout.addView(txtId);
        new AlertDialog.Builder(this)
                .setTitle("Add a new plant")
                .setMessage("Please fill both fields")
                .setView(layout)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = txtName.getText().toString();
                        String id = txtId.getText().toString();
                        DataModel plant = new DataModel(name,id);
                        if (getNumberOfPlants() == -1) {
                            editor.putString("plant1", plant.getName() + ";" + plant.getId());
                            editor.putInt("nplants", 1);
                        }
                        else {
                            editor.putString("plant" + (getNumberOfPlants() + 1), plant.getName() + ";" + plant.getId());
                            editor.putInt("nplants", getNumberOfPlants()+1);
                        }
                        Log.d("WATERING", name);
                        Log.d("WATERING", id);
                        editor.commit();
                        publishMessage("plants/" + id + "/name/set", name, false);
                        refreshList(true, true);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    public ArrayList<DataModel> getPlants() {
        ArrayList<DataModel> plants = new ArrayList<DataModel>();
        SharedPreferences prefs = getSharedPreferences(prefsFile, MODE_PRIVATE);
        for (int i = 0 ; i < getNumberOfPlants()+1; i++) {
            String plant = prefs.getString("plant" + i, null);
            Log.w("WATERING", i + "");
            if (plant != null) {
                String name = plant.split(";")[0];
                String id = plant.split(";")[1];
                plants.add(new DataModel(name, id));
            }
        }
        return plants;
    }

    public int getNumberOfPlants() {
        SharedPreferences prefs = getSharedPreferences(prefsFile, MODE_PRIVATE);
        int numberPlants = prefs.getInt("nplants", -1);
        return numberPlants;
    }

    public void refreshList(boolean subscribe, boolean overwrite) {
        if (overwrite) dataModels = getPlants();
        adapter= new CustomAdapter(dataModels,getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataModel dataModel= dataModels.get(position);
                /*Snackbar.make(view, dataModel.getName()+"\n"+dataModel.getId(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();*/
                Intent intent = new Intent(getBaseContext(), PlantStatusActivity.class);
                selectedPlant = dataModel;
                startActivity(intent);
            }
        });
        if (subscribe) subscribeStateRefresh();
    }

    public void subscribeStateRefresh() {
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
                    client.setCallback(MainActivity.this);
                    for (DataModel p : dataModels) {
                        final String topic = "plants/" + p.getId() + "/#";
                        final int qos = 0;
                        try {
                            IMqttToken subToken = client.subscribe(topic, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    // successfully subscribed
                                    Log.w("WATERING", "Successfully subscribed to: " + topic);

                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken,
                                                      Throwable exception) {
                                    // The subscription could not be performed, maybe the user was not
                                    // authorized to subscribe on the specified topic e.g. using wildcards
                                    Log.w("WATERING", "Couldn't subscribe to: " + topic);

                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
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

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        /*
        * To test ,publish  "open"/"close" at topic you subscibed app to in above .
        * */
        Log.d("WATERING",message.toString());
        Log.w("WATERING", "Topic: "+topic+"\nMessage: "+message);
        String[] parsedTopic = topic.split("/");
        String plantID = parsedTopic[1];
        String command = parsedTopic[2];
        String sensor = parsedTopic[3];
        for (DataModel p : dataModels) {
            if (p.getId().equals(plantID) && !command.equals("set")) {
                int value = Double.valueOf(message.toString()).intValue();
                if (sensor.equals("light"))
                    p.luminosity = value;
                else if (sensor.equals("temperature"))
                    p.temperature = value;
                else if (sensor.equals("humidity"))
                    p.humidity = value;
                break;
            }
            if (p.getId().equals(plantID) && command.equals("set") && parsedTopic.length > 4) {
                boolean flag = (message.toString().equals("auto"));
                Log.w("WATERING", message.toString() + " " + flag);
                if (sensor.equals("temperature"))
                    p.autoTemp = flag;
                if (sensor.equals("light"))
                    p.autoLumi = flag;
                if (sensor.equals("humidity"))
                    p.autoHumi = flag;
            }
        }
        refreshList(false, false);
        PlantStatusActivity.updateStatus();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

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
