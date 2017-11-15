/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.List;

import me.tornado.android.patternlock.PatternUtils;
import me.tornado.android.patternlock.PatternView;

import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.util.PatternLockUtils;

public class PatternActivity extends me.tornado.android.patternlock.SetPatternActivity implements RestAPI.RectAPIListenner{


    String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);
        password = PatternUtils.patternToString(pattern);
     //   Intent returnIntent = new Intent();
      //  returnIntent.putExtra("result", PatternUtils.patternToString(pattern));
      //  setResult(this.RESULT_OK,returnIntent);
       // finish();
    }

    @Override
    public void OnComplete(String error, String s) {

    }
}
