package edu.rosehulman.yuanx.sharepointviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuanx on 8/16/2015.
 * The adapter of the menu list, implementing the header feature
 */
public class MenuListAdapter extends ArrayAdapter<MenuItems> {

    private ArrayList<MenuItems> items;
    private LayoutInflater inflater;

    public MenuListAdapter(Context context, ArrayList<MenuItems> objects) {
        super(context, 0, objects);
        this.items = objects;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //use the isHeader flag to decide which kind of view to inflate
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        MenuItems item = items.get(position);
        if(item.isHeader()){
            view = inflater.inflate(R.layout.menu_list_header, null);
            ((TextView)view.findViewById(R.id.menulist_category)).setText(item.getName());
        }else{
            view = inflater.inflate(R.layout.menu_list_item, null);
            ((TextView)view.findViewById(R.id.menulist_dish)).setText(item.getName());
        }
        return view;
    }
}
