package pt.ruiandrade.watering;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<DataModel> implements View.OnClickListener{

    private ArrayList<DataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtID;
        TextView txtHumidity;
        TextView txtTemperature;
        TextView txtLuminosity;
        ImageView info;
    }



    public CustomAdapter(ArrayList<DataModel> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {


        int position=(Integer) v.getTag();
        Object object= getItem(position);
        DataModel dataModel=(DataModel)object;




        switch (v.getId())
        {

            case R.id.item_info:

                Snackbar.make(v, "Plant " + dataModel.getName(), Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();

                break;


        }


    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {


            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtID = (TextView) convertView.findViewById(R.id.id);
            viewHolder.txtHumidity = (TextView) convertView.findViewById(R.id.humidity);
            viewHolder.txtLuminosity = (TextView) convertView.findViewById(R.id.luminosity);
            viewHolder.txtTemperature = (TextView) convertView.findViewById(R.id.temperature);
            viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        /*Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);*/
        lastPosition = position;


        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.txtID.setText(dataModel.getId());
        viewHolder.txtHumidity.setText(String.valueOf(dataModel.getHumidity()));
        viewHolder.txtLuminosity.setText(String.valueOf(dataModel.getLuminosity()));
        viewHolder.txtTemperature.setText(String.valueOf(dataModel.getTemperature()));
        viewHolder.info.setOnClickListener(this);
        viewHolder.info.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }


}
