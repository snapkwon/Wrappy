/*
 * Copyright (C) 2008 Esmertec AG. Copyright (C) 2008 The Android Open Source
 * Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.wrappy.im.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.wrappy.im.ImApp;
import net.wrappy.im.ImUrlActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.Store;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.ui.onboarding.OnboardingManager;
import net.wrappy.im.ui.widgets.ImageViewActivity;
import net.wrappy.im.ui.widgets.MessageViewHolder;
import net.wrappy.im.util.ConferenceUtils;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.LinkifyHelper;
import net.wrappy.im.util.PopupUtils;
import net.wrappy.im.util.SecureMediaStore;
import net.wrappy.im.util.Utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.net.URLConnection;
import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageListItem extends FrameLayout {

    public enum DeliveryState {
        NEUTRAL, DELIVERED, UNDELIVERED
    }

    public enum EncryptionState {
        NONE, ENCRYPTED, ENCRYPTED_AND_VERIFIED

    }

    private String lastMessage = null;
    private Uri mediaUri = null;
    private String mimeType = null;

    private Context context;
    private boolean linkify = false;

    private MessageViewHolder mHolder = null;

    private static PrettyTime sPrettyTime = null;

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        sPrettyTime = new PrettyTime(getCurrentLocale());


    }


    /**
     * This trickery is needed in order to have clickable links that open things
     * in a new {@code Task} rather than in ChatSecure's {@code Task.} Thanks to @commonsware
     * https://stackoverflow.com/a/11417498
     */
    class NewTaskUrlSpan extends ClickableSpan {

        private String urlString;

        NewTaskUrlSpan(String urlString) {
            this.urlString = urlString;
        }

        @Override
        public void onClick(View widget) {

            OnboardingManager.DecodedInviteLink diLink = OnboardingManager.decodeInviteLink(urlString);

            //not an invite link, so just send it out
            if (diLink == null) {
                Uri uri = Uri.parse(urlString);
                Context context = widget.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                //it is an invite link, so target it back at us!
                Uri uri = Uri.parse(urlString);
                Context context = widget.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                intent.setPackage(context.getPackageName()); //The package name of the app to which intent is to be sent
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    class URLSpanConverter implements LinkifyHelper.SpanConverter<URLSpan, ClickableSpan> {
        @Override
        public NewTaskUrlSpan convert(URLSpan span) {
            return (new NewTaskUrlSpan(span.getURL()));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setLinkify(boolean linkify) {
        this.linkify = linkify;
    }

    public URLSpan[] getMessageLinks() {
        return mHolder.mTextViewForMessages.getUrls();
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void bindIncomingMessage(MessageViewHolder holder, int id, int messageType, String address, String nickname, final String mimeType, final String body, Date date,
                                    boolean scrolling, EncryptionState encryption, boolean showContact, int presenceStatus, String textsearch) {


        mHolder = holder;
        applyStyleColors();
        mHolder.mTextViewForMessages.setVisibility(View.VISIBLE);
        mHolder.mAudioContainer.setVisibility(View.GONE);
        mHolder.mMediaContainer.setVisibility(View.GONE);

        if (TextUtils.isEmpty(nickname)) {
            nickname = ImApp.getNickname(address);
        }

        if (nickname.contains("@")) {
            nickname = nickname.split("@")[0];
        }

        lastMessage = formatMessage(body);
        showAvatar(address, nickname);

        mHolder.resetOnClickListenerMediaThumbnail();

        if (mimeType != null) {

            Uri mediaUri = Uri.parse(body);
            lastMessage = "";

            if (mediaUri != null && mediaUri.getScheme() != null) {
                if (mimeType.startsWith("audio")/* || mimeType.equals("application/octet-stream")*/) {
                    mHolder.mAudioButton.setImageResource(R.drawable.media_audio_play);

                    try {
                        mHolder.mAudioContainer.setVisibility(View.VISIBLE);
                        showAudioPlayer(mimeType, mediaUri, id, mHolder);
                    } catch (Exception e) {
                        mHolder.mAudioContainer.setVisibility(View.GONE);
                    }

                } else {
                    mHolder.mTextViewForMessages.setVisibility(View.GONE);
                    boolean isJpeg = mimeType.contains("jpg") || mimeType.contains("jpeg");

                    showMediaThumbnail(mimeType, mediaUri, id, mHolder, isJpeg, true);

                    mHolder.mMediaContainer.setVisibility(View.VISIBLE);

                }
            }

        } else if ((!TextUtils.isEmpty(lastMessage)) && (lastMessage.charAt(0) == '/' || lastMessage.charAt(0) == ':')) {
            boolean cmdSuccess = false;

            if (lastMessage.startsWith("/sticker:")) {
                String[] cmds = lastMessage.split(":");

                String mimeTypeSticker = "image/png";

                try {

                    String assetPath = cmds[1].split(" ")[0].toLowerCase();//just get up to any whitespace;

                    //make sure sticker exists
                    AssetFileDescriptor afd = getContext().getAssets().openFd(assetPath);
                    afd.getLength();
                    afd.close();

                    //now setup the new URI for loading local sticker asset
                    Uri mediaUri = Uri.parse("asset://localhost/" + assetPath);

                    //now load the thumbnail
                    cmdSuccess = showMediaThumbnail(mimeTypeSticker, mediaUri, id, mHolder, false, true);
                } catch (Exception e) {
                    Log.e(ImApp.LOG_TAG, "error loading sticker bitmap: " + cmds[1], e);
                    cmdSuccess = false;
                }

            } else if (lastMessage.startsWith(ConferenceConstant.CONFERENCE_PREFIX)) {
                bindConference(lastMessage);
                cmdSuccess = true;
            } else if (lastMessage.startsWith(ConferenceConstant.SEND_LOCATION_FREFIX)) {
                bindLocation(lastMessage);
                cmdSuccess = true;
            } else if (lastMessage.startsWith(ConferenceConstant.DELETE_GROUP_BY_ADMIN)) {
                deleteAndLeaveGroup();
            } else if (lastMessage.startsWith(ConferenceConstant.REMOVE_MEMBER_GROUP_BY_ADMIN)) {
                bindRemoveMemberGroup(lastMessage);
                cmdSuccess = true;
            } else if (lastMessage.startsWith(":")) {
                cmdSuccess = bindSticker(lastMessage, id);
            }
            if (!cmdSuccess) {
                mHolder.mTextViewForMessages.setText(new SpannableString(lastMessage));
            } else {
                mHolder.mContainer.setBackgroundResource(android.R.color.transparent);
                lastMessage = "";
            }

        } else {

            //  if (isSelected())
            //    mHolder.mContainer.setBackgroundColor(getResources().getColor(R.color.holo_blue_bright));

            if (lastMessage.length() > 0) {
                if (textsearch.isEmpty()) {
                    mHolder.mTextViewForMessages.setText(new SpannableString((lastMessage)));
                } else {
                    lastMessage = lastMessage.replaceAll(textsearch, "<font color='red'>" + textsearch + "</font>");
                    mHolder.mTextViewForMessages.setText(new SpannableString(Html.fromHtml(lastMessage)));
                }

            } else {
                mHolder.mTextViewForMessages.setText(lastMessage);
            }
        }


        if (date != null) {

            String contact = null;
            if (showContact) {
                String[] nickParts = nickname.split("/");
                contact = nickParts[nickParts.length - 1];
                String fName = Imps.Contacts.getNicknameFromAddress(ImApp.sImApp.getContentResolver(),contact + Constant.EMAIL_DOMAIN);
                if (!TextUtils.isEmpty(fName)) {
                    contact = fName;
                }
            }

            CharSequence tsText = formatTimeStamp(date, messageType, null, encryption, contact);


            mHolder.mTextViewForTimestamp.setText(tsText);
            mHolder.mTextViewForTimestamp.setVisibility(View.VISIBLE);

        } else {

            mHolder.mTextViewForTimestamp.setText("");
            //mHolder.mTextViewForTimestamp.setVisibility(View.GONE);

        }
        if (linkify)
            LinkifyHelper.addLinks(mHolder.mTextViewForMessages, new URLSpanConverter());
        LinkifyHelper.addTorSafeLinks(mHolder.mTextViewForMessages);
    }

    private void deleteAndLeaveGroup() {
        String groupName = lastMessage.split(":")[2];
        AppFuncs.alert(context, groupName + " is deleted by admin", true);
        ((ConversationDetailActivity) context).finish();
    }

    private void bindRemoveMemberGroup(String lastMessage) {
        if (mHolder != null) {
            String member = lastMessage.split(":")[2];
            mHolder.mTextViewForMessages.setText(new SpannableString(member + " is removed by admin"));
        }
    }

    private void bindBackground(String lastMessage) {
        String message = lastMessage.replace(ConferenceConstant.SEND_BACKGROUND_CHAT_PREFIX, "");
    }

    private void bindConference(String lastMessage) {
        String callDes = ConferenceUtils.convertConferenceMessage(lastMessage);
        if (mHolder != null) {
            mHolder.mTextViewForMessages.setText(callDes);
        }
    }

    private void bindLocation(String lastMessage) {
        String latLng = lastMessage.replace(ConferenceConstant.SEND_LOCATION_FREFIX, "");
        final String[] coordinates = latLng.split(":");
        if (mHolder != null && coordinates.length != 0) {
            Glide.with(context).load(ConferenceUtils.getGoogleMapThumbnail(coordinates[0], coordinates[1])).into(mHolder.mMediaThumbnail);
            mHolder.mMediaThumbnail.setVisibility(VISIBLE);
            mHolder.mTextViewForMessages.setVisibility(View.GONE);
            mHolder.mMediaContainer.setVisibility(View.VISIBLE);
            mHolder.mContainer.setBackgroundResource(android.R.color.transparent);
            mHolder.mMediaThumbnail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMapIntent(coordinates[0], coordinates[1]);
                }
            });
        } else {

        }
    }

    private void openMapIntent(String lat, String lng) {
        String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                AppFuncs.alert(context, "Please install a maps application", true);
            }
        }
    }

    private boolean showMediaThumbnail(String mimeType, Uri mediaUri, int id, MessageViewHolder holder, boolean centerCrop) {
        return showMediaThumbnail(mimeType, mediaUri, id, holder, centerCrop, false);
    }

    private boolean showMediaThumbnail(String mimeType, Uri mediaUri, int id, MessageViewHolder holder, boolean centerCrop, boolean isIncoming) {
        this.mediaUri = mediaUri;
        this.mimeType = mimeType;

        /* Guess the MIME type in case we received a file that we can display or play*/
        if (TextUtils.isEmpty(mimeType) || mimeType.startsWith("application")) {
            String guessed = URLConnection.guessContentTypeFromName(mediaUri.toString());
            if (!TextUtils.isEmpty(guessed)) {
                if (TextUtils.equals(guessed, "video/3gpp"))
                    mimeType = "audio/3gpp";
                else
                    mimeType = guessed;
            }
        }

        holder.mTextViewForMessages.setText(lastMessage);
        holder.mTextViewForMessages.setVisibility(View.GONE);
        holder.btntranslate.setVisibility(View.GONE);


        if (centerCrop)
            holder.mMediaThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        else
            holder.mMediaThumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (mimeType.startsWith("image/")) {
            setImageThumbnail(getContext().getContentResolver(), id, holder, mediaUri);
            holder.mMediaThumbnail.setBackgroundResource(android.R.color.transparent);
            holder.setOnClickListenerMediaThumbnail(mimeType, mediaUri, false);

        } else {
            holder.mMediaThumbnail.setImageResource(R.drawable.ic_file); // generic file icon
            holder.mTextViewForMessages.setText(mediaUri.getLastPathSegment() + " (" + mimeType + ")");
            holder.mTextViewForMessages.setVisibility(View.VISIBLE);
            holder.setOnClickListenerMediaThumbnail(mimeType, mediaUri, isIncoming);
        }

        holder.mMediaContainer.setVisibility(View.VISIBLE);
        holder.mContainer.setBackgroundResource(android.R.color.transparent);

        return true;

    }

    private void showAudioPlayer(String mimeType, Uri mediaUri, int id, final MessageViewHolder holder) throws Exception {
        /* Guess the MIME type in case we received a file that we can display or play*/
        if (TextUtils.isEmpty(mimeType) || mimeType.startsWith("application")) {
            String guessed = URLConnection.guessContentTypeFromName(mediaUri.toString());
            if (!TextUtils.isEmpty(guessed)) {
                if (TextUtils.equals(guessed, "video/3gpp"))
                    mimeType = "audio/3gpp";
                else
                    mimeType = guessed;
            }
        }

        holder.setOnClickListenerMediaThumbnail(mimeType, mediaUri, false);
        mHolder.mTextViewForMessages.setText("");

        holder.mContainer.setBackgroundResource(android.R.color.transparent);
    }

    protected String convertMediaUriToPath(Uri uri) {
        String path = null;

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null && (!cursor.isClosed())) {
            if (cursor.isBeforeFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                path = cursor.getString(column_index);
            }

            cursor.close();
        }

        return path;
    }

    public static AudioPlayer mAudioPlayer;

    public void onClickAudioIcon(String mimeType, final String mediaUri) throws Exception {
        Debug.d(mediaUri);

        if (mAudioPlayer == null || !mediaUri.equals(mAudioPlayer.getFileName())) {
            if (mAudioPlayer != null)
                mAudioPlayer.stop();

            mAudioPlayer = new AudioPlayer(getContext(), mediaUri, mimeType, mHolder.mVisualizerView, mHolder.mTextViewForMessages);
            mAudioPlayer.setOnFinishPlaying(new AudioPlayer.OnFinishPlaying() {
                @Override
                public void onFinishPlaying() {
                    if (mHolder.mAudioButton != null)
                        mHolder.mAudioButton.setImageResource(R.drawable.media_audio_play);
                }
            });
        }

        if (mAudioPlayer.getDuration() != -1)
            mHolder.mTextViewForMessages.setText(Utils.formatDurationMedia(mAudioPlayer.getDuration()));

        if (mAudioPlayer.isPlaying()) {
            mHolder.mAudioButton.setImageResource(R.drawable.media_audio_play);
            mAudioPlayer.pause();
        } else {
            mHolder.mAudioButton.setImageResource(R.drawable.media_audio_pause);
            mAudioPlayer.play();
        }
    }

    public void onClickMediaIcon(String mimeType, final Uri mediaUri, boolean isIncoming) {


        if (mimeType.startsWith("image")) {

            Intent intent = new Intent(context, ImageViewActivity.class);
            intent.putExtra(ImageViewActivity.URI, mediaUri.toString());
            intent.putExtra(ImageViewActivity.MIMETYPE, mimeType);

            context.startActivity(intent);

        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                if (isIncoming)
                    PopupUtils.showOKCancelDialog(context, "", context.getString(R.string.message_download_attachment), new OnClickListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onClick(View v) {
                            AppFuncs.openPickFolder((Activity) context, ConversationDetailActivity.REQUEST_PICK_FOLDER, mediaUri);
                        }
                    }, null);
//            exportMediaFile();
        }
    }

    private void forwardMediaFile(String mimeType, Uri mediaUri) {

        String resharePath = "vfs:/" + mediaUri.getPath();
        Intent shareIntent = new Intent(context, ImUrlActivity.class);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setDataAndType(Uri.parse(resharePath), mimeType);
        context.startActivity(shareIntent);


    }

    public void forwardMediaFile() {
        if (mimeType != null && mediaUri != null) {
            forwardMediaFile(mimeType, mediaUri);
        } else {
            Intent shareIntent = new Intent(context, ImUrlActivity.class);
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, lastMessage);
            shareIntent.setType("text/plain");
            context.startActivity(shareIntent);
        }
    }

    public void exportMediaFile() {
        if (mimeType != null && mediaUri != null) {
            java.io.File exportPath = SecureMediaStore.exportPath(mimeType, mediaUri);
            exportMediaFile(mimeType, mediaUri, exportPath, true);
        } else {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, lastMessage);
            shareIntent.setType("text/plain");
            context.startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.export_media)));
        }

    }

    ;

    private void exportMediaFile(String mimeType, Uri mediaUri, java.io.File exportPath, boolean doView) {
        try {

            SecureMediaStore.exportContent(mimeType, mediaUri, exportPath);
            Intent shareIntent = new Intent();

            if (doView) {
                shareIntent.setDataAndType(Uri.fromFile(exportPath), mimeType);
                shareIntent.setAction(Intent.ACTION_VIEW);
                context.startActivity(shareIntent);
            } else {
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportPath));
                shareIntent.setType(mimeType);
                shareIntent.setAction(Intent.ACTION_SEND);
                context.startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.export_media)));

            }
        } catch (Exception e) {
            AppFuncs.alert(getContext(), "Export Failed " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    /**
     * @param contentResolver
     * @param id
     * @param aHolder
     * @param mediaUri
     */
    private void setImageThumbnail(final ContentResolver contentResolver, final int id, final MessageViewHolder aHolder, final Uri mediaUri) {

        //if the same URI, we don't need to reload
        if (aHolder.mMediaUri != null
                && aHolder.mMediaUri.getPath() != null
                && aHolder.mMediaUri.getPath().equals(mediaUri.getPath()))
            return;

        // pair this holder to the uri. if the holder is recycled, the pairing is broken
        aHolder.mMediaUri = mediaUri;
        // if a content uri - already scanned

        Glide.clear(aHolder.mMediaThumbnail);
        if (SecureMediaStore.isVfsUri(mediaUri)) {
            try {
                info.guardianproject.iocipher.File fileImage = new info.guardianproject.iocipher.File(mediaUri.getPath());
                if (fileImage.exists()) {
                    Glide.with(context)
                            .load(new info.guardianproject.iocipher.FileInputStream(fileImage))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(aHolder.mMediaThumbnail);
                }
            } catch (Exception e) {
                Log.w(ImApp.LOG_TAG, "unable to load thumbnail: " + mediaUri.toString());
            }
        } else if (mediaUri.getScheme() != null
                && mediaUri.getScheme().equals("asset")) {
            String assetPath = "file:///android_asset/" + mediaUri.getPath().substring(1);
            Glide.with(context)
                    .load(assetPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(aHolder.mMediaThumbnail);
        } else {
            Glide.with(context)
                    .load(mediaUri)
                    .into(aHolder.mMediaThumbnail);
        }


    }

    private String formatMessage(String body) {

        if (body != null)
            try {
                return (android.text.Html.fromHtml(body).toString()); //this happens on Xiaomi sometimes
            } catch (RuntimeException re) {
                return "";
            }
        else
            return "";
    }

    private Spannable highlight(int color, Spannable original, String word) {
        String normalized = Normalizer.normalize(original, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        int start = normalized.indexOf(word);
        if (start < 0) {
            return original;
        } else {
            Spannable highlighted = new SpannableString(original);
            while (start >= 0) {
                int spanStart = Math.min(start, original.length());
                int spanEnd = Math.min(start + word.length(), original.length());

                highlighted.setSpan(new ForegroundColorSpan(color), spanStart,
                        spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                start = normalized.indexOf(word, spanEnd);
            }
            return highlighted;
        }
    }

    public void bindOutgoingMessage(MessageViewHolder holder, int id, int messageType, String address, final String mimeType, final String body, Date date, boolean scrolling,
                                    DeliveryState delivery, EncryptionState encryption, String textsearch) {

        mHolder = holder;
        applyStyleColors();

        mHolder.mTextViewForMessages.setVisibility(View.VISIBLE);
        mHolder.mAudioContainer.setVisibility(View.GONE);
        mHolder.mMediaContainer.setVisibility(View.GONE);
        mHolder.mAudioButton.setImageResource(R.drawable.media_audio_play);

        showAvatar(address, Store.getStringData(context, Store.USERNAME));
        mHolder.resetOnClickListenerMediaThumbnail();

        lastMessage = body;

        if (mimeType != null) {

            lastMessage = "";

            String mediaPath = body;

            if (body.contains(" "))
                mediaPath = body.split(" ")[0];

            Uri mediaUri = Uri.parse(mediaPath);

            if (mimeType.startsWith("audio")) {
                try {
                    mHolder.mAudioContainer.setVisibility(View.VISIBLE);
                    showAudioPlayer(mimeType, mediaUri, id, mHolder);
                } catch (Exception e) {
                    mHolder.mAudioContainer.setVisibility(View.GONE);
                }

            } else {
                mHolder.mTextViewForMessages.setVisibility(View.GONE);

                mHolder.mMediaContainer.setVisibility(View.VISIBLE);

                boolean isJpeg = mimeType.contains("jpg") || mimeType.contains("jpeg");
                showMediaThumbnail(mimeType, mediaUri, id, mHolder, isJpeg);

            }

        } else if ((!TextUtils.isEmpty(lastMessage)) && (lastMessage.charAt(0) == '/' || lastMessage.charAt(0) == ':')) {
//            String cmd = lastMessage.toString().substring(1);
            boolean cmdSuccess = false;

            if (lastMessage.startsWith("/sticker:")) {
                String[] cmds = lastMessage.split(":");

                String mimeTypeSticker = "image/png";
                try {
                    //make sure sticker exists
                    AssetFileDescriptor afd = getContext().getAssets().openFd(cmds[1]);
                    afd.getLength();
                    afd.close();

                    //now setup the new URI for loading local sticker asset
                    Uri mediaUri = Uri.parse("asset://localhost/" + cmds[1]);

                    //now load the thumbnail
                    cmdSuccess = showMediaThumbnail(mimeTypeSticker, mediaUri, id, mHolder, false);
                } catch (Exception e) {
                    cmdSuccess = false;
                }

            } else if (lastMessage.startsWith(ConferenceConstant.CONFERENCE_PREFIX)) {
                bindConference(lastMessage);
                cmdSuccess = true;
            } else if (lastMessage.startsWith(ConferenceConstant.SEND_LOCATION_FREFIX)) {
                bindLocation(lastMessage);
                cmdSuccess = true;
            } else if (lastMessage.startsWith(ConferenceConstant.REMOVE_MEMBER_GROUP_BY_ADMIN)) {
                bindRemoveMemberGroup(lastMessage);
                cmdSuccess = true;
            } else if (lastMessage.startsWith(":")) {
                cmdSuccess = bindSticker(lastMessage, id);
            }

            if (!cmdSuccess) {
                mHolder.mTextViewForMessages.setText(new SpannableString(lastMessage));
            } else {
                holder.mContainer.setBackgroundResource(android.R.color.transparent);
                lastMessage = "";
            }

        } else {
            if (textsearch.isEmpty()) {
                mHolder.mTextViewForMessages.setText(new SpannableString((lastMessage)));
            } else {
                lastMessage = lastMessage.replaceAll(textsearch, "<font color='red'>" + textsearch + "</font>");
                mHolder.mTextViewForMessages.setText(new SpannableString(Html.fromHtml(lastMessage)));
            }
        }

        //if (isSelected())
        //  mHolder.mContainer.setBackgroundColor(getResources().getColor(R.color.holo_blue_bright));

        if (date != null) {

            CharSequence tsText = formatTimeStamp(date, messageType, delivery, encryption, null);

            mHolder.mTextViewForTimestamp.setText(tsText);

            mHolder.mTextViewForTimestamp.setVisibility(View.VISIBLE);

        } else {
            mHolder.mTextViewForTimestamp.setText("");

        }
        if (linkify)
            LinkifyHelper.addLinks(mHolder.mTextViewForMessages, new URLSpanConverter());
        LinkifyHelper.addTorSafeLinks(mHolder.mTextViewForMessages);
    }

    private void showAvatar(String address, String nickname) {
        if (mHolder.mAvatar == null)
            return;

        String reference = Imps.Avatars.getAvatar(context.getContentResolver(), address);
        if (!TextUtils.isEmpty(reference)) {
            GlideHelper.loadBitmapToCircleImageDefault(getContext(), mHolder.mAvatar, RestAPI.getAvatarUrl(reference), nickname);
        } else {
            GlideHelper.loadAvatarFromNickname(getContext(), mHolder.mAvatar, nickname);
        }
    }

    /**
     * public int getAvatarBorder(int status) {
     * switch (status) {
     * case Presence.AVAILABLE:
     * return (getResources().getColor(R.color.holo_green_light));
     * <p>
     * case Presence.IDLE:
     * return (getResources().getColor(R.color.holo_green_dark));
     * case Presence.AWAY:
     * return (getResources().getColor(R.color.holo_orange_light));
     * <p>
     * case Presence.DO_NOT_DISTURB:
     * return(getResources().getColor(R.color.holo_red_dark));
     * <p>
     * case Presence.OFFLINE:
     * return(getResources().getColor(R.color.holo_grey_dark));
     * <p>
     * default:
     * }
     * <p>
     * return Color.TRANSPARENT;
     * }
     **/

    public void bindPresenceMessage(MessageViewHolder holder, String contact, int type, Date date, boolean isGroupChat, boolean scrolling) {

        mHolder = holder;
        mHolder.mContainer.setBackgroundResource(android.R.color.transparent);
        mHolder.mTextViewForMessages.setVisibility(View.GONE);
        mHolder.mTextViewForTimestamp.setVisibility(View.VISIBLE);

        CharSequence message = formatPresenceUpdates(contact, type, date, isGroupChat, scrolling);
        mHolder.mTextViewForTimestamp.setText(message);


    }

    public void bindErrorMessage(int errCode) {

        mHolder = (MessageViewHolder) getTag();

        mHolder.mTextViewForMessages.setText(R.string.msg_sent_failed);
        mHolder.mTextViewForMessages.setTextColor(getResources().getColor(R.color.error));

    }

    private SpannableString formatTimeStamp(Date date, int messageType, MessageListItem.DeliveryState delivery, EncryptionState encryptionState, String nickname) {


        StringBuilder deliveryText = new StringBuilder();

        if (nickname != null) {
            deliveryText.append(nickname);
            deliveryText.append(' ');
        }
        String formatText = sPrettyTime.format(date);
        if (formatText.contains("さっき"))// cheat japanese
            formatText = "Now";

        deliveryText.append(formatText);

        SpannableString spanText = null;

        spanText = new SpannableString(deliveryText.toString());

        if (delivery != null) {
            deliveryText.append(' ');
            //this is for delivery

            if (messageType == Imps.MessageType.QUEUED) {
                //do nothing
                deliveryText.append("X");
                spanText = new SpannableString(deliveryText.toString());
                int len = spanText.length();
                spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_message_wait_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (delivery == DeliveryState.DELIVERED) {

                if (encryptionState == EncryptionState.ENCRYPTED || encryptionState == EncryptionState.ENCRYPTED_AND_VERIFIED) {
                    deliveryText.append("X");
                    spanText = new SpannableString(deliveryText.toString());
                    int len = spanText.length();

                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_delivered_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_encrypted_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                } else {
                    deliveryText.append("X");
                    spanText = new SpannableString(deliveryText.toString());
                    int len = spanText.length();
                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_delivered_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }


            } else if (delivery == DeliveryState.UNDELIVERED) {

                if (encryptionState == EncryptionState.ENCRYPTED || encryptionState == EncryptionState.ENCRYPTED_AND_VERIFIED) {
                    deliveryText.append("X");
                    spanText = new SpannableString(deliveryText.toString());
                    int len = spanText.length();
                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_sent_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_encrypted_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    deliveryText.append("X");
                    spanText = new SpannableString(deliveryText.toString());
                    int len = spanText.length();
                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_sent_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }


            } else if (delivery == DeliveryState.NEUTRAL) {

                if (encryptionState == EncryptionState.ENCRYPTED || encryptionState == EncryptionState.ENCRYPTED_AND_VERIFIED) {
                    deliveryText.append("X");
                    spanText = new SpannableString(deliveryText.toString());
                    int len = spanText.length();
                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_sent_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_encrypted_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    deliveryText.append("X");
                    spanText = new SpannableString(deliveryText.toString());
                    int len = spanText.length();
                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_sent_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                }

            }


        } else {
            if (encryptionState == EncryptionState.ENCRYPTED || encryptionState == EncryptionState.ENCRYPTED_AND_VERIFIED) {
                deliveryText.append(' ');
                spanText = new SpannableString(deliveryText.toString());
                int len = spanText.length();

//                if (encryptionState == EncryptionState.ENCRYPTED || encryptionState == EncryptionState.ENCRYPTED_AND_VERIFIED)
//                    spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_encrypted_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (messageType == Imps.MessageType.OUTGOING) {
                //do nothing
                deliveryText.append("X");
                spanText = new SpannableString(deliveryText.toString());
                int len = spanText.length();
                spanText.setSpan(new ImageSpan(getContext(), R.drawable.ic_message_wait_grey), len - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spanText;
    }

    private boolean bindSticker(String message, int id) {
        String[] cmds = message.split(":");
        boolean cmdSuccess = false;
        String mimeTypeSticker = "image/png";
        try {
            String[] stickerParts = cmds[1].split("-");
            String stickerPath = "stickers/" + stickerParts[0].toLowerCase() + "/" + stickerParts[1] + ".png";

            //make sure sticker exists
            AssetFileDescriptor afd = getContext().getAssets().openFd(stickerPath);
            afd.getLength();
            afd.close();

            //now setup the new URI for loading local sticker asset
            Uri mediaUri = Uri.parse("asset://localhost/" + stickerPath);

            //now load the thumbnail
            cmdSuccess = showMediaThumbnail(mimeTypeSticker, mediaUri, id, mHolder, false);
        } catch (Exception e) {
            cmdSuccess = false;
        }
        return cmdSuccess;
    }

    private CharSequence formatPresenceUpdates(String contact, int type, Date date, boolean isGroupChat,
                                               boolean scrolling) {
        String body;

        Resources resources = getResources();

        switch (type) {
            case Imps.MessageType.PRESENCE_AVAILABLE:
                body = resources.getString(isGroupChat ? R.string.contact_joined
                        : R.string.contact_online, contact);
                break;

            case Imps.MessageType.PRESENCE_AWAY:
                body = resources.getString(R.string.contact_away, contact);
                break;

            case Imps.MessageType.PRESENCE_DND:
                body = resources.getString(R.string.contact_busy, contact);
                break;

            case Imps.MessageType.PRESENCE_UNAVAILABLE:
                body = resources.getString(isGroupChat ? R.string.contact_left
                        : R.string.contact_offline, contact);
                break;

            default:
                return null;
        }

        body += " - ";
        body += formatTimeStamp(date, type, null, EncryptionState.NONE, null);

        if (scrolling) {
            return body;
        } else {
            SpannableString spanText = new SpannableString(body);
            int len = spanText.length();
            spanText.setSpan(new StyleSpan(Typeface.ITALIC), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new RelativeSizeSpan((float) 0.8), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spanText;
        }
    }

    /**
     * public void setAvatarBorder(int status, RoundedAvatarDrawable avatar) {
     * switch (status) {
     * case Presence.AVAILABLE:
     * avatar.setBorderColor(getResources().getColor(R.color.holo_green_light));
     * break;
     * <p>
     * case Presence.IDLE:
     * avatar.setBorderColor(getResources().getColor(R.color.holo_green_dark));
     * <p>
     * break;
     * <p>
     * case Presence.AWAY:
     * avatar.setBorderColor(getResources().getColor(R.color.holo_orange_light));
     * break;
     * <p>
     * case Presence.DO_NOT_DISTURB:
     * avatar.setBorderColor(getResources().getColor(R.color.holo_red_dark));
     * <p>
     * break;
     * <p>
     * case Presence.OFFLINE:
     * avatar.setBorderColor(getResources().getColor(R.color.holo_grey_light));
     * <p>
     * break;
     * <p>
     * <p>
     * default:
     * }
     * }
     **/

    public void applyStyleColors() {
        //not set color
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        int themeColorHeader = settings.getInt("themeColor", -1);
        int themeColorText = settings.getInt("themeColorText", -1);
        int themeColorBg = settings.getInt("themeColorBg", -1);

        if (mHolder != null) {
            if (themeColorText != -1) {
                if (mHolder.mTextViewForMessages != null)
                    mHolder.mTextViewForMessages.setTextColor(themeColorText);

                if (mHolder.mTextViewForTimestamp != null)
                    mHolder.mTextViewForTimestamp.setTextColor(themeColorText);

            }

            if (themeColorBg != -1) {

                int textBubbleBg = Utils.getContrastColor(themeColorText);
                if (textBubbleBg == Color.BLACK)
                    mHolder.mContainer.setBackgroundResource(R.drawable.message_view_rounded_dark);
                else
                    mHolder.mContainer.setBackgroundResource(R.drawable.message_view_rounded_light);

                //mHolder.mContainer.setBackgroundResource(android.R.color.transparent);
                //mHolder.mContainer.setBackgroundColor(themeColorBg);
            } else {
                mHolder.mContainer.setBackgroundResource(R.drawable.message_view_rounded_light);

            }
        }

    }

    public Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }
}
