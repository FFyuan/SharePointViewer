package edu.rosehulman.yuanx.sharepointviewer;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.Sharepointviewer;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Sharepoint;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.SharepointCollection;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yuanx on 7/21/2015.
 */
public class SharePointListActivity extends ListActivity implements View.OnClickListener {

    static Sharepointviewer mService;
    GoogleAccountCredential mCredential;
    SharedPreferences mSettings = null;

    public static final String SHARED_PREFERENCES_NAME = "SharepointViewer";
    public static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
    public static final String SV = "SV";
    static final int REQUEST_ACCOUNT_PICKER = 1;

    //----------------------------
    //activity status
    //-----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.sharepoint_main_layout);
        //configure the action bar and show it
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.sharepoint_main_actionbar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.getCustomView().findViewById(R.id.sync_button).setOnClickListener(this);
        actionBar.getCustomView().findViewById(R.id.add_sharepoint_button).setOnClickListener(this);
        actionBar.getCustomView().findViewById(R.id.show_self_button).setOnClickListener(this);

        mCredential = GoogleAccountCredential.usingAudience(this,
                "server:client_id:702117983306-uo985e1nandi1slhh1gt8op69egjme34.apps.googleusercontent.com");
        mSettings = getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        setAccountName(mSettings.getString(PREF_ACCOUNT_NAME, null));
        Sharepointviewer.Builder builder = new Sharepointviewer.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), mCredential);
        mService = builder.build();

        if (mCredential.getSelectedAccountName() == null) {
            // Not signed in, show login window or request an existing account.
            chooseAccount();
        } else {
            updateSharepoints();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Sharepoint sharepoint = (Sharepoint) getListAdapter().getItem(position);
        Intent intent = new Intent(this, SharepointDetailActivity.class);
        intent.putExtra(SharepointDetailActivity.KEY_TITLE, sharepoint.getTitle());
        intent.putExtra(SharepointDetailActivity.KEY_DETAIL, sharepoint.getDetail());
        intent.putExtra(SharepointDetailActivity.KEY_USER, sharepoint.getUserKey());
        intent.putExtra(SharepointDetailActivity.KEY_NAME, sharepoint.getUserNickname());
        intent.putExtra(SharepointDetailActivity.KEY_ENTITY, sharepoint.getEntityKey());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_sharepoint_button:
                addSharepoint();
                break;
            case R.id.sync_button:
                updateSharepoints();
                break;
            case R.id.show_self_button:
                Intent intent = new Intent(this, SelfSharepointListActivity.class);
                //should put the user information
                startActivity(intent);
                break;
        }
    }

    //Dialog to add a sharepoint
    private void addSharepoint() {
        DialogFragment df = new DialogFragment() {
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.dialog_add_sharepoint, container);
                getDialog().setTitle(R.string.dialog_add_sharepoint_title);
                final Button confirmButton = (Button) view
                        .findViewById(R.id.dialog_add_sharepoint_ok);
                final Button cancelButton = (Button) view
                        .findViewById(R.id.dialog_add_sharepoint_cancel);
                final EditText sharepointTitleEditText = (EditText) view
                        .findViewById(R.id.dialog_add_sharepoint_title);
                final EditText sharepointDetailEditText = (EditText) view
                        .findViewById(R.id.dialog_add_sharepoint_detail);

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = sharepointTitleEditText.getText().toString();
                        String detail = sharepointDetailEditText.getText().toString();
                        Toast.makeText(SharePointListActivity.this, "Posting the sharepoint with title " + title, Toast.LENGTH_LONG).show();
                        Sharepoint sharepoint = new Sharepoint();
                        sharepoint.setTitle(title);
                        sharepoint.setDetail(detail);
//                        ((SharePointDataAdapter) getListAdapter()).add(sharepoint);
//                        ((SharePointDataAdapter) getListAdapter()).notifyDataSetChanged();
                        new InsertSharepoint().execute(sharepoint);
                        dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                return view;
            }
        };
        df.show(getFragmentManager(), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        setAccountName(accountName); // User is authorized.
                        updateSharepoints();
                    }
                }
                break;
        }
    }

    //----------------------------
    //helper methods
    //---------------------------

    private void setAccountName(String accountName) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
        mCredential.setSelectedAccountName(accountName);
    }

    void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), 1);
    }

    private void updateSharepoints() {
        new QueryForAllSharepoints().execute();
    }


    //-------------------------------
    //Backend communication
    //-------------------------------

    class QueryForAllSharepoints extends AsyncTask<Void, Void, SharepointCollection> {

        @Override
        protected SharepointCollection doInBackground(Void... params) {
            SharepointCollection sharepoints = null;
            try {
                Sharepointviewer.Sharepoint.ListAll query = mService.sharepoint().listAll();
                query.setLimit(50L);
                sharepoints = query.execute();
            } catch (IOException e) {
                Log.d(SV, "Failed loading " + e);
            }
            return sharepoints;
        }

        @Override
        protected void onPostExecute(SharepointCollection sharepointCollection) {
            super.onPostExecute(sharepointCollection);
            if (sharepointCollection == null) {
                Log.d(SV, "Failed loading, result is null");
                return;
            }

            if (sharepointCollection.getItems() == null) {
                sharepointCollection.setItems(new ArrayList<Sharepoint>());
            }
            SharePointDataAdapter adapter = new SharePointDataAdapter(SharePointListActivity.this, sharepointCollection.getItems());
            setListAdapter(adapter);
        }
    }

    class InsertSharepoint extends AsyncTask<Sharepoint, Void, Sharepoint> {

        @Override
        protected Sharepoint doInBackground(Sharepoint... params) {
            try {
                Sharepoint sharepoint = mService.sharepoint().insert(params[0]).execute();
                return sharepoint;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Sharepoint sharepoint) {
            super.onPostExecute(sharepoint);
            if (sharepoint == null) {
                Log.d(SV, "Error inserting sharepoint, sharepoint is null");
                return;
            }
            updateSharepoints();
        }
    }
}
