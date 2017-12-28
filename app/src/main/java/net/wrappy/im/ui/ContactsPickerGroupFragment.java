package net.wrappy.im.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yalantis.ucrop.UCrop;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.model.SelectedContact;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.ImpsProvider;
import net.wrappy.im.util.PopupUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ben on 22/11/2017.
 */

public class ContactsPickerGroupFragment extends Fragment implements View.OnClickListener {

    View mainView;
    private final int IMAGE_AVARTA = 101;
    @BindView(R.id.btnGroupPhoto)
    ImageButton btnGroupPhoto;

    @BindView(R.id.edGroupName)
    AppEditTextView edGroupName;

    @BindView(R.id.imgGroupPhoto)
    CircleImageView imgGroupPhoto;

    @BindView(R.id.lstContacts)
    ListView lstContacts;
    @BindView(R.id.txtGroupNumbers)
    TextView txtGroupNumbers;
    private ContactAdapter mAdapter;

    private Activity mActivity;
    private boolean mAwaitingUpdate;
    private MyLoaderCallbacks mLoaderCallbacks;
    private Handler mHandler = new Handler();
    Bitmap bmpThumbnail;
    public Cursor cursor;
    Uri resultUri;

    public static ContactsPickerGroupFragment newsIntance() {
        return new ContactsPickerGroupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.contacts_picker_group_fragment, null);
        ButterKnife.bind(this, mainView);

        mActivity = getActivity();
        btnGroupPhoto.setOnClickListener(this);

        doFilter();

        return mainView;
    }


    public synchronized void doFilter() {
        if (mAdapter == null) {
            mAdapter = new ContactAdapter(mActivity, R.layout.contact_view);
            lstContacts.setAdapter(mAdapter);

            mLoaderCallbacks = new MyLoaderCallbacks();
            getLoaderManager().initLoader(2, null, mLoaderCallbacks);
        } else {

            if (!mAwaitingUpdate) {
                mAwaitingUpdate = true;
                mHandler.postDelayed(new Runnable() {

                    public void run() {
                        getLoaderManager().restartLoader(2, null, mLoaderCallbacks);
                        mAwaitingUpdate = false;
                    }
                }, 1000);
            }

        }
    }

    @Override
    public void onClick(View view) {
        ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
        BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_choose_camera, getString(R.string.popup_take_photo));
        sheetCells.add(sheetCell);
        sheetCell = new BottomSheetCell(2, R.drawable.ic_choose_gallery, getString(R.string.popup_choose_gallery));
        sheetCells.add(sheetCell);
        PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
            @Override
            public void onSelectBottomSheetCell(int index) {
                switch (index) {
                    case 1:
                        AppFuncs.openCamera(getActivity(), IMAGE_AVARTA);
                        break;
                    case 2:
                        AppFuncs.openGallery(getActivity(), IMAGE_AVARTA);
                        break;
                    default:
                }
            }
        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data != null) {
                if (requestCode == IMAGE_AVARTA) {

                    if (data.getData() != null) {
                        AppFuncs.cropImage(getActivity(), data.getData(), true);

                    }
                } else if (requestCode == UCrop.REQUEST_CROP) {
                    resultUri = UCrop.getOutput(data);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    imgGroupPhoto.setLayoutParams(layoutParams);
                    imgGroupPhoto.requestLayout();
                    GlideHelper.loadBitmap(getActivity(), imgGroupPhoto, resultUri.toString(), true);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getGroupName() {
        return edGroupName.getText().toString().trim();
    }

    public ArrayList<String> getListUsername() {
        ArrayList<String> arrListMember = new ArrayList<>();
        try {
            if (cursor != null && cursor.moveToFirst()) { //make sure you got results, and move to first row
                do {
                    String mName = cursor.getString(ContactListItem.COLUMN_CONTACT_NICKNAME); //column 1 for the current row
                    arrListMember.add(mName);
                } while (cursor.moveToNext()); //move to next row in the query result
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return arrListMember;
    }

    public Uri getGroupUri() {
        return resultUri;
    }


    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<android.database.Cursor> {
//        @Override
//        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//            StringBuilder buf = new StringBuilder();
//
//
//            buf.append('(');
//            buf.append(BaseColumns._ID).append(" in ").append('(');
//            LongSparseArray<ContactsPickerActivity.SelectedContact> contacts = ((ContactsPickerActivity) getActivity()).getSelection();
//            for (int i = 0; i < contacts.size(); i++) {
//                Long key = contacts.keyAt(i);
//                ContactsPickerActivity.SelectedContact contact = contacts.get(key);
//                buf.append(contact.id);
//                if (i < contacts.size() - 1)
//                    buf.append(',');
//            }
//            buf.append(')');
//
//            buf.append(')');
//
//            CursorLoader loader = new CursorLoader(getActivity(), Imps.Contacts.CONTENT_URI, ContactListItem.CONTACT_PROJECTION,
//                    buf == null ? null : buf.toString(), null, Imps.Contacts.MODE_AND_ALPHA_SORT_ORDER);
//            return loader;
//        }

        @Override
        public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();


            buf.append('(');
            buf.append(ImpsProvider.TABLE_CONTACTS).append('.').append(BaseColumns._ID).append(" in ").append('(');
            LongSparseArray<SelectedContact> contacts = ((ContactsPickerActivity) getActivity()).getSelection();
            for (int i = 0; i < contacts.size(); i++) {
                Long key = contacts.keyAt(i);
                SelectedContact contact = contacts.get(key);
                buf.append(contact.id);
                if (i < contacts.size() - 1)
                    buf.append(',');
            }
            buf.append(')');

            buf.append(')');

            CursorLoader loader = new CursorLoader(getActivity(), Imps.Contacts.CONTENT_URI, ContactListItem.CONTACT_PROJECTION,
                    buf == null ? null : buf.toString(), null, Imps.Contacts.MODE_AND_ALPHA_SORT_ORDER);
            return loader;
        }

        @Override
        public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor newCursor) {
            cursor = newCursor;
            mAdapter.swapCursor(newCursor);

            if (mActivity != null && isAdded())
                txtGroupNumbers.setText(getString(R.string.num_member_group_chat, String.valueOf(mAdapter.getCount())));
        }

        @Override
        public void onLoaderReset(android.content.Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

    }

    public class ContactAdapter extends ResourceCursorAdapter {

        public ContactAdapter(Context context, int view) {
            super(context, view, null, 0);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);//let the adapter handle setting up the row views
            if (mActivity != null && isAdded())
                v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ContactListItem v = (ContactListItem) view;

            ContactViewHolder holder = v.getViewHolder();
            if (holder == null) {
                holder = new ContactViewHolder(v);
                v.setViewHolder(holder);
            }

            v.bind(holder, cursor, "", false);
            holder.mAvatarCheck.setVisibility(View.VISIBLE);
            holder.mLine1.setTextColor((holder.mLine1.getCurrentTextColor() & 0x00ffffff) | 0x80000000);
        }
    }
}
