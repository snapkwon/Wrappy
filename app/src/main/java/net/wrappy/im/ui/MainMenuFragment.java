package net.wrappy.im.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.BottomSheetCell;
import net.wrappy.im.model.BottomSheetListener;
import net.wrappy.im.ui.legacy.SettingActivity;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.PopupUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 19/12/2017.
 */

public class MainMenuFragment extends Fragment {

    View mainView;
    MainActivity mainActivity;
    @BindView(R.id.txtMainMenuVersionName)
    AppTextView txtMainMenuVersionName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.main_menu_fragment, null);
        ButterKnife.bind(this, mainView);
        mainActivity = (MainActivity) getActivity();
        String versionName = BuildConfig.VERSION_NAME;
        if (versionName != null) {
            txtMainMenuVersionName.setText(getString(R.string.app_version) + " " + versionName);
        }
        return mainView;
    }

    private void confirmDeleteAccount(int mAccountId, int mProviderId) {

        //need to delete
        ImApp.deleteAccount(getActivity().getContentResolver(), mAccountId, mProviderId);

        // ((ImApp) getActivity().getApplication()).resetDB();

        PackageManager packageManager = getActivity().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getActivity().getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(componentName);
        getActivity().startActivity(mainIntent);
        System.exit(0);

       /* Intent i =  getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(  getActivity().getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);*/


        // Intent intent = new Intent(AccountSettingsActivity.this, IntroActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //startActivity(intent);

        //  finish();
    }

    @OnClick({R.id.lnMainMenuNewGroup, R.id.lnMainMenuContact, R.id.lnMainMenuNewList, R.id.lnMainMenuSettings, R.id.lnMainMenuAboutUs, R.id.lnMainMenuFAQ, R.id.lnMainMenuLogout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lnMainMenuNewGroup:
                Intent intentGroup = new Intent(getActivity(), ContactsPickerActivity.class);
                intentGroup.putExtra(BundleKeyConstant.KEY_GROUP, true);
                getActivity().startActivityForResult(intentGroup, MainActivity.REQUEST_CHOOSE_CONTACT);
                break;
            case R.id.lnMainMenuContact:
                Intent intent = new Intent(getActivity(), ContactsPickerActivity.class);
                getActivity().startActivityForResult(intent, MainActivity.REQUEST_CHOOSE_CONTACT);
                break;
            case R.id.lnMainMenuNewList:
                AppFuncs.alert(getActivity(), "Developing", true);
                break;
            case R.id.lnMainMenuSettings:
                Intent sintent = new Intent(getActivity(), SettingActivity.class);
                getActivity().startActivityForResult(sintent, MainActivity.REQUEST_CHANGE_SETTINGS);
                break;
            case R.id.lnMainMenuAboutUs:
                Intent aboutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://proteusion.com/en/about/"));
                startActivity(aboutIntent);
                break;
            case R.id.lnMainMenuFAQ:
                Intent faqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zondir.jp/en/wrappy"));
                startActivity(faqIntent);
                break;
            case R.id.lnMainMenuLogout:
                ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
                BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_menutab_logout, getString(R.string.logout_device));
                sheetCells.add(sheetCell);
                sheetCell = new BottomSheetCell(2, R.drawable.ic_logout_all, getString(R.string.logout_all_devices));
                sheetCells.add(sheetCell);
                BottomSheetDialog bottomSheetDialog = PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
                    @Override
                    public void onSelectBottomSheetCell(int index) {
                        if (index == 1) {
                            confirmDeleteAccount(mainActivity.getDefaultAcountid(), mainActivity.getDefaultProviderid());
                            AppFuncs.alert(getActivity(), getString(R.string.logout_device), true);
                        } else if (index == 2) {
                            AppFuncs.alert(getActivity(), getString(R.string.logout_all_devices), true);
                        }
                    }
                });
                bottomSheetDialog.show();
                break;

            default:
        }
    }


}
