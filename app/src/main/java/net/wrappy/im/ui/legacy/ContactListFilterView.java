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

package net.wrappy.im.ui.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.ui.ContactListItem;
import net.wrappy.im.ui.ContactViewHolder;
import net.wrappy.im.ui.ContactsPickerActivity;
import net.wrappy.im.util.LogCleaner;
import net.wrappy.im.util.PopupUtils;

public class ContactListFilterView extends LinearLayout {
    private AbsListView mFilterList;
    private ContactAdapter mContactAdapter;

    private String mSearchString;

    private TextView mEmptyView = null;

    private Uri mUri;
    private final Context mContext;
    private final SimpleAlertHandler mHandler;

    private MyLoaderCallbacks mLoaderCallbacks;

    private ContactsPickerActivity.ContactListListener mListener = null;
    private LoaderManager mLoaderManager;
    private int mLoaderId;

    public ContactListFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mHandler = new SimpleAlertHandler((Activity) context);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

//        mFilterList = (AbsListView) findViewById(R.id.filteredList);
        mFilterList.setTextFilterEnabled(true);

        //      mEmptyView = (TextView) findViewById(R.id.empty);

        mFilterList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = (Cursor) mFilterList.getItemAtPosition(position);

                if (mListener != null)
                    mListener.openChat(c);

            }
        });

        mFilterList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {

                String[] contactOptions = {
                        //        mContext.getString(R.string.menu_verify),
                        mContext.getString(R.string.menu_contact_nickname),
                        mContext.getString(R.string.menu_remove_contact)};
                //      mContext.getString(R.string.menu_block_contact)};
                PopupUtils.getSelectionDialog(mContext, "", contactOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (which == 0)
                            setContactNickname(position);
                        else if (which == 1)
                            removeContactAtPosition(position);
                        else if (which == 2)
                            blockContactAtPosition(position);
                    }
                });
                return true;
            }

        });

        /**
         mEtSearch = (EditText)findViewById(R.id.contactSearch);

         mEtSearch.addTextChangedListener(new TextWatcher()
         {

         @Override public void afterTextChanged(Editable s) {

         }

         @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

         }

         @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

         ContactListFilterView.this.doFilter(mEtSearch.getText().toString());

         }

         });


         mEtSearch.setOnKeyListener(new OnKeyListener ()
         {

         @Override public boolean onKey(View v, int keyCode, KeyEvent event) {

         ContactListFilterView.this.doFilter(mEtSearch.getText().toString());
         return false;
         }

         });
         */

        /*
        mFilterList.setItemActionListener(new ListView.OnActionClickListener() {

            @Override
            public void onClick(View listView, View buttonview, int position) {

                Cursor c = (Cursor) mFilterList.getItemAtPosition(position);
                if (mListener != null)
                    if (buttonview.getId() == R.id.btnExListChat)
                        mListener.startChat(c);
                    else if (buttonview.getId() == R.id.btnExListProfile)
                        mListener.showProfile(c);

            }
    }, R.id.btnExListChat, R.id.btnExListProfile);
    */

        //

        //if (!isInEditMode())
        //  mPresenceView = (UserPresenceView) findViewById(R.id.userPresence);

    }

    public AbsListView getListView() {
        return mFilterList;
    }

    public Cursor getContactAtPosition(int position) {
        return (Cursor) mContactAdapter.getItem(position);
    }


    public void doFilter(Uri uri, String filterString) {
        if (uri != null && !uri.equals(mUri)) {
            mUri = uri;
        }
        doFilter(filterString);
    }

    boolean mAwaitingUpdate = false;

    public synchronized void doFilter(String filterString) {

        mSearchString = filterString;

        if (mContactAdapter == null) {

            mContactAdapter = new ContactAdapter(mContext, R.layout.contact_view);

            ((ListView) mFilterList).setAdapter(mContactAdapter);

            mLoaderCallbacks = new MyLoaderCallbacks();
            mLoaderManager.initLoader(mLoaderId, null, mLoaderCallbacks);
        } else {

            if (!mAwaitingUpdate) {
                mAwaitingUpdate = true;
                mHandler.postDelayed(new Runnable() {

                    public void run() {

                        mLoaderManager.restartLoader(mLoaderId, null, mLoaderCallbacks);
                        mAwaitingUpdate = false;
                    }
                }, 1000);
            }

        }
    }

    private class ContactAdapter extends ResourceCursorAdapter {

        public ContactAdapter(Context context, int view) {
            super(context, view, null, 0);

        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            ContactListItem view = (ContactListItem) super.newView(context, cursor, parent);

            ContactViewHolder holder = view.getViewHolder();

            if (holder == null) {
                holder = new ContactViewHolder(view);

                // holder.mMediaThumb = (ImageView)findViewById(R.id.media_thumbnail);
                view.setViewHolder(holder);
            }


            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ContactListItem v = (ContactListItem) view;
            v.bind(v.getViewHolder(), cursor, mSearchString, true);
        }

    }


    private void setContactNickname(int aPosition) {
        Cursor cursor = (Cursor) mFilterList.getItemAtPosition(aPosition);
        final IImConnection conn = getConnection(cursor);

        final String address = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
        final String nickname = cursor.getString(cursor.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
        PopupUtils.showCustomEditDialog(mContext, mContext.getString(R.string.menu_contact_nickname), address, nickname, R.string.yes, R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newNickname = String.valueOf(v.getTag());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setContactNickname(address, newNickname, conn);
                    }
                }, 500);
            }
        }, null, false);
    }

    protected void setContactNickname(String aAddress, String aNickname, IImConnection conn) {
        try {

            IContactListManager listManager = conn.getContactListManager();
            int result = listManager.setContactName(aAddress, aNickname);
            if (result != ImErrorInfo.NO_ERROR) {
                AppFuncs.alert(mContext, mContext.getString(R.string.error_prefix) + result, true); // TODO -LS error handling
            }
        } catch (Exception e) {
            AppFuncs.alert(mContext, mContext.getString(R.string.error_prefix) + e.getMessage(), true); // TODO -LS error handling
        }
        mFilterList.invalidate();
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }


    public void removeContactAtPosition(int packedPosition) {
        removeContact((Cursor) mFilterList.getItemAtPosition(packedPosition));
    }

    void removeContact(Cursor c) {
        final IImConnection conn = getConnection(c);

        String nickname = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
        final String address = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
        View.OnClickListener confirmListener = new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    IContactListManager manager = conn.getContactListManager();
                    int res = manager.removeContact(address);
                    if (res != ImErrorInfo.NO_ERROR) {
                        mHandler.showAlert(R.string.attention,
                                ErrorResUtils.getErrorRes(getResources(), res, address));
                    }
                } catch (RemoteException e) {

                    mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                    LogCleaner.error(ImApp.LOG_TAG, "remote error", e);
                }
            }
        };

        Resources r = getResources();
        PopupUtils.showCustomDialog(mContext, r.getString(R.string.confirm), r.getString(R.string.confirm_delete_contact, nickname), R.string.yes, R.string.no, confirmListener, null, false);
    }

    private IImConnection getConnection(Cursor c) {
        return ImApp.sImApp.getConnection(c.getLong(ContactListItem.COLUMN_CONTACT_PROVIDER), c.getLong(ContactListItem.COLUMN_CONTACT_ACCOUNT));

    }

    public void blockContactAtPosition(int packedPosition) {
        blockContact((Cursor) mFilterList.getItemAtPosition(packedPosition));
    }

    void blockContact(Cursor c) {

        final IImConnection conn = getConnection(c);

        String nickname = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.NICKNAME));
        final String address = c.getString(c.getColumnIndexOrThrow(Imps.Contacts.USERNAME));
        View.OnClickListener confirmListener = new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    IContactListManager manager = conn.getContactListManager();

                    int res = -1;

                    if (!manager.isBlocked(address)) {
                        res = manager.blockContact(address);

                        if (res != ImErrorInfo.NO_ERROR) {
                            mHandler.showAlert(R.string.attention,
                                    ErrorResUtils.getErrorRes(getResources(), res, address));
                        }
                    }
                } catch (RemoteException e) {

                    mHandler.showServiceErrorAlert(e.getLocalizedMessage());
                    LogCleaner.error(ImApp.LOG_TAG, "remote error", e);
                }
            }
        };

        Resources r = getResources();
        PopupUtils.showCustomDialog(mContext, r.getString(R.string.confirm), r.getString(R.string.confirm_block_contact, nickname), R.string.yes, R.string.no, confirmListener, null, false);
    }


    public void setLoaderManager(LoaderManager loaderManager, int loaderId) {
        mLoaderManager = loaderManager;
        mLoaderId = loaderId;
    }

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();

            if (mSearchString != null) {

                buf.append(Imps.Contacts.NICKNAME);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
                buf.append(" OR ");
                buf.append(Imps.Contacts.USERNAME);
                buf.append(" LIKE ");
                DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
            }

            CursorLoader loader = new CursorLoader(getContext(), mUri, ContactListItem.CONTACT_PROJECTION,
                    buf == null ? null : buf.toString(), null, Imps.Contacts.DEFAULT_SORT_ORDER);

            //     loader.setUpdateThrottle(10L);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            if (newCursor == null)
                return; // the app was quit or something while this was working
            newCursor.setNotificationUri(getContext().getContentResolver(), mUri);

            mContactAdapter.changeCursor(newCursor);

            if (newCursor != null && newCursor.getCount() == 0) {
                if (mUri.getPath().contains("/contacts/chatting"))
                    mEmptyView.setText(R.string.empty_conversation_group);
                else
                    mEmptyView.setText(R.string.empty_contact_list);
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            mContactAdapter.swapCursor(null);

        }

    }


}
