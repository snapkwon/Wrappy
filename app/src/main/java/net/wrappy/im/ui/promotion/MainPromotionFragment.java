package net.wrappy.im.ui.promotion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.Promotions;
import net.wrappy.im.ui.adapters.PromotionAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 02/01/2018.
 */

public class MainPromotionFragment extends Fragment {

    View mainView;

    @BindView(R.id.recyclerPromotion)
    RecyclerView recyclerPromotion;
    @BindView(R.id.txtPromotionBalance)
    AppTextView txtBalance;

    ArrayList<Promotions> list;
    PromotionAdapter promotionAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView  = inflater.inflate(R.layout.main_promotion_fragment,null);
        ButterKnife.bind(this, mainView);
        list = new ArrayList<>();
        Promotions promotions = new Promotions("Receiver","20/01/2017",100);
        list.add(promotions);
        promotionAdapter = new PromotionAdapter(getActivity(),list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerPromotion.setLayoutManager(layoutManager);
        recyclerPromotion.setAdapter(promotionAdapter);
        return mainView;
    }

    @OnClick(R.id.btnPromotionInvite)
    public void onClick(View v) {
        PromotionActivity.start(getActivity(),null);
    }

}
