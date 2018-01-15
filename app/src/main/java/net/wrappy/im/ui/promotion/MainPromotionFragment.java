package net.wrappy.im.ui.promotion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import net.wrappy.im.R;
import net.wrappy.im.comon.BaseFragmentV4;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.model.AwardHistory;
import net.wrappy.im.model.PromotionLevel;
import net.wrappy.im.model.PromotionSetting;
import net.wrappy.im.model.Promotions;
import net.wrappy.im.ui.adapters.PromotionAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ben on 02/01/2018.
 */
public class MainPromotionFragment extends BaseFragmentV4 {

    View mainView;

    @BindView(R.id.recyclerPromotion)
    RecyclerView recyclerPromotion;
    @BindView(R.id.txtPromotionBalance)
    AppTextView txtBalance;
    @BindView(R.id.btnPromotionInvite)
    LinearLayout btnPromotionInvite;

    ArrayList<Promotions> list;
    PromotionAdapter promotionAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.main_promotion_fragment, null);
        ButterKnife.bind(this, mainView);
        list = new ArrayList<>();
        promotionAdapter = new PromotionAdapter(getActivity(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerPromotion.setLayoutManager(layoutManager);
        recyclerPromotion.setAdapter(promotionAdapter);
        return mainView;
    }

    public void reloadData() {
        list.clear();
        getPromotionBalance();
        getStatusInviteFriend();
        getPromotionHistory();
    }

    @OnClick(R.id.btnPromotionInvite)
    public void onClick(View v) {
        PromotionActivity.start(getActivity(), null);
    }

    private void getPromotionHistory() {
        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_PROMOTION_HISTORY, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    if (RestAPI.checkHttpCode(httpCode)) {
                        Gson gson = new Gson();
                        AwardHistory history = gson.fromJson(s, new TypeToken<AwardHistory>() {
                        }.getType());
                        Promotions promotions = new Promotions(history.getLevel0().getTitle(), AppFuncs.convertTimestamp(history.getLevel0().getTimestamp()), history.getLevel0().getBonus());
                        list.add(promotions);
                        ArrayList<PromotionLevel> levels = history.getLevels();
                        for (PromotionLevel level : levels) {
                            promotions = new Promotions(level.getTitle(), AppFuncs.convertTimestamp(level.getTimestamp())+ " " + getString(R.string.from) + " <b>" + level.getIdentifier()+"</b> " + getString(R.string.level) + " (" + level.getLevel() + ")", level.getBonus());
                            list.add(promotions);
                        }
                        promotionAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void getStatusInviteFriend() {
        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_PROMOTION_SETTING, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    if (RestAPI.checkHttpCode(httpCode)) {
                        Gson gson = new Gson();
                        PromotionSetting promotionSetting = gson.fromJson(s, new TypeToken<PromotionSetting>(){}.getType());
                        if (promotionSetting.isEnablePromotion()) {
                            btnPromotionInvite.setVisibility(View.VISIBLE);
                        }
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void getPromotionBalance() {
        RestAPI.GetDataWrappy(getActivity(), RestAPI.GET_PROMOTION_BALANCE, new RestAPI.RestAPIListenner() {
            @Override
            public void OnComplete(int httpCode, String error, String s) {
                try {
                    if (RestAPI.checkHttpCode(httpCode)) {
                        JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
                        long balance = jsonObject.get("balance").getAsLong();
                        txtBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(balance)+".00");
                    }
                }catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void reloadFragment() {
        reloadData();
    }
}
