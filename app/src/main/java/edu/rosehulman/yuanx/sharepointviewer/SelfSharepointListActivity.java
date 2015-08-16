package edu.rosehulman.yuanx.sharepointviewer;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.Sharepointviewer;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Sharepoint;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.SharepointCollection;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.SharepointProtoEntityKey;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yuanx on 7/22/2015.
 */
public class SelfSharepointListActivity extends ListActivity {

    public static String KEY_USERNAME = "KEY_USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.sharepoint_self_layout);
        //setup action bar
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        String username = (getIntent().getStringExtra(KEY_USERNAME));
        ((TextView) findViewById(R.id.self_title)).setText(username + "'s Sharepoints");
        updateSelfSharepoints();
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
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflator = getMenuInflater();
        if (v == getListView()) {
            inflator.inflate(R.menu.selfsharepointview_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final Sharepoint sharepoint = (Sharepoint) getListAdapter().getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_item_list_view_delete:
                Toast.makeText(SelfSharepointListActivity.this, "Deleting Sharepoint : " + sharepoint.getTitle(), Toast.LENGTH_LONG).show();
                new RemoveSharepoint().execute(sharepoint.getEntityKey());
                return true;
            case R.id.menu_item_list_view_edit:
                DialogFragment df = new DialogFragment() {
                    @Override
                    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                        View view = inflater.inflate(R.layout.dialog_add_sharepoint, container);
                        getDialog().setTitle(R.string.edit_sharepoint_title);
                        final Button confirmButton = (Button) view
                                .findViewById(R.id.dialog_add_sharepoint_ok);
                        final Button cancelButton = (Button) view
                                .findViewById(R.id.dialog_add_sharepoint_cancel);
                        final EditText sharepointTitleEditText = (EditText) view
                                .findViewById(R.id.dialog_add_sharepoint_title);
                        final EditText sharepointDetailEditText = (EditText) view
                                .findViewById(R.id.dialog_add_sharepoint_detail);
                        sharepointTitleEditText.setText(sharepoint.getTitle());
                        sharepointDetailEditText.setText(sharepoint.getDetail());
                        confirmButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String title = sharepointTitleEditText.getText().toString();
                                String detail = sharepointDetailEditText.getText().toString();
                                Toast.makeText(SelfSharepointListActivity.this, "Editing the sharepoint with title " + title, Toast.LENGTH_LONG).show();
                                sharepoint.setTitle(title);
                                sharepoint.setDetail(detail);
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
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void updateSelfSharepoints() {
        new QueryForSelfSharepoints().execute();
    }

    class QueryForSelfSharepoints extends AsyncTask<Void, Void, SharepointCollection> {

        @Override
        protected SharepointCollection doInBackground(Void... params) {
            SharepointCollection sharepoints = null;
            try {
                Sharepointviewer.Sharepoint.List query = SharePointListActivity.mService.sharepoint().list();
                query.setLimit(50L);
                sharepoints = query.execute();
            } catch (IOException e) {
                Log.d(SharePointListActivity.SV, "Failed loading " + e);
            }
            return sharepoints;
        }

        @Override
        protected void onPostExecute(SharepointCollection sharepointCollection) {
            super.onPostExecute(sharepointCollection);
            if (sharepointCollection == null) {
                Log.d(SharePointListActivity.SV, "Failed loading, result is null");
                return;
            }

            if (sharepointCollection.getItems() == null) {
                sharepointCollection.setItems(new ArrayList<Sharepoint>());
            }
            SharePointDataAdapter adapter = new SharePointDataAdapter(SelfSharepointListActivity.this, sharepointCollection.getItems());
            setListAdapter(adapter);
            registerForContextMenu(getListView());
        }
    }

    class RemoveSharepoint extends AsyncTask<String, Void, SharepointProtoEntityKey> {

        @Override
        protected SharepointProtoEntityKey doInBackground(String... params) {
            SharepointProtoEntityKey returned = null;
            try {
                returned = SharePointListActivity.mService.sharepoint().delete(params[0]).execute();
            } catch (IOException e) {
                Log.d(SharePointListActivity.SV, "Failded Deleting " + e);
            }
            return returned;
        }

        @Override
        protected void onPostExecute(SharepointProtoEntityKey sharepointProtoEntityKey) {
            super.onPostExecute(sharepointProtoEntityKey);
            if (sharepointProtoEntityKey == null) {
                Log.d(SharePointListActivity.SV, "Failed Deleting, result is null");
                return;
            }
            updateSelfSharepoints();
        }
    }

    class InsertSharepoint extends AsyncTask<Sharepoint, Void, Sharepoint> {

        @Override
        protected Sharepoint doInBackground(Sharepoint... params) {
            try {
                Sharepoint sharepoint = SharePointListActivity.mService.sharepoint().insert(params[0]).execute();
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
                Log.d(SharePointListActivity.SV, "Error inserting sharepoint, sharepoint is null");
                return;
            }
            updateSelfSharepoints();
        }
    }
}
