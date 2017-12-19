package net.wrappy.im.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.MainActivity;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.ui.legacy.SettingActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 19/12/2017.
 */

public class MainMenuFragment extends Fragment {

    View mainView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.main_menu_fragment,null);
        ButterKnife.bind(this,mainView);
        return mainView;
    }

    @OnClick({R.id.txtMainMenuNewGroup,R.id.txtMainMenuContact,R.id.txtMainMenuNewList,R.id.txtMainMenuSettings,R.id.txtMainMenuAboutUs,R.id.txtMainMenuFAQ,R.id.txtMainMenuLogout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtMainMenuNewGroup:
                Intent intentGroup = new Intent(getActivity(), ContactsPickerActivity.class);
                intentGroup.putExtra("isGroup",true);
                startActivityForResult(intentGroup, MainActivity.REQUEST_CHOOSE_CONTACT);
                break;
            case R.id.txtMainMenuContact:
                Intent intent = new Intent(getActivity(), ContactsPickerActivity.class);
                startActivityForResult(intent, MainActivity.REQUEST_CHOOSE_CONTACT);
                break;
            case R.id.txtMainMenuNewList:
                AppFuncs.alert(getActivity(),"Developing",true);
                break;
            case R.id.txtMainMenuSettings:
                Intent sintent = new Intent(getActivity(), SettingActivity.class);
                startActivityForResult(sintent, MainActivity.REQUEST_CHANGE_SETTINGS);
                break;
            case R.id.txtMainMenuAboutUs:
                Intent aboutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://proteusion.com/en/about/"));
                startActivity(aboutIntent);
                break;
            case R.id.txtMainMenuFAQ:
                Intent faqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.zondir.jp/en/wrappy"));
                startActivity(faqIntent);
                break;
            case R.id.txtMainMenuLogout:
                break;

            default:
        }
    }
}
