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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.ironrabbit.type.CustomTypefaceEditText;
import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListenner;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.tasks.AddContactAsyncTask;
import net.wrappy.im.ui.adapters.ContactAdapter;
import net.wrappy.im.ui.legacy.SimpleAlertHandler;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.Debug;

import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.BindView;


public class AddContactNewActivity extends BaseActivity {
    private static final String TAG = AddContactNewActivity.class.getSimpleName();

    ImApp mApp;
    SimpleAlertHandler mHandler;

    @BindView(R.id.edt_search_username)
    CustomTypefaceEditText search_username;
    @BindView(R.id.lstContacts)
    RecyclerView lstContacts;

    private ContactAdapter contactAdapter;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.add_contact_new_activity);

        super.onCreate(savedInstanceState);

        setTitle(R.string.add_friends);

        mApp = (ImApp) getApplication();
        mHandler = new SimpleAlertHandler(this);


        lstContacts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        contactAdapter = new ContactAdapter(this, new ArrayList<WpKMemberDto>());
        lstContacts.setAdapter(contactAdapter);

        search_username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
//                    inviteBuddies();
                    searchUsername(search_username.getText().toString());
                    AppFuncs.dismissKeyboard(AddContactNewActivity.this);
                }
                return false;
            }
        });
//        Intent intent = getIntent();
//        String scheme = intent.getScheme();
//        if (TextUtils.equals(scheme, "xmpp"))
//        {
//            addContactFromUri(intent.getData());
//        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void searchUsername(String s) {
        if (!TextUtils.isEmpty(s)) {
            RestAPI.GetDataWrappy(this, String.format(RestAPI.GET_SEARCH_USERNAME, s), new RestAPIListenner() {
                @Override
                public void OnComplete(int httpCode, String error, String s) {
                    Debug.d(s);
                    try {
                        ArrayList<WpKMemberDto> wpKMemberDtos = new Gson().fromJson(s, new TypeToken<ArrayList<WpKMemberDto>>() {
                        }.getType());
                        contactAdapter.setData(wpKMemberDtos);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkConnection()) {
            Snackbar sb = Snackbar.make(findViewById(R.id.main_content), R.string.error_suspended_connection, Snackbar.LENGTH_LONG);
//            sb.setAction(getString(R.string.connect), new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent i = new Intent(AddContactNewActivity.this, AccountsActivity.class);
//                    startActivity(i);
//                }
//            });
            sb.show();

        }
    }

    void inviteBuddies() {
        Rfc822Token[] recipients = Rfc822Tokenizer.tokenize("");

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        boolean foundOne = false;

        for (Rfc822Token recipient : recipients) {

            String address = recipient.getAddress();
            if (pattern.matcher(address).matches()) {
                new AddContactAsyncTask(mApp.getDefaultProviderId(), mApp.getDefaultAccountId(), mApp).execute(address, null, null);
                foundOne = true;
            }
        }

        if (foundOne) {
            Intent intent = new Intent();
            intent.putExtra(BundleKeyConstant.RESULT_KEY, recipients[0].getAddress());
            intent.putExtra(BundleKeyConstant.PROVIDER_KEY, mApp.getDefaultProviderId());
            setResult(RESULT_OK, intent);
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    private boolean checkConnection() {
        try {
            if (mApp.getDefaultProviderId() != -1) {
                IImConnection conn = mApp.getConnection(mApp.getDefaultProviderId(), mApp.getDefaultAccountId());

                if (conn.getState() == ImConnection.DISCONNECTED
                        || conn.getState() == ImConnection.SUSPENDED
                        || conn.getState() == ImConnection.SUSPENDING)
                    return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
