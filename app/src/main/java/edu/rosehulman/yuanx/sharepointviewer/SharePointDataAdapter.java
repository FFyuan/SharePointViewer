package edu.rosehulman.yuanx.sharepointviewer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Sharepoint;

import java.util.List;

/**
 * Created by yuanx on 7/21/2015.
 */
public class SharePointDataAdapter extends ArrayAdapter<Sharepoint> {
    public SharePointDataAdapter(Context context, List<Sharepoint> objects) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Sharepoint sp = getItem(position);
        TextView titleView = (TextView) view.findViewById(android.R.id.text1);
        TextView detailView = (TextView) view.findViewById(android.R.id.text2);
        titleView.setText(sp.getTitle());
        detailView.setText(sp.getDetail());
        return view;
    }
}
