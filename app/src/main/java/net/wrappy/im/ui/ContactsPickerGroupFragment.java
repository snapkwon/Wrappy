package net.wrappy.im.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.util.SecureMediaStore;

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

    @BindView(R.id.lstContacts)
    ListView lstContacts;
    @BindView(R.id.txtGroupNumbers)
    TextView txtGroupNumbers;
    private ContactAdapter mAdapter;

    public static ContactsPickerGroupFragment newsIntance() {
        return new ContactsPickerGroupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.contacts_picker_group_fragment, null);
        ButterKnife.bind(this, mainView);

        btnGroupPhoto.setOnClickListener(this);

        mAdapter = new ContactAdapter(getActivity(), R.layout.contact_view);
        lstContacts.setAdapter(mAdapter);
        MyLoaderCallbacks mLoaderCallbacks = new MyLoaderCallbacks();
        ((ContactsPickerActivity) getActivity()).getSupportLoaderManager().initLoader(ContactsPickerActivity.LOADER_ID, null, mLoaderCallbacks);

        return mainView;
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            AppFuncs.getImageFromDevice(getActivity(), IMAGE_AVARTA);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 199);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data != null) {
                if (requestCode == IMAGE_AVARTA) {
                    Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(getActivity(), data.getData(), 512);
                    btnGroupPhoto.setImageBitmap(bmpThumbnail);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    class MyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            StringBuilder buf = new StringBuilder();


            buf.append('(');
            buf.append(BaseColumns._ID).append(" in ").append('(');
            LongSparseArray<ContactsPickerActivity.SelectedContact> contacts = ((ContactsPickerActivity) getActivity()).getSelection();
            for (int i = 0; i < contacts.size(); i++) {
                Long key = contacts.keyAt(i);
                ContactsPickerActivity.SelectedContact contact = contacts.get(key);
                buf.append(contact.id);
                if (i < contacts.size() - 1)
                    buf.append(',');
            }
            buf.append(')');

            buf.append(')');

            CursorLoader loader = new CursorLoader(getActivity(), Imps.Contacts.CONTENT_URI, ContactListItem.CONTACT_PROJECTION,
                    buf == null ? null : buf.toString(), null, Imps.Contacts.MODE_AND_ALPHA_SORT_ORDER);
            //    loader.setUpdateThrottle(50L);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
            mAdapter.swapCursor(newCursor);

            txtGroupNumbers.setText(getString(R.string.num_member_group_chat, String.valueOf(mAdapter.getCount())));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
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
            v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ContactListItem v = (ContactListItem) view;

            ContactViewHolder holder = v.getViewHolder();
            if (holder == null) {
                holder = new ContactViewHolder(v);

                // holder.mMediaThumb = (ImageView)findViewById(R.id.media_thumbnail);
                v.setViewHolder(holder);
            }


            v.bind(holder, cursor, "", false);
            int index = cursor.getPosition();
            long itemId = getItemId(index);
            holder.mAvatarCheck.setVisibility(View.VISIBLE);
            String userName = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
            holder.mLine1.setTextColor((holder.mLine1.getCurrentTextColor() & 0x00ffffff) | 0x80000000);
        }
    }
}
