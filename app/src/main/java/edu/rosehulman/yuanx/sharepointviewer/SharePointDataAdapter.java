package edu.rosehulman.yuanx.sharepointviewer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Sharepoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuanx on 7/21/2015.
 */
public class SharePointDataAdapter extends ArrayAdapter<Sharepoint> implements Filterable{

    private ArrayList<Sharepoint> mList;
    private Filter filter;

    public SharePointDataAdapter(Context context, List<Sharepoint> objects) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1, objects);
        mList = copySharepoints(objects);
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

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new MyFilter();
        }
        return filter;
    }

    class MyFilter extends  Filter {
        @Override
        protected android.widget.Filter.FilterResults performFiltering(CharSequence constraint) {
            android.widget.Filter.FilterResults results = new android.widget.Filter.FilterResults();
            ArrayList<Sharepoint> filteredArray = new ArrayList<>();
            constraint = constraint.toString().toLowerCase();
            List<Sharepoint> temp = mList;
            for(int i=0;i < temp.size(); i++){
                String title = temp.get(i).getTitle();
                String detail = temp.get(i).getDetail();
                if(title.toLowerCase().contains(constraint.toString()) || detail.toLowerCase().contains(constraint.toString())){
                    filteredArray.add(temp.get(i));
                }
            }
            results.count = filteredArray.size();
            results.values = filteredArray;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, android.widget.Filter.FilterResults
        results) {
            clear();

            if(constraint == null || constraint == ""){
                addAll(mList);
            }else {
                if(results.values == null){
                    Log.d(SharePointListActivity.SV, "origin.count = + " + mList.size());
                } else {
                    addAll((List<Sharepoint>) results.values);
                }
            }
            notifyDataSetChanged();
        }
    };

    private ArrayList<Sharepoint> copySharepoints(List<Sharepoint> sharepoints){
        ArrayList<Sharepoint> newSharepoints = new ArrayList<>();
        for(Sharepoint sp : sharepoints){
            newSharepoints.add(sp);
        }
        return newSharepoints;
    }
}
