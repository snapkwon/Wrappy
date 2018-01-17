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

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.ui.stickers.Sticker;
import net.wrappy.im.ui.stickers.StickerGroup;
import net.wrappy.im.ui.stickers.StickerManager;
import net.wrappy.im.ui.stickers.StickerPagerAdapter;
import net.wrappy.im.ui.stickers.StickerSelectListener;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class StickerActivity extends BaseActivity {


    private ImApp mApp;
    private ViewPager mStickerPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.awesome_activity_stickers);
        setTitle(R.string.action_stickers);
        Intent intent = getIntent();
        mApp = (ImApp)getApplication();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mStickerPager = (ViewPager)findViewById(R.id.stickerPager);

        applyStyleForToolbar();

        initStickers();
    }


    public void applyStyleForToolbar() {



        //not set color
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int selColor = settings.getInt("themeColor",-1);

        if (selColor != -1) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setNavigationBarColor(selColor);
                getWindow().setStatusBarColor(selColor);
            }

            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(selColor));
        }

    }

    private void initStickers ()
    {

        Collection<StickerGroup> emojiGroups = StickerManager.getInstance(this).getEmojiGroups();

        if (emojiGroups.size() > 0)
        {
            StickerPagerAdapter emojiPagerAdapter = new StickerPagerAdapter(this, new ArrayList<StickerGroup>(emojiGroups),
                    new StickerSelectListener() {
                        @Override
                        public void onStickerSelected(Sticker s) {

                           exportAsset(s.assetUri,s.name);

                        }
                    });

            mStickerPager.setAdapter(emojiPagerAdapter);

        }


    }

    private final static int MY_PERMISSIONS_REQUEST_FILE = 1;

    private boolean checkPermissions ()
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck ==PackageManager.PERMISSION_DENIED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {


                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(mStickerPager, R.string.grant_perms, Snackbar.LENGTH_LONG).show();
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_FILE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            return false;

        }
        else {

            return true;
        }
    }

    private void exportAsset (Uri mediaUri, String name)
    {
        if (checkPermissions()) {
            try {

                String mimeType = "image/png";
                java.io.File exportPath = new File(Environment.getExternalStorageDirectory(), name + ".png");

                java.io.InputStream fis = getResources().getAssets().open(mediaUri.getPath());
                java.io.FileOutputStream fos = new java.io.FileOutputStream(exportPath, false);

                IOUtils.copyLarge(fis, fos);

                fos.close();
                fis.close();

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportPath));
                shareIntent.setType(mimeType);
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.export_media)));
            } catch (IOException e) {
                AppFuncs.alert(this, "Export Failed " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      //  getMenuInflater().inflate(R.menu.menu_conversation_detail, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);



    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}
