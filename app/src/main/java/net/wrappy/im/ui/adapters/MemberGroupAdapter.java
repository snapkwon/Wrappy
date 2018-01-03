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
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.util.Debug;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by QUOC CUONG on 12/20/2017.
 */

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.ViewHolder> {

    private ArrayList<MemberGroupDisplay> mMembers;
    private Context mContext;
    private String currentUser;
    private String mAdminGroup;

    public MemberGroupAdapter(Context mContext, ArrayList<MemberGroupDisplay> mMembers, String currentUser, String mAdminGroup) {
        this.mContext = mContext;
        this.mMembers = mMembers;
        this.currentUser = currentUser;
        this.mAdminGroup = mAdminGroup;
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
        @BindView(R.id.avatarCrown)
        ImageView avatarCrown;
        @BindView(R.id.delete_member_group)
        ImageView mDeleteMember;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(MemberGroupDisplay member) {
            line1.setText(member.getNickname());
            int padding = 24;

            GlideHelper.loadAvatarFromNickname(itemView.getContext(), avatar, member.getNickname());
            String referenceAvatar = Imps.Avatars.getAvatar(itemView.getContext().getContentResolver(), member.getUsername());
            if (!TextUtils.isEmpty(referenceAvatar)) {
                GlideHelper.loadBitmapToCircleImage(itemView.getContext(), avatar, RestAPI.getAvatarUrl(referenceAvatar));
            }

            if (currentUser.equals(mAdminGroup)) {
                if (isAdminGroup(member)) {
                    avatarCrown.setVisibility(View.VISIBLE);
                    mDeleteMember.setVisibility(View.GONE);
                } else {
                    mDeleteMember.setVisibility(View.VISIBLE);
                }
            } else {
                if (isAdminGroup(member)) {
                    avatarCrown.setVisibility(View.VISIBLE);
                }
                mDeleteMember.setVisibility(View.GONE);
            }
        }
    }

    private boolean isAdminGroup(MemberGroupDisplay member) {
        if (member.getAffiliation() != null && (member.getAffiliation().contentEquals("owner") ||
                member.getAffiliation().contentEquals("admin"))) {
            return true;
        }
        return false;
    }
}
