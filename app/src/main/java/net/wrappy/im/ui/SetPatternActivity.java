/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

import net.wrappy.im.util.PatternLockUtils;
import net.wrappy.im.util.PreferenceContract;
import net.wrappy.im.util.PreferenceUtils;

public class SetPatternActivity extends me.zhanghai.android.patternlock.SetPatternActivity {

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
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", PatternUtils.patternToString(pattern));
        setResult(this.RESULT_OK,returnIntent);
        finish();
    }
}