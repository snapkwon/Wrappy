package net.wrappy.im.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.ImApp;
import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.comon.BaseFragmentV4;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.util.BundleKeyConstant;
import net.wrappy.im.util.PopupUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 19/12/2017.
 */

public class MainMenuFragment extends BaseFragmentV4 {

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

    private void logout() {
        ImApp.sImApp.logout();
        getActivity().finishAffinity();
    }
    boolean isLoadWeb;
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
//                Intent sintent = new Intent(getActivity(), SettingActivity.class);
//                getActivity().startActivityForResult(sintent, MainActivity.REQUEST_CHANGE_SETTINGS);
                break;
            case R.id.lnMainMenuAboutUs:

                WebViewActivity.start(getActivity(),getString(R.string.about_us),"http://wrappy.net/#about");
                break;
            case R.id.lnMainMenuFAQ:
                WebViewActivity.start(getActivity(),getString(R.string.faq),"http://wrappy.net/faq.html");
                break;
            case R.id.lnMainMenuLogout:
                PopupUtils.showCustomDialog(getActivity(), getString(R.string.warning), getString(R.string.logout_noti), R.string.ok, R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logout();
                    }
                },null);
//                ArrayList<BottomSheetCell> sheetCells = new ArrayList<>();
//                BottomSheetCell sheetCell = new BottomSheetCell(1, R.drawable.ic_menutab_logout, getString(R.string.logout_device));
//                sheetCells.add(sheetCell);
//                sheetCell = new BottomSheetCell(2, R.drawable.ic_logout_all, getString(R.string.logout_all_devices));
//                sheetCells.add(sheetCell);
//                BottomSheetDialog bottomSheetDialog = PopupUtils.createBottomSheet(getActivity(), sheetCells, new BottomSheetListener() {
//                    @Override
//                    public void onSelectBottomSheetCell(int index) {
//                        if (index == 1) {
//                            logout();
//                            //AppFuncs.alert(getActivity(), getString(R.string.logout_device), true);
//                        } else if (index == 2) {
//                            logout();
//                            //AppFuncs.alert(getActivity(), getString(R.string.logout_all_devices), true);
//                        }
//                    }
//                });
//                bottomSheetDialog.show();
                break;

            default:
        }
    }


    @Override
    public void reloadFragment() {

    }
}
