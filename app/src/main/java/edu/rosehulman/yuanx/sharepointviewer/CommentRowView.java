package edu.rosehulman.yuanx.sharepointviewer;

import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appspot.yuanx_sharepoint_viewer.sharepointviewer.model.Comment;

/**
 * Created by yuanx on 7/26/2015.
 * The custom class for Comment Row
 */
public class CommentRowView extends RelativeLayout {
    private TextView mFromUserText;
    private TextView mToUserText;
    private TextView mMessageText;

    public CommentRowView(Context context) {
        super(context);

        ((Activity) context).getLayoutInflater().inflate(R.layout.comment_row, this);

        mFromUserText = (TextView) findViewById(R.id.comment_from_user_text);
        ;
        mToUserText = (TextView) findViewById(R.id.comment_to_user_text);
        mMessageText = (TextView) findViewById(R.id.comment_message_text);
    }

    public void updateComment(Comment comment) {
        mFromUserText.setText("--" + comment.getFromUserNickname());
        mToUserText.setText("Reply to: " + comment.getToUserNickname());
        mMessageText.setText("                 " + comment.getMessage());
    }
}
