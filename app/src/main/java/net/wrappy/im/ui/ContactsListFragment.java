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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.MainActivity;
import net.wrappy.im.model.Address;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.provider.Imps;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.util.BundleKeyConstant;

//import com.bumptech.glide.Glide;

public class ContactsListFragment extends Fragment {

    private ContactListRecyclerViewAdapter mAdapter = null;
    private Uri mUri;
    private MyLoaderCallbacks mLoaderCallbacks;
    private LoaderManager mLoaderManager;
    private int mLoaderId = 1001;
    private static RecyclerView mRecView;
    private View mEmptyView;
    String mSearchString = null;

    private int mType = Imps.Contacts.TYPE_NORMAL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.awesome_fragment_contacts_list, container, false);

        mRecView = (RecyclerView)view.findViewById(R.id.recyclerview);

        mEmptyView = view.findViewById(R.id.empty_view);

        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).inviteContact();
            }
        });

        setupRecyclerView(mRecView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    public int getCurrentType ()
    {
        return mType;
    }

    public void setArchiveFilter (boolean filterAchive)
    {
       if (filterAchive)
           setType(Imps.Contacts.TYPE_HIDDEN);
        else
           setType(Imps.Contacts.TYPE_NORMAL);
    }

    public void setType (int type)
    {
        mType = type;

        if (mLoaderManager != null)
            mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
    }

    public void doSearch (String searchString)
    {
        mSearchString = searchString;

        if (mLoaderManager != null)
            mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);

    }

    public int getContactCount ()
    {
        if (mAdapter != null)
            return mAdapter.getItemCount();
        else
            return 1;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

        Uri baseUri = Imps.Contacts.CONTENT_URI;
        Uri.Builder builder = baseUri.buildUpon();
        mUri = builder.build();

        mLoaderManager = getLoaderManager();
        mLoaderCallbacks = new MyLoaderCallbacks();
        mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);

        Cursor cursor = null;
        mAdapter = new ContactListRecyclerViewAdapter(getActivity(),cursor);

        if (mAdapter.getItemCount() == 0) {
            mRecView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);

        }
        else {
            mRecView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

        }

        // init swipe to dismiss logic

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) {

            public static final float ALPHA_FULL = 1.0f;

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                // We only want the active item to change
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof ContactViewHolder) {
                        // Let the view holder know that this item is being moved or dragged
                        ContactViewHolder itemViewHolder = (ContactViewHolder) viewHolder;
                        //itemViewHolder.onItemSelected();
                    }
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    int contactType = Imps.Contacts.TYPE_NORMAL;

                    if (viewHolder instanceof ContactViewHolder) {
                        // Let the view holder know that this item is being moved or dragged
                        ContactViewHolder itemViewHolder = (ContactViewHolder) viewHolder;
                        contactType = itemViewHolder.mType;
                    }

                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
            /* Note, ApplicationManager is a helper class I created
               myself to get a context outside an Activity class -
               feel free to use your own method */

                        if (contactType == Imps.Contacts.TYPE_HIDDEN)
                        {
                            icon = BitmapFactory.decodeResource(
                                    getActivity().getResources(), R.drawable.ic_unarchive_white_24dp);

                    /* Set your color for positive displacement */
                            //p.setARGB(255, 150, 255, 150);
                            p.setColor(getResources().getColor(R.color.holo_green_dark));


                        }
                        else
                        {
                            icon = BitmapFactory.decodeResource(
                                    getActivity().getResources(), R.drawable.ic_archive_white_24dp);

                /* Set your color for positive displacement */
                            p.setARGB(255, 150, 150, 150);

                        }

                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);

                        // Set the image icon for Right swipe
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + convertDpToPx(16),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight())/2,
                                p);
                    }


                    // Fade out the view as it is swiped out of the parent's bounds
                    final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);

                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            private int convertDpToPx(int dp){
                return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                viewHolder.itemView.setAlpha(ALPHA_FULL);

                if (viewHolder instanceof ContactViewHolder) {
                    // Tell the view holder it's time to restore the idle state
                    ContactViewHolder itemViewHolder = (ContactViewHolder) viewHolder;
                    //itemViewHolder.onItemClear();
                }
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // callback for drag-n-drop, false to skip this feature
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {


                // callback for swipe to dismiss, removing item from data and adapter
                int position = viewHolder.getAdapterPosition();

                //delete / endchat
                //items.remove(viewHolder.getAdapterPosition());
                long itemId = mAdapter.getItemId(position);

                if (viewHolder instanceof ContactViewHolder) {
                    // Tell the view holder it's time to restore the idle state
                    final ContactViewHolder itemViewHolder = (ContactViewHolder) viewHolder;
                    //itemViewHolder.onItemClear();

                    final boolean doArchive = (itemViewHolder.mType == Imps.Contacts.TYPE_NORMAL ? true : false);

                    //then hide/archive contact
                    archiveContact(((MainActivity) getActivity()), itemViewHolder.itemView.getId(), itemViewHolder.mAddress, itemViewHolder.mProviderId, itemViewHolder.mAccountId, doArchive);

                    String action =  getString(R.string.action_archived);
                    if (!doArchive)
                        action = getString(R.string.action_active);

                    Snackbar snack = Snackbar.make(mRecView,action, Snackbar.LENGTH_LONG);
                    snack.setAction(R.string.action_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            archiveContact(((MainActivity) getActivity()), itemViewHolder.itemView.getId(), itemViewHolder.mAddress, itemViewHolder.mProviderId, itemViewHolder.mAccountId, !doArchive);

                        }
                    });

                    snack.show();

                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerView);




    }

    private static void archiveContact (Activity activity, long itemId, String address, long providerId, long accountId, boolean isArchived)
    {

        try {

            IImConnection mConn;
            ImApp app = ((ImApp)activity.getApplication());
            mConn = app.getConnection(providerId, accountId);

            //first leave, delete an existing chat session
            IChatSessionManager sessionMgr = mConn.getChatSessionManager();
            if (sessionMgr != null) {
                IChatSession session = sessionMgr.getChatSession(Address.stripResource(address));

            }

            //then delete the contact from our list
            IContactListManager manager = mConn.getContactListManager();

            int res = manager.hideContact(address,isArchived);
            if (res != ImErrorInfo.NO_ERROR) {
                //mHandler.showAlert(R.string.error,
                //      ErrorResUtils.getErrorRes(getResources(), res, address));
            }


        }
        catch (RemoteException re)
        {

        }


    }

    private static void deleteContact (Activity activity, long itemId, String address, long providerId, long accountId)
    {

        try {

            IImConnection mConn;
            ImApp app = ((ImApp)activity.getApplication());
            mConn = app.getConnection(providerId, accountId);

            //first leave, delete an existing chat session
            IChatSessionManager sessionMgr = mConn.getChatSessionManager();
            if (sessionMgr != null) {
                IChatSession session = sessionMgr.getChatSession(Address.stripResource(address));

            }

            //then delete the contact from our list
            IContactListManager manager = mConn.getContactListManager();

            int res = manager.removeContact(address);
            if (res != ImErrorInfo.NO_ERROR) {
                //mHandler.showAlert(R.string.error,
                  //      ErrorResUtils.getErrorRes(getResources(), res, address));
            }


        }
        catch (RemoteException re)
        {

        }


    }

    public static class ContactListRecyclerViewAdapter
            extends CursorRecyclerViewAdapter<ContactViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private Context mContext;

        public ContactListRecyclerViewAdapter(Context context, Cursor cursor) {
            super(context,cursor);
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mContext = context;


        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            ContactListItem view = (ContactListItem)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contact_view, parent, false);
            view.setBackgroundResource(mBackground);

            ContactViewHolder holder = view.getViewHolder();
            if (holder == null) {
                holder = new ContactViewHolder(view);
                view.applyStyleColors(holder);

                // holder.mMediaThumb = (ImageView)findViewById(R.id.media_thumbnail);
                view.setViewHolder(holder);
            }

            return holder;

        }

        @Override
        public void onBindViewHolder(final ContactViewHolder viewHolder, Cursor cursor,int position) {

            viewHolder.mContactId =  cursor.getInt(ContactListItem.COLUMN_CONTACT_ID);
           viewHolder.mAddress =  cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
            viewHolder.mType =  cursor.getInt(ContactListItem.COLUMN_CONTACT_TYPE);

            String nickname =  cursor.getString(ContactListItem.COLUMN_CONTACT_NICKNAME);
            viewHolder.email =  cursor.getString(ContactListItem.COLUMN_CONTACT_EMAIL);
            final String reference = cursor.getString(ContactListItem.COLUMN_AVATAR_HASH);

            if (TextUtils.isEmpty(nickname))
            {
                nickname = viewHolder.mAddress.split("@")[0].split("\\.")[0];
            }
            else
            {
                viewHolder.mProviderId = cursor.getLong(ContactListItem.COLUMN_CONTACT_PROVIDER);
                viewHolder.mAccountId = cursor.getLong(ContactListItem.COLUMN_CONTACT_ACCOUNT);
                nickname = nickname.split("@")[0].split("\\.")[0];
             }

            viewHolder.mNickname = nickname;

            if (viewHolder.itemView instanceof ContactListItem) {
                ((ContactListItem)viewHolder.itemView).bind(viewHolder, cursor, "", false, false);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /*
                    if (mContext instanceof ContactListActivity)
                        ((ContactListActivity)mContext).startChat(viewHolder.mProviderId, viewHolder.mAccountId, viewHolder.mAddress);
                    else if (mContext instanceof MainActivity)
                        ((MainActivity)mContext).startChat(viewHolder.mProviderId,viewHolder.mAccountId, viewHolder.mAddress);
                        */

                    Intent intent = new Intent(mContext,ProfileActivity.class);
                    intent.putExtra(BundleKeyConstant.ADDRESS_KEY, viewHolder.mAddress);
                    intent.putExtra(BundleKeyConstant.NICK_NAME_KEY, viewHolder.mNickname);
                    intent.putExtra(BundleKeyConstant.PROVIDER_KEY, viewHolder.mProviderId);
                    intent.putExtra(BundleKeyConstant.ACCOUNT_KEY, viewHolder.mAccountId);
                    intent.putExtra(BundleKeyConstant.CONTACT_ID_KEY, viewHolder.mContactId);
                    intent.putExtra(BundleKeyConstant.REFERENCE_KEY,reference);
                    mContext.startActivity(intent);

                }
            });

            /**
            viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener()
            {

                @Override
                public boolean onLongClick(View view) {

                    if (mContext instanceof MainActivity) {

                        String message = mContext.getString(R.string.confirm_delete_contact,viewHolder.mNickname);

                        Snackbar.make(mRecView, message, Snackbar.LENGTH_LONG)
                                .setAction(mContext.getString(R.string.yes), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //if they click, then cancel timer that will be used to end the chat

                                        deleteContact(((MainActivity) mContext), viewHolder.mView.getId(), viewHolder.mAddress, viewHolder.mProviderId, viewHolder.mAccountId);
                                    }
                                }).show();

                        return true;
                    }




                    return false;
                }
            });**/

        }

    }

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();

            if (mSearchString != null) {
                buf.append('(');
                buf.append(Imps.Contacts.NICKNAME);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
                buf.append(" OR ");
                buf.append(Imps.Contacts.USERNAME);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
                buf.append(')');
               // buf.append(" AND ");
            }
            else {
                buf.append(Imps.Contacts.TYPE).append('=').append(mType);
            }
            
            CursorLoader loader = new CursorLoader(getActivity(), mUri, CHAT_PROJECTION,
                    buf == null ? null : buf.toString(), null, null);

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

            if (mAdapter.getItemCount() == 0) {
                mRecView.setVisibility(View.GONE);
//                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.VISIBLE);

            }
            else {
                mRecView.setVisibility(View.VISIBLE);
  //              mEmptyView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.GONE);

            };


        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            mAdapter.swapCursor(null);

        }

        public final String[] CHAT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER,
                Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
                Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
                Imps.Contacts.SUBSCRIPTION_TYPE,
                Imps.Contacts.SUBSCRIPTION_STATUS,
                Imps.Presence.PRESENCE_STATUS,
                Imps.Presence.PRESENCE_CUSTOM_STATUS,
                Imps.Chats.LAST_MESSAGE_DATE,
                Imps.Chats.LAST_UNREAD_MESSAGE,
                Imps.Contacts.AVATAR_HASH,
                Imps.Contacts.AVATAR_DATA,
                Imps.Contacts.CONTACT_EMAIL

        };


    }
}
