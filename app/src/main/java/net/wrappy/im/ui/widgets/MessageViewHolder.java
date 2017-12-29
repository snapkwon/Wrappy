package net.wrappy.im.ui.widgets;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.ui.MediaViewHolder;
import net.wrappy.im.ui.MessageListItem;

import net.wrappy.im.R;

/**
 * Created by n8fr8 on 12/11/15.
 */
public class MessageViewHolder extends MediaViewHolder {
    public TextView mTextViewForMessages;
    public TextView mTextViewForTimestamp;
    public ImageView mAvatar;

    public View mMediaContainer;
    public View mAudioContainer;
    public VisualizerView mVisualizerView;
    public ImageView mAudioButton;
    public TextView btntranslate;
    public TextView txttranslate;

    int Pos;
    // save the media uri while the MediaScanner is creating the thumbnail
    // if the holder was reused, the pair is broken

    public MessageViewHolder(View view) {
        super(view);

        mTextViewForMessages = (TextView) view.findViewById(R.id.message);
        mTextViewForTimestamp = (TextView) view.findViewById(R.id.messagets);
        mAvatar = (ImageView) view.findViewById(R.id.avatar);
        mMediaContainer = view.findViewById(R.id.media_thumbnail_container);
        mAudioContainer = view.findViewById(R.id.audio_container);
        mVisualizerView = (VisualizerView) view.findViewById(R.id.audio_view);
        mAudioButton = (ImageView) view.findViewById(R.id.audio_button);
        btntranslate = (TextView) view.findViewById(R.id.btntranslate);
        txttranslate = (TextView) view.findViewById(R.id.txttranslate);

        // disable built-in autoLink so we can add custom ones
        mTextViewForMessages.setAutoLinkMask(0);

        //mContainer.setBackgroundResource(R.drawable.message_view_rounded_light);
    }


    public void setOnClickListenerMediaThumbnail(final String mimeType, final Uri mediaUri, final boolean isIncoming) {

        if (mimeType.startsWith("audio") && mAudioContainer != null) {
            mAudioContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MessageListItem) itemView).onClickMediaIcon(mimeType, mediaUri, isIncoming);
                }
            });

        } else {
            mMediaThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MessageListItem) itemView).onClickMediaIcon(mimeType, mediaUri, isIncoming);
                }
            });
        }
    }

    public void setPosition(int position) {
        this.Pos = position;
    }

    public int getPos() {
        return this.Pos;
    }

    public void resetOnClickListenerMediaThumbnail() {
        mMediaThumbnail.setOnClickListener(null);
    }

    long mTimeDiff = -1;

}

