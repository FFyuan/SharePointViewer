package edu.rosehulman.yuanx.sharepointviewer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Comment;

import java.util.List;

/**
 * Created by yuanx on 7/27/2015.
 * The comment list adapter
 */
public class CommnetListAdapter extends BaseAdapter {

    private Context mContext;
    private List<Comment> mComments;

    public CommnetListAdapter(Context context, List<Comment> comments) {
        mContext = context;
        mComments = comments;
    }

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentRowView view;
        if (convertView == null) {
            view = new CommentRowView(mContext);
        } else {
            view = (CommentRowView) convertView;
        }
        view.updateComment((Comment) getItem(position));
        return view;
    }
}
