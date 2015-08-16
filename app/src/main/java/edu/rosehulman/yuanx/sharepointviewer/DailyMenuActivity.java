package edu.rosehulman.yuanx.sharepointviewer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class DailyMenuActivity extends TabActivity {

    TabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_menu);
        tabHost = getTabHost();

        // Set TabChangeListener called when tab changed
        //tabHost.setOnTabChangedListener(this);

        TabHost.TabSpec spec;
        Intent intent;

        /************* Breakfast ************/
        intent = new Intent().setClass(this, MenuListActivity.class);
        intent.putExtra(MenuListActivity.KEY_PERIOD, "Breakfast");
        spec = tabHost.newTabSpec("Breakfast").setIndicator("Breakfast")
                .setContent(intent);
        tabHost.addTab(spec);
        /************* Lunch ************/
        intent = new Intent().setClass(this, MenuListActivity.class);
        intent.putExtra(MenuListActivity.KEY_PERIOD, "Lunch");
        spec = tabHost.newTabSpec("Lunch").setIndicator("Lunch")
                .setContent(intent);
        tabHost.addTab(spec);
        /************* Dinner ************/
        intent = new Intent().setClass(this, MenuListActivity.class);
        intent.putExtra(MenuListActivity.KEY_PERIOD, "Dinner");
        spec = tabHost.newTabSpec("Dinner").setIndicator("Dinner")
                .setContent(intent);
        tabHost.addTab(spec);

    }
}
