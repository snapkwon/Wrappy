package net.wrappy.im.ui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.R;

/**
 * Created by n8fr8 on 12/14/15.
 */
public class ConversationViewHolder extends RecyclerView.ViewHolder
{

    public View mViewGroup;
    public TextView mLine1;
    public TextView mLine2;
    public TextView mStatusText;
    public ImageView mAvatar;
    public ImageView mStatusIcon;
    public View mContainer;
    public ImageView mMediaThumb;
    public ImageView mPinIcon;
    public ImageView mAvatarStatus;

    public ConversationViewHolder(View view)
    {
        super(view);

        mViewGroup = view.findViewById(R.id.convoitemview);

        mLine1 = (TextView) view.findViewById(R.id.line1);
        mLine2 = (TextView) view.findViewById(R.id.line2);

        mAvatar = (ImageView)view.findViewById(R.id.avatar);
        mStatusIcon = (ImageView)view.findViewById(R.id.statusIcon);
        mStatusText = (TextView)view.findViewById(R.id.statusText);
        mAvatarStatus = (ImageView)view.findViewById(R.id.avatarStatus);

        mContainer = view.findViewById(R.id.message_container);

        mMediaThumb = (ImageView)view.findViewById(R.id.media_thumbnail);

        mPinIcon = (ImageView)view.findViewById(R.id.pinIcon);
    }

    public void onItemSelected ()
    {

    }

    public void onItemClear ()
    {

    }

}
