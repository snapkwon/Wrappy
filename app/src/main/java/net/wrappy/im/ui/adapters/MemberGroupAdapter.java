package net.wrappy.im.ui.adapters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.R;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.util.PopupUtils;

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
    private long mLastChatId;
    private WpKChatGroupDto mWpKChatGroupDto;

    public void setmWpKChatGroupDto(WpKChatGroupDto mWpKChatGroupDto) {
        this.mWpKChatGroupDto = mWpKChatGroupDto;
    }

    public MemberGroupAdapter(Context mContext, ArrayList<MemberGroupDisplay> mMembers, String currentUser, String mAdminGroup, long mLastChatId) {
        this.mContext = mContext;
        this.mMembers = mMembers;
        this.currentUser = currentUser;
        this.mAdminGroup = mAdminGroup;
        this.mLastChatId = mLastChatId;
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
        @BindView(R.id.layout_remove_member)
        RelativeLayout mRemoveMember;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final MemberGroupDisplay member) {
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

                    mDeleteMember.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmRemoveMember(getAdapterPosition(), member);
                        }
                    });
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

    private void confirmRemoveMember(final int position, final MemberGroupDisplay member) {
        PopupUtils.showCustomDialog(mContext, mContext.getString(R.string.action_remove_member_group), mContext.getString(R.string.confirm_remove_member_group), R.string.yes, R.string.no, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWpKChatGroupDto != null) {
                    RestAPI.apiDELETE(mContext, String.format(RestAPI.DELETE_MEMBER_GROUP, mWpKChatGroupDto.getId(),
                            member.getNickname()), null).setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if (result != null && RestAPI.checkHttpCode(result.getHeaders().code())) {
                                AppFuncs.log(result.getResult());
                                AppFuncs.alert(mContext, "Remove " + member.getNickname() + " in this group", false);
                            }
                        }
                    });
                    removeMemberInArray(position);
                    removeMemberInDB(member);
                }
            }
        }, null, false);
    }

    private void removeMemberInArray(int position) {
        mMembers.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mMembers.size());
    }

    private void removeMemberInDB(MemberGroupDisplay member) {
        StringBuilder buf = new StringBuilder();

        buf.append(Imps.GroupMembers.USERNAME);
        buf.append(" LIKE ");
        android.database.DatabaseUtils.appendValueToSql(buf, "%" + member.getUsername() + "%");

        Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, mLastChatId);
        ContentResolver cr = mContext.getContentResolver();
        cr.delete(memberUri, buf.toString(), null);
    }
}
