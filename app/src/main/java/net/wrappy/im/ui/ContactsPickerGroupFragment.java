package net.wrappy.im.ui;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.util.SecureMediaStore;

/**
 * Created by ben on 22/11/2017.
 */

public class ContactsPickerGroupFragment extends Fragment implements View.OnClickListener {

    View mainView;
    private final int IMAGE_AVARTA = 101;
    ImageButton btnGroupPhoto;

    public static ContactsPickerGroupFragment newsIntance() {
        return new ContactsPickerGroupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.contacts_picker_group_fragment,null);
        btnGroupPhoto = (ImageButton) mainView.findViewById(R.id.btnGroupPhoto);
        btnGroupPhoto.setOnClickListener(this);
        return mainView;
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            AppFuncs.getImageFromDevice(getActivity(),IMAGE_AVARTA);
            } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},199);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data!=null) {
                if (requestCode==IMAGE_AVARTA) {
                    Bitmap bmpThumbnail = SecureMediaStore.getThumbnailFile(getActivity(), data.getData(), 512);
                    btnGroupPhoto.setImageBitmap(bmpThumbnail);
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }


    }
}
