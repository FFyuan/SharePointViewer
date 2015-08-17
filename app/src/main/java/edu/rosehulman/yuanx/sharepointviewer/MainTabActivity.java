package edu.rosehulman.yuanx.sharepointviewer;

import android.app.TabActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
//the activity load on launch, with two tabs inside
public class MainTabActivity extends TabActivity {

    TabHost tabHost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        tabHost = getTabHost();

        TabHost.TabSpec spec;
        Intent intent;

        /************* Sharepoint ************/
        intent = new Intent().setClass(this, SharePointListActivity.class);
        spec = tabHost.newTabSpec("Sharepoint").setIndicator("Sharepoint")
                .setContent(intent);
        tabHost.addTab(spec);

        /************* DailyMenu ************/
        intent = new Intent().setClass(this, DailyMenuActivity.class);
        spec = tabHost.newTabSpec("Daily Menu").setIndicator("Daily Menu")
                .setContent(intent);
        tabHost.addTab(spec);
    }

}
