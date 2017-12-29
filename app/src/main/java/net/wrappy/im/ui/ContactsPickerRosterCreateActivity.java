package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v4.widget.ListViewCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.ContactsLoaderCallbacks;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppEditTextView;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.SelectedContact;
import net.wrappy.im.model.WpkRoster;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.widgets.FlowLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ben on 11/12/2017.
 */

public class ContactsPickerRosterCreateActivity extends BaseActivity {

    @BindView(R.id.flSelectedContacts) FlowLayout mSelectedContacts;
    @BindView(R.id.headerbarDone) ImageButton headerbarDone;
    @BindView(R.id.headerbarTitleLeft) AppTextView headerbarTitle;
    @BindView(R.id.contactsList) ListView mListView = null;
    @BindView(R.id.imgPhotoAvatar) CircleImageView imgPhotoAvatar;
    @BindView(R.id.spnRosterType) AppCompatSpinner spnRosterType;
    @BindView(R.id.txtRosterName) AppEditTextView txtRosterName;
    private ImApp mApp;
    private ContactAdapter mAdapter;
    String mSearchString;
    boolean isFlag;
    private LongSparseArray<SelectedContact> mSelection = new LongSparseArray<>();
    ContactsLoaderCallbacks mLoaderCallbacks;
    Bitmap photo;
    ArrayAdapter<String> arrAdapterType;
    AppFuncs appFuncs;
    ArrayList<String> arrType = new ArrayList<>();
    String rostername;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity,ContactsPickerRosterCreateActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.contacts_picker_roster_create_activity);
        super.onCreate(savedInstanceState);
        mApp = (ImApp) getApplication();
        appFuncs = AppFuncs.getInstance();
        headerbarDone.setVisibility(View.VISIBLE);
        headerbarTitle.setText(R.string.new_list);
        mSelectedContacts.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                final int diff = bottom - oldBottom;
                ListViewCompat.scrollListBy(mListView, diff);
            }
        });
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                if (isSelected(id)) {
                    unselect(id);
                } else {
                    select(position);
                }
            }

        });
        arrAdapterType = new ArrayAdapter<String>(getApplicationContext(),R.layout.update_profile_textview,arrType);
        spnRosterType.setAdapter(arrAdapterType);
        getTypeRoster();
        doFilterAsync("");
    }

    void getTypeRoster() {
        RestAPI.apiGET(getApplicationContext(),RestAPI.GET_TYPE_ROSTER).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                if (result!=null) {
                    try {
                        if (RestAPI.checkHttpCode(result.getHeaders().code())) {
                            String s = result.getResult();
                            JsonArray jsonArray = AppFuncs.convertToJson(s).getAsJsonArray();
                            for (JsonElement element: jsonArray) {
                                arrType.add(element.getAsString());
                            }
                            arrAdapterType.notifyDataSetChanged();
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    public void doFilterAsync(final String query) {

        doFilter(query);
    }

    public synchronized void doFilter(String filterString) {

        mSearchString = filterString;

        if (mAdapter == null) {

            mAdapter = new ContactAdapter(getApplicationContext(), R.layout.contact_view);
            mListView.setAdapter(mAdapter);

            mLoaderCallbacks = new ContactsLoaderCallbacks(getApplicationContext(), mSearchString, false, new ContactsLoaderCallbacks.ContactsLoaderDelegate() {
                @Override
                public void onCompleteContactsLoader(Cursor newCursor) {
                    if (newCursor!=null) {
                        mAdapter.swapCursor(newCursor);
                    }
                }
            });
            getSupportLoaderManager().initLoader(1, null, mLoaderCallbacks);
        }
    }

    private void select(int index) {
        long id = mAdapter.getItemId(index);
        if (!isSelected(id)) {
            Cursor cursor = (Cursor) mAdapter.getItem(index);
            String userName = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);

            SelectedContact contact = new SelectedContact(id,
                    userName,
                    (int) cursor.getLong(ContactListItem.COLUMN_CONTACT_ACCOUNT),
                    (int) cursor.getLong(ContactListItem.COLUMN_CONTACT_PROVIDER));
            mSelection.put(id, contact);
            createTagView(index, contact);
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean isSelected(long id) {
        return mSelection.indexOfKey(id) >= 0;
    }

    private void unselect(long id) {
        if (isSelected(id)) {
            removeTagView(mSelection.get(id));
            mSelection.remove((Long) id);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void createTagView(int index, SelectedContact contact) {
        Cursor cursor = (Cursor) mAdapter.getItem(index);
        long itemId = mAdapter.getItemId(index);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.picked_contact_item, mSelectedContacts, false);
        view.setTag(contact);

        // TODO - Feel a little awkward to create a ContactListItem here just to use the binding code.
        // I guess we should move that somewhere else.
        ContactListItem cli = new ContactListItem(this, null);
        ContactViewHolder cvh = new ContactViewHolder(view);
        cli.bind(cvh, cursor, null, false);
        View btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            private long itemId;
            private View view;

            public View.OnClickListener init(long itemId, View view) {
                this.itemId = itemId;
                this.view = view;
                return this;
            }

            @Override
            public void onClick(View v) {
                unselect(this.itemId);
            }
        }.init(itemId, view));
        mSelectedContacts.addView(view);
    }

    private void removeTagView(SelectedContact contact) {
        View view = mSelectedContacts.findViewWithTag(contact);
        if (view != null) {
            mSelectedContacts.removeView(view);
        }
    }

    public class ContactAdapter extends ResourceCursorAdapter {

        private Context mContext;

        public ContactAdapter(Context context, int view) {
            super(context, view, null, 0);
            mContext = context;
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


            v.bind(holder, cursor, mSearchString, false);
            int index = cursor.getPosition();
            long itemId = getItemId(index);
            holder.mAvatarCheck.setVisibility(isSelected(itemId) ? View.VISIBLE : View.GONE);
            String userName = cursor.getString(ContactListItem.COLUMN_CONTACT_USERNAME);
            holder.mLine1.setTextColor(holder.mLine1.getCurrentTextColor() | 0xff000000);
        }
    }

    @OnClick({R.id.headerbarBack, R.id.headerbarDone, R.id.btnPhotoCameraAvatar})
    public void onClick(View v) {
        if (isFlag) {
            return;
        } isFlag = true;
        try {
            switch (v.getId()) {
                case R.id.headerbarBack:
                    finish();
                    break;
                case R.id.headerbarDone:
                    postDataToServer("adas");
                    break;
                case R.id.btnPhotoCameraAvatar:
                    AppFuncs.getImageFromDevice(this,100);
                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        } finally {
            isFlag = false;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null && requestCode==100){
            photo = AppFuncs.getBitmapFromIntentResult(getApplicationContext(),data);
            imgPhotoAvatar.setImageBitmap(photo);

        }
    }

    private void postDataToServer(String referenceImage) {
        ArrayList<String> listUsernames = new ArrayList<>();
        for (int i=0; i < mSelection.size(); i++) {
            if (mSelection.valueAt(i)!=null) {
                SelectedContact selectedContact = mSelection.valueAt(i);
                listUsernames.add(selectedContact.getUsername());
            }
        }
        String type = arrAdapterType.getItem(spnRosterType.getSelectedItemPosition());

        WpkRoster wpkRoster = new WpkRoster();
        wpkRoster.setId(2);
        wpkRoster.setName(rostername);
        wpkRoster.setReference(referenceImage);
        wpkRoster.setType(type);
        wpkRoster.setListUsername(listUsernames);


        Imps.Roster.insert(getContentResolver(),wpkRoster);

        ArrayList<WpkRoster> wpkRosters = Imps.Roster.getListRoster(getContentResolver());

        Log.i("LTH",wpkRosters.get(0).getName());
    }
}
