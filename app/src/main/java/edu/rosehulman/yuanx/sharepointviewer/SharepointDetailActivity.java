package edu.rosehulman.yuanx.sharepointviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Comment;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.CommentCollection;
import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.CommentProtoEntityKey;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yuanx on 7/22/2015.
 */
public class SharepointDetailActivity extends Activity {
    public static String KEY_ENTITY = "KEY_ENTITY";
    public static String KEY_TITLE = "KEY_TITLE";
    public static String KEY_DETAIL = "KEY_DETAIL";
    public static String KEY_USER = "KEY_USER";
    public static String KEY_NAME = "KEY_NAME";

    private String mSharepointKey;
    private ListView mCommentsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sharepoint_detail_layout);
        //setup the action bar
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent data = getIntent();
        final String ownerKey = data.getStringExtra(KEY_USER);
        final String ownerName = data.getStringExtra(KEY_NAME);
        //configure the owner, the title and the description,
        ((TextView) findViewById(R.id.titile_text)).setText(data.getStringExtra(KEY_TITLE));
        ((TextView) findViewById(R.id.detail_text)).setText(data.getStringExtra(KEY_DETAIL));
        ((TextView) findViewById(R.id.owner_text)).setText(ownerName);
        mSharepointKey = data.getStringExtra(KEY_ENTITY);
        mCommentsView = (ListView) findViewById(R.id.comment_list);
        mCommentsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Comment commentClicked = (Comment) mCommentsView.getAdapter().getItem(position);
                addDialog(commentClicked.getFromUserKey(), commentClicked.getFromUserNickname());
            }
        });
        mCommentsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Comment commentClicked = (Comment) mCommentsView.getAdapter().getItem(position);
                deleteDialog(commentClicked.getEntityKey());
                return true;
            }
        });
        ((Button) findViewById(R.id.add_comment_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog(ownerKey, ownerName);
            }
        });
        updateComments();
    }

    private void updateComments() {
        new QueryForComments().execute(mSharepointKey);
    }

    private void addDialog(final String userkey, final String userName) {
        DialogFragment df = new DialogFragment() {
            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                View view = inflater.inflate(R.layout.dialog_add_comment, container);
                getDialog().setTitle("To " + userName);
                final Button confirmButton = (Button) view
                        .findViewById(R.id.dialog_add_comment_ok);
                final Button cancelButton = (Button) view
                        .findViewById(R.id.dialog_add_comment_cancel);
                final EditText messageEditText = (EditText) view
                        .findViewById(R.id.dialog_add_comment_message);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(SharepointDetailActivity.this, "Posting comment to user: " + userName, Toast.LENGTH_LONG).show();
                        Comment comment = new Comment();
                        comment.setToUserKey(userkey);
                        comment.setToUserNickname(userName);
                        comment.setMessage(messageEditText.getText().toString());
                        comment.setSharepointKey(mSharepointKey);
                        new InsertNewComments().execute(comment);
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

    private void deleteDialog(final String entityKey) {
        DialogFragment df = new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete the comment?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteComment().execute(entityKey);
                        dismiss();
                    }
                });
                return builder.create();
            }
        };
        df.show(getFragmentManager(), "");
    }

    class QueryForComments extends AsyncTask<String, Void, CommentCollection> {

        @Override
        protected CommentCollection doInBackground(String... params) {
            CommentCollection commentCollection = null;
            try {
                commentCollection = SharePointListActivity.mService.comment().list(params[0]).execute();
            } catch (IOException e) {
                Log.d(SharePointListActivity.SV, "Failed loading Comments " + e);
            }
            return commentCollection;
        }

        @Override
        protected void onPostExecute(CommentCollection commentCollection) {
            super.onPostExecute(commentCollection);
            if (commentCollection == null) {
                Log.d(SharePointListActivity.SV, "Failed loading Comments, return null");
                return;
            }
            if (commentCollection.getItems() == null) {
                commentCollection.setItems(new ArrayList<Comment>());
            }
            CommnetListAdapter adapter = new CommnetListAdapter(SharepointDetailActivity.this, commentCollection.getItems());
            mCommentsView.setAdapter(adapter);
        }
    }

    class InsertNewComments extends AsyncTask<Comment, Void, Comment> {

        @Override
        protected Comment doInBackground(Comment... params) {
            try {
                Comment comment = SharePointListActivity.mService.comment().insert(params[0]).execute();
                return comment;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Comment comment) {
            super.onPostExecute(comment);
            if (comment == null) {
                Log.d(SharePointListActivity.SV, "Faild inserting the comment, result is null");
                return;
            }
            updateComments();
        }
    }

    class DeleteComment extends AsyncTask<String, Void, CommentProtoEntityKey> {

        @Override
        protected CommentProtoEntityKey doInBackground(String... params) {
            try {
                return SharePointListActivity.mService.comment().delete(params[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(CommentProtoEntityKey commentProtoEntityKey) {
            super.onPostExecute(commentProtoEntityKey);
            updateComments();
        }
    }
}
