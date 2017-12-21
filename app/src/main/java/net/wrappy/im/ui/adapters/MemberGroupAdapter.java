package net.wrappy.im.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.ui.widgets.LetterAvatar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by QUOC CUONG on 12/20/2017.
 */

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.ViewHolder> {

    private ArrayList<MemberGroupDisplay> mMembers;
    private Context mContext;

    public MemberGroupAdapter(ArrayList<MemberGroupDisplay> mMembers, Context mContext) {
        this.mMembers = mMembers;
        this.mContext = mContext;
    }

    public void setData(ArrayList<MemberGroupDisplay> groups) {
        this.mMembers = groups;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_member_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(mMembers.get(position));
    }

    @Override
    public int getItemCount() {
        return mMembers == null ? 0 : mMembers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.avatar)
        ImageView avatar;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(MemberGroupDisplay groupMemberDisplay) {
            line1.setText(groupMemberDisplay.getNickname());
            line2.setText("");
            int padding = 24;
            avatar.setVisibility(View.VISIBLE);
            if (avatar == null && !TextUtils.isEmpty(groupMemberDisplay.getNickname())) {
                LetterAvatar la = new LetterAvatar(mContext, groupMemberDisplay.getNickname(), padding);
                avatar.setImageDrawable(la);
            }
        }
    }
}
