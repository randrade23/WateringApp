package pt.ruiandrade.watering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlantStatusActivity extends AppCompatActivity {
    static DataModel selectedPlant = null;
    static ProgressBar progHumidity = null;
    static ProgressBar progLuminosity = null;
    static ProgressBar progTemperature = null;
    static TextView psName = null;
    static TextView psID = null;
    private static final int ANIMATION_DURATION = 750;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_status);
        this.selectedPlant = MainActivity.selectedPlant;
        progHumidity = (ProgressBar) findViewById(R.id.progHumidity);
        progLuminosity = (ProgressBar) findViewById(R.id.progLuminosity);
        progTemperature = (ProgressBar) findViewById(R.id.progTemperature);
        psName = (TextView) findViewById(R.id.psName);
        psID = (TextView) findViewById(R.id.psID);
        psName.setText(selectedPlant.getName());
        psID.setText("(" + selectedPlant.getId() + ")");
        updateStatus();
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
    }
}
