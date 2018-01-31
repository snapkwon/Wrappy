/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wrappy.im.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.comon.BaseFragmentV4;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.tasks.MigrateAccountTask;
import net.wrappy.im.ui.conversation.CustomBottomSheetDialogFragment;
import net.wrappy.im.ui.widgets.ConversationViewHolder;

public class ConversationListFragment extends BaseFragmentV4 {

    private ConversationListRecyclerViewAdapter mAdapter = null;
    private Uri mUri;
    private MyLoaderCallbacks mLoaderCallbacks;
    private LoaderManager mLoaderManager;
    private int mLoaderId = 1001;
    private RecyclerView mRecView;

    private int mChatType = Imps.Chats.CHAT_TYPE_ACTIVE; //default chat type

    private View mEmptyView;
    private View mEmptyViewImage;

    private TextView mUpgradeDesc;
    private ImageView mUpgradeImage;
    private Button mUpgradeAction;

    private boolean mFilterArchive = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.awesome_fragment_message_list, container, false);

        mRecView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mEmptyView = view.findViewById(R.id.empty_view);

        mUpgradeImage = (ImageView) view.findViewById(R.id.upgrade_view_image);
        mUpgradeDesc = (TextView) view.findViewById(R.id.upgrade_view_text);
        mUpgradeAction = (Button) view.findViewById(R.id.upgrade_action);

        mUpgradeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUpgrade();
            }
        });

        mEmptyViewImage = view.findViewById(R.id.empty_view_image);
        mEmptyViewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity) getActivity()).inviteContact();
            }
        });

        setupRecyclerView(mRecView);

        //not set color
        /**
         final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
         int themeColorBg = settings.getInt("themeColorBg",-1);
         view.setBackgroundColor(themeColorBg);
         */

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLoaderManager != null)
            mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        Uri baseUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS_BY;
        Uri.Builder builder = baseUri.buildUpon();
        mUri = builder.build();

        mLoaderManager = getLoaderManager();
        mLoaderCallbacks = new MyLoaderCallbacks();
        mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);

        Cursor cursor = null;
        mAdapter = new ConversationListRecyclerViewAdapter(getActivity(), cursor);
    }

    public boolean getArchiveFilter() {
        return mFilterArchive;
    }

    public void setArchiveFilter(boolean filterAchive) {
        mFilterArchive = filterAchive;

        if (mFilterArchive)
            mChatType = Imps.Chats.CHAT_TYPE_ARCHIVED;
        else
            mChatType = Imps.Chats.CHAT_TYPE_ACTIVE;

        if (mLoaderManager != null)
            mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
    }

    private void endConversation(long itemId) {
        Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, itemId);
        getActivity().getContentResolver().delete(chatUri, null, null);

    }

    private void archiveConversation(long itemId) {
        Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, itemId);
        ContentValues values = new ContentValues();
        values.put(Imps.Chats.CHAT_TYPE, Imps.Chats.CHAT_TYPE_ARCHIVED);
        getActivity().getContentResolver().update(chatUri, values, Imps.Chats.CONTACT_ID + "=" + itemId, null);
    }

    private void unarchiveConversation(long itemId) {
        Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, itemId);
        ContentValues values = new ContentValues();
        values.put(Imps.Chats.CHAT_TYPE, Imps.Chats.CHAT_TYPE_ACTIVE);
        getActivity().getContentResolver().update(chatUri, values, Imps.Chats.CONTACT_ID + "=" + itemId, null);
    }

    public void pinConversation(long itemId, Context context) {
        Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, itemId);
        ContentValues values = new ContentValues();
        values.put(Imps.Chats.CHAT_FAVORITE, Imps.Chats.CHAT_PIN);
        if (context != null) {
            context.getContentResolver().update(chatUri, values, Imps.Chats.CONTACT_ID + "=" + itemId, null);
        }
    }

    public void unpinConversation(long itemId, Context context) {
        Uri chatUri = ContentUris.withAppendedId(Imps.Chats.CONTENT_URI, itemId);
        ContentValues values = new ContentValues();
        values.put(Imps.Chats.CHAT_FAVORITE, Imps.Chats.CHAT_UNPIN);
        if (context != null) {
            context.getContentResolver().update(chatUri, values, Imps.Chats.CONTACT_ID + "=" + itemId, null);
        }
    }

    @Override
    public void reloadFragment() {

    }

    public static class ConversationListRecyclerViewAdapter
            extends CursorRecyclerViewAdapter<ConversationViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private Context mContext;
        private CustomBottomSheetDialogFragment mBottomSheet = null;


        public ConversationListRecyclerViewAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mContext = context;
            setHasStableIds(true);
        }

        public long getItemId(int position) {
            Cursor c = getCursor();
            c.moveToPosition(position);
            long chatId = c.getLong(ConversationListItem.COLUMN_CONTACT_ID);
            return chatId;
        }


        @Override
        public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.conversation_view, parent, false);
            view.setBackgroundResource(mBackground);

            ConversationViewHolder viewHolder = (ConversationViewHolder) view.getTag();

            if (viewHolder == null) {
                viewHolder = new ConversationViewHolder(view);
                view.setTag(viewHolder);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ConversationViewHolder viewHolder, Cursor cursor, int position) {

            if (TextUtils.isEmpty(mSearchString)) {
                String s = DatabaseUtils.dumpCursorToString(cursor);
                final long chatId = cursor.getLong(ConversationListItem.COLUMN_CONTACT_ID);
                final String address = cursor.getString(ConversationListItem.COLUMN_CONTACT_USERNAME);
                final String nickname = cursor.getString(ConversationListItem.COLUMN_CONTACT_NICKNAME);

                final long providerId = cursor.getLong(ConversationListItem.COLUMN_CONTACT_PROVIDER);
                final long accountId = cursor.getLong(ConversationListItem.COLUMN_CONTACT_ACCOUNT);
                final int type = cursor.getInt(ConversationListItem.COLUMN_CONTACT_TYPE);
                final String lastMsg = cursor.getString(ConversationListItem.COLUMN_LAST_MESSAGE);

                long lastMsgDate = cursor.getLong(ConversationListItem.COLUMN_LAST_MESSAGE_DATE);
                final int presence = cursor.getInt(ConversationListItem.COLUMN_CONTACT_PRESENCE_STATUS);

                final int chatFavorite = cursor.getInt(ConversationListItem.COLUMN_CHAT_FAVORITE);
                final String reference = cursor.getString(ConversationListItem.COLUMN_AVATAR_HASH);

                String lastMsgType = null;
                if (!TextUtils.isEmpty(lastMsg)) {
                    if (lastMsg.endsWith(".jpg") || lastMsg.endsWith(".png") || lastMsg.endsWith(".gif"))
                        lastMsgType = "image/*";
                    else if (lastMsg.endsWith(".m4a") || lastMsg.endsWith(".3gp") || lastMsg.endsWith(".mp3"))
                        lastMsgType = "audio/*";
                }

                ConversationListItem clItem = ((ConversationListItem) viewHolder.itemView.findViewById(R.id.convoitemview));

                clItem.bind(viewHolder, chatId, providerId, accountId, address, nickname, type, lastMsg, lastMsgDate, lastMsgType, presence, null, true, false, chatFavorite, reference);

                clItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        context.startActivity(ConversationDetailActivity.getStartIntent(v.getContext(), chatId, nickname, reference , address));
                    }
                });

                // long click item to show bottom sheet
                clItem.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showBottomSheetDialog(address, chatId, chatFavorite, type, providerId, accountId);
                        return false;
                    }
                });
            } else {
                final long chatId = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.Messages.THREAD_ID));
                final String nickname = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
                final String address = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Messages.CONTACT));
                final String body = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Messages.BODY));
                final long messageDate = cursor.getLong(cursor.getColumnIndexOrThrow(Imps.Messages.DATE));
                final String messageType = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Messages.MIME_TYPE));
                final String reference = cursor.getString(ConversationListItem.COLUMN_AVATAR_HASH);
                if (address != null) {

                    if (viewHolder.itemView instanceof ConversationListItem) {
                        ((ConversationListItem) viewHolder.itemView).bind(viewHolder, chatId, -1, -1, address, nickname, -1, body, messageDate, messageType, -1, mSearchString, true, false, -1, reference);

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Context context = v.getContext();
                                context.startActivity(ConversationDetailActivity.getStartIntent(v.getContext(), chatId, nickname, reference));
                            }
                        });
                    }
                }
            }
        }

        private void showBottomSheetDialog(String address, long chatId, int chatFavorite, int type, long providerId, long accountId) {
            String account = address.split("@")[0].split("\\.")[0];
            mBottomSheet = CustomBottomSheetDialogFragment.getInstance(chatId, chatFavorite, account, type, providerId, accountId, address);
            mBottomSheet.show(((FragmentActivity) mContext).getSupportFragmentManager(), "Dialog");
        }

    }

    static String mSearchString = null;

    public void doSearch(String searchString) {
        mSearchString = searchString;

        if (mLoaderManager != null)
            mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);

    }

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private int mLastCount = 0;

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();

            CursorLoader loader = null;

            //search nickname, jabber id, or last message
            if (!TextUtils.isEmpty(mSearchString)) {

                mUri = Imps.Messages.CONTENT_URI_MESSAGES_BY_SEARCH;

                //     buf.append("contacts." + Imps.Contacts.NICKNAME);
                //    buf.append(" LIKE ");
                //    DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
                //     buf.append(" OR ");
                buf.append(Imps.Messages.BODY);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");

                buf.append(" AND ");
                buf.append(Imps.MessageColumns.STATUS);
                buf.append('=');
                buf.append(Imps.MessageColumns.VISIBLE);

                loader = new CursorLoader(getActivity(), mUri, null,
                        buf == null ? null : buf.toString(), null, Imps.Messages.REVERSE_SORT_ORDER);
            } else {
                mUri = Imps.Contacts.CONTENT_URI_CHAT_CONTACTS_BY;

                if (mFilterArchive)
                    buf.append(Imps.Chats.CHAT_TYPE + '=' + Imps.Chats.CHAT_TYPE_ARCHIVED);
                else
                    buf.append("(" + Imps.Chats.CHAT_TYPE + " IS NULL")
                            .append(" OR " + Imps.Chats.CHAT_TYPE + '=' + Imps.Chats.CHAT_TYPE_MUTED)
                            .append(" OR " + Imps.Chats.CHAT_TYPE + '=' + Imps.Chats.CHAT_TYPE_ACTIVE + ")");

                loader = new CursorLoader(getActivity(), mUri, CHAT_PROJECTION,
                        buf == null ? null : buf.toString(), null, Imps.Contacts.PIN_ORDER);
            }

            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {

            if (newCursor == null)
                return; // the app was quit or something while this was working
            newCursor.setNotificationUri(getActivity().getContentResolver(), mUri);

            mAdapter.changeCursor(newCursor);

            if (mRecView.getAdapter() == null)
                mRecView.setAdapter(mAdapter);


            if (mLastCount == 0 && mAdapter.getItemCount() > 0) {
                mRecView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                mEmptyViewImage.setVisibility(View.GONE);

            } else if (mAdapter.getItemCount() == 0) {
                mRecView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyViewImage.setVisibility(View.VISIBLE);

            }

            mLastCount = mAdapter.getItemCount();

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            mAdapter.swapCursor(null);

        }

        public final String[] CHAT_PROJECTION = {Imps.Contacts._ID, Imps.Contacts.PROVIDER,
                Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
                Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
                Imps.Contacts.SUBSCRIPTION_TYPE,
                Imps.Contacts.SUBSCRIPTION_STATUS,
                Imps.Presence.PRESENCE_STATUS,
                Imps.Presence.PRESENCE_CUSTOM_STATUS,
                Imps.Chats.LAST_MESSAGE_DATE,
                Imps.Chats.LAST_UNREAD_MESSAGE,
                Imps.Chats.CHAT_TYPE,
                Imps.Chats.CHAT_FAVORITE,
                Imps.Contacts.AVATAR_DATA
                //        Imps.Contacts.AVATAR_DATA

        };


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    private MigrateAccountTask.MigrateAccountListener mMigrateTaskListener;

    private synchronized void doUpgrade() {

//        if (mMigrateTaskListener == null) {
//
//            mMigrateTaskListener = new MigrateAccountTask.MigrateAccountListener() {
//                @Override
//                public void migrateComplete(OnboardingAccount account) {
//
//                    mUpgradeAction.setText(getString(R.string.upgrade_complete_action));
//                    mUpgradeAction.setBackgroundColor(getResources().getColor(R.color.message_background_light));
//                    mUpgradeAction.setTextColor(getResources().getColor(R.color.wrappy_primary));
//                    mUpgradeAction.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                        }
//                    });
//                }
//
//                @Override
//                public void migrateFailed(long providerId, long accountId) {
//
//                    mUpgradeDesc.setText(getString(R.string.upgrade_failed));
//                    mUpgradeAction.setText(getString(R.string.upgrade_complete_action));
//                    mUpgradeAction.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                        }
//                    });
//                }
//            };
//
//            mUpgradeAction.setText(getString(R.string.upgrade_progress_action));
//            mUpgradeAction.setBackgroundColor(getResources().getColor(R.color.message_background_dark));
//            mUpgradeAction.setTextColor(getResources().getColor(R.color.message_background_light));
//
//            mUpgradeAction.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //do nothing
//
//                }
//            });
//
//            mUpgradeImage.setImageResource(R.drawable.olo_thinking);
//
//
//            ((ImApp) getActivity().getApplication()).doUpgrade(getActivity(), Constant.EMAIL_DOMAIN, mMigrateTaskListener);
//
//        }
    }

}
