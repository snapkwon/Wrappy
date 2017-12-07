package net.wrappy.im.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.R;

/**
 * Created by n8fr8 on 3/29/16.
 */
public class ContactViewHolder extends RecyclerView.ViewHolder {
    public ContactViewHolder(View view) {
        super(view);
        mLine1 = (TextView) view.findViewById(R.id.line1);
        mLine2 = (TextView) view.findViewById(R.id.line2);
        mAvatar = (ImageView) view.findViewById(R.id.avatar);
        mAvatarCheck = (ImageView) view.findViewById(R.id.avatarCheck);

        mSubBox = view.findViewById(R.id.subscriptionBox);
        mButtonSubApprove = (Button) view.findViewById(R.id.btnApproveSubscription);
        mButtonSubDecline = (Button) view.findViewById(R.id.btnDeclineSubscription);

        //holder.mStatusIcon = (ImageView)view.findViewById(R.id.statusIcon);
        //holder.mStatusText = (TextView)view.findViewById(R.id.statusText);
        //holder.mEncryptionIcon = (ImageView)view.findViewById(R.id.encryptionIcon);

        mContainer = view.findViewById(R.id.message_container);
    }

    public String mAddress;
    public String mNickname;
    public String email;
    public int mType;

    public long mProviderId;
    public long mAccountId;
    public long mContactId;

    public View mContainer;
    public TextView mLine1;
    public TextView mLine2;
    //public TextView mStatusText;
    public ImageView mAvatar;
    public ImageView mAvatarCheck;
    //public ImageView mStatusIcon;
    public ImageView mMediaThumb;

    public View mSubBox;
    public Button mButtonSubApprove;
    public Button mButtonSubDecline;

    public void bindSelectedData(Context context, ContactsPickerActivity.SelectedContact contact) {
        mAvatarCheck.setVisibility(View.VISIBLE);
        mLine1.setTextColor((mLine1.getCurrentTextColor() & 0x00ffffff) | 0x80000000);
//        mLine1.setText(context.getString(R.string.is_already_in_your_group, mLine1.getText()));
    }
}
