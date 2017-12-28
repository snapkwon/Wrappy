package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.wrappy.im.R;
import net.wrappy.im.helper.layout.AppTextView;
import net.wrappy.im.helper.layout.CircleImageView;
import net.wrappy.im.model.WpkRoster;
import net.wrappy.im.provider.Imps;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ben on 11/12/2017.
 */

public class ContactsPickerRosterActivity extends BaseActivity {

    @BindView(R.id.headerbarTitleLeft) AppTextView headerbarTitleLeft;
    @BindView(R.id.recyclerViewRoster) RecyclerView recyclerViewRoster;
    ArrayList<WpkRoster> wpkRosters = new ArrayList<>();
    RosterAdapter rosterAdapter;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity,ContactsPickerRosterActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.contacts_picker_roster_activity);
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        wpkRosters = Imps.Roster.getListRoster(getContentResolver());
        rosterAdapter.notifyDataSetChanged();
    }

    private void initView() {
        headerbarTitleLeft.setText(R.string.new_list);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewRoster.setLayoutManager(mLayoutManager);
        recyclerViewRoster.setItemAnimator(new DefaultItemAnimator());
        rosterAdapter = new RosterAdapter();
        recyclerViewRoster.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerViewRoster.setAdapter(rosterAdapter);
    }

    @OnClick({R.id.headerbarBack,R.id.btnRosterAdd})
    public void onClick(View v) {
        if (v.getId()==R.id.headerbarBack) {
            finish();
        } else if (v.getId() == R.id.btnRosterAdd) {
            ContactsPickerRosterCreateActivity.start(this);
        }
    }

    public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.RosterViewHolder> {

        @Override
        public RosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.roster_adapter_item,null);
            return new RosterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RosterViewHolder holder, int position) {
            holder.rosterName.setText(wpkRosters.get(position).getName());
            holder.rosterType.setText(wpkRosters.get(position).getType());
            //holder.rosterAvarta.setText(wpkRosters.get(position).get);
        }

        @Override
        public int getItemCount() {
            return wpkRosters.size();
        }

        public class RosterViewHolder extends RecyclerView.ViewHolder {
            AppTextView rosterName, rosterType;
            CircleImageView rosterAvarta;

            public RosterViewHolder(View itemView) {
                super(itemView);
                rosterAvarta = (CircleImageView) itemView.findViewById(R.id.avatar);
                rosterName = (AppTextView) itemView.findViewById(R.id.line1);
                rosterType = (AppTextView) itemView.findViewById(R.id.line2);
            }
        }
    }
}
