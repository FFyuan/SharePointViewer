package edu.rosehulman.yuanx.sharepointviewer;

import android.app.Activity;
import android.app.ListActivity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.AramarkHelperMenuFetcher;
import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.model.Category;
import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.model.CategoryCollection;
import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.model.Dish;
import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.model.DishCollection;
import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.model.Period;
import com.appspot.aramark_helper_menu_fetcher.aramarkHelperMenuFetcher.model.PeriodCollection;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.Sharepointviewer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

//the Menu activity
public class MenuListActivity extends ListActivity {

    private AramarkHelperMenuFetcher mService;
    public static String KEY_PERIOD = "KEY_PERIOD";
    private String period;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);
        //get the period from intent and set it as title
        this.period = getIntent().getStringExtra(KEY_PERIOD);
        ((TextView)findViewById(R.id.daily_menu_period_title)).setText(this.period);
        //perpare to connect with the api
        mService = (new AramarkHelperMenuFetcher.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), null)).build();
        //set the adapter
        MenuListAdapter adapter = new MenuListAdapter(this, new ArrayList<MenuItems>());
        setListAdapter(adapter);
        //query for the current date's menu
        DateFormat df = new SimpleDateFormat(getString(R.string.date_format));
        String date = df.format(Calendar.getInstance().getTime());
        (new QueryForPeriod()).execute(date);
    }

    //Find the entityKey of the specific period
    class QueryForPeriod extends AsyncTask<String, Void, PeriodCollection>{

        @Override
        protected PeriodCollection doInBackground(String... params) {
            PeriodCollection results = null;
            try {
                results = mService.period().list(params[0]).execute();
            } catch (IOException e) {
                Log.d("SV","Fail loading periods" + e);
            }
            return results;
        }

        @Override
        protected void onPostExecute(PeriodCollection periodCollection) {
            super.onPostExecute(periodCollection);
            if(periodCollection == null){
                Log.d("SV","Fail loading periods" );
            }else{
                for(Period period : periodCollection.getItems()){
                    Log.d("SV", "Checking period for " + period.getName() + " " + MenuListActivity.this.period);
                    if(period.getName().equals(MenuListActivity.this.period)){
                        //find the categories in certain perid
                        Log.w("SV","Now Loading categories");
                        (new QueryForCategories()).execute(period);
                    }
                }
            }
        }
    }
    //Find the categories of the specific period
    class QueryForCategories extends AsyncTask<Period, Void, CategoryCollection>{

        @Override
        protected CategoryCollection doInBackground(Period... params) {
            CategoryCollection results = null;
            try {
                results = mService.category().list(params[0].getEntityKey()).execute();
            } catch (IOException e) {
                Log.d("SV", "Failed loading categories " + e);
            }
            return results;
        }

        @Override
        protected void onPostExecute(CategoryCollection categoryCollection) {
            super.onPostExecute(categoryCollection);
            if(categoryCollection == null){
                Log.d("SV", "Failed loading categories ");
            }else{
                Log.w("SV", "Now Loading the dishes");
                //then get all the information of the Menu
                (new QueryForDishes()).execute(categoryCollection.getItems());
            }
        }
    }

    //take the list of categories as input, request for each of the categories
    class QueryForDishes extends AsyncTask<List<Category>, Void, ArrayList<MenuItems>>{
        @Override
        protected ArrayList<MenuItems> doInBackground(List<Category>... params) {
            ArrayList<MenuItems> results = new ArrayList<>();
            for(Category category : params[0]) {
                try {
                    Log.d("SV", "Searching dishes for " + category.getName());
                    AramarkHelperMenuFetcher.Dish.List query = mService.dish().list(category.getEntityKey());
                    DishCollection dishes = query.execute();
                    //wrap up in the menuItems object and add to a temp result
                    results.add(new MenuItems(category.getName(), true));
                    for(Dish dish : dishes.getItems()){
                        results.add(new MenuItems(dish.getName(), false));
                    }
                } catch (IOException e) {
                    Log.e("SV", "Failed loading dishes for category : " + category.getName() + " " + e);
                }
            }
            return results;
        }

        @Override
        protected void onPostExecute(ArrayList<MenuItems> menuItemses) {
            super.onPostExecute(menuItemses);
            if(menuItemses.size()==0){
                Log.d("SV", "Failed loading dishes, result is null");
            }else{
                Log.d("SV", "Success loading dishes.");
                //add the result to the adapter
                ((MenuListAdapter)getListAdapter()).addAll(menuItemses);
                ((MenuListAdapter)getListAdapter()).notifyDataSetChanged();
            }
        }

    }
}
