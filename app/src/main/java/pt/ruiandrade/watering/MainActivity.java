package pt.ruiandrade.watering;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<DataModel> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    private static final String prefsFile = "WATERING_TWITTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView=(ListView)findViewById(R.id.list);
        refreshList();
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
            refreshList();
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
                        refreshList();
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

    public void refreshList() {
        dataModels = getPlants();
        adapter= new CustomAdapter(dataModels,getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataModel dataModel= dataModels.get(position);
                Snackbar.make(view, dataModel.getName()+"\n"+dataModel.getId(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
            }
        });
    }
}
