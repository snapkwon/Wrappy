package net.wrappy.im.ui.adapters;

import android.app.Activity;
import android.os.RemoteException;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.helper.AppFuncs;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.model.MemberGroupDisplay;
import net.wrappy.im.model.WpKChatGroupDto;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.ui.ProfileActivity;
import net.wrappy.im.ui.conference.ConferenceConstant;
import net.wrappy.im.util.PopupUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by QUOC CUONG on 12/20/2017.
 */

public class MemberGroupAdapter extends RecyclerView.Adapter<MemberGroupAdapter.ViewHolder> {

    private ArrayList<MemberGroupDisplay> mMembers;
    private Activity mContext;
    private String currentUser;
    private String mOwnerGroup;
    private long mLastChatId;
    private WpKChatGroupDto mWpKChatGroupDto;
    private IChatSession session;
    private ImApp mApp;

    public void setmWpKChatGroupDto(WpKChatGroupDto mWpKChatGroupDto) {
        this.mWpKChatGroupDto = mWpKChatGroupDto;
    }

    public void setSession(IChatSession session) {
        this.session = session;
    }

    public MemberGroupAdapter(Activity mContext, ArrayList<MemberGroupDisplay> mMembers, String currentUser, String mOwnerGroup, long mLastChatId, IChatSession session) {
        this.mContext = mContext;
        this.mMembers = mMembers;
        this.currentUser = currentUser;
        this.mOwnerGroup = mOwnerGroup;
        this.mLastChatId = mLastChatId;
        this.session = session;
        mApp = (ImApp) mContext.getApplication();
    }

    public void setData(ArrayList<MemberGroupDisplay> groups) {
        this.mMembers = groups;
        notifyDataSetChanged();
    }

    public void setOwner(String mOwnerGroup) {
        this.mOwnerGroup = mOwnerGroup;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_member_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bind(mMembers.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MemberGroupDisplay display = mMembers.get(position);

                if (!display.getUsername().equalsIgnoreCase(mApp.getDefaultUsername().split("@")[0])) {
                    ProfileActivity.start(mContext,display.getUsername());
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return mMembers == null ? 0 : mMembers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

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
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

        }

        public void bind(final MemberGroupDisplay member) {


            if(!TextUtils.isEmpty(member.getNickname()))
            {
                line1.setText(member.getNickname());

                GlideHelper.loadAvatarFromNickname(itemView.getContext(), avatar, member.getNickname());

            } else {
                String nameText = member.getUsername();
                if (member.getUsername().contains("@")) {
                    String[] name = member.getUsername().split("@");
                    nameText = name[0];
                }
                line1.setText(nameText);
                GlideHelper.loadAvatarFromNickname(itemView.getContext(), avatar, nameText);
            }


            if (!TextUtils.isEmpty(member.getReferenceAvatar())) {
                GlideHelper.loadBitmapToCircleImage(itemView.getContext(), avatar, RestAPI.getAvatarUrl(member.getReferenceAvatar()));
            }
            mDeleteMember.setVisibility(View.GONE);
            if (currentUser.equals(mOwnerGroup)) {
                if (isAdminGroup(member)) {
                    avatarCrown.setVisibility(View.VISIBLE);
                } else {
//                    mDeleteMember.setVisibility(View.VISIBLE);
//
//                    mDeleteMember.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            confirmRemoveMember(getAdapterPosition(), member);
//                        }
//                    });
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
        if(member.getAffiliation() == null)
        {
            return  false;
        }
        else if(member.getAffiliation().contentEquals("owner") ||
                member.getAffiliation().contentEquals("admin"))
        {
            return true;
        }
        return false;
    }

    private void confirmRemoveMember(final int position, final MemberGroupDisplay member) {
        PopupUtils.showCustomDialog(mContext, mContext.getString(R.string.action_remove_member_group), mContext.getString(R.string.confirm_remove_member_group), R.string.yes, R.string.no, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWpKChatGroupDto != null) {
                    RestAPI.DeleteDataWrappy(mContext, new JsonObject(), String.format(RestAPI.DELETE_MEMBER_GROUP, mWpKChatGroupDto.getId(),
                            member.getNickname()), new RestAPIListener(mContext) {
                        @Override
                        public void OnComplete(String s) {
                            AppFuncs.alert(mContext, String.format(mContext.getString(R.string.remove_member_success), member.getNickname()), false);
                            try {
                                // session.removeMemberGroup();
                                session.notifycationMemberLeft(member.getUsername());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            removeMemberInArray(position);
                            removeMemberInDB(member);
                        }
                    });
                }
            }
        }, null, false);
    }

    private void removeMemberInArray(int position) {
        mMembers.remove(position);
        notifyDataSetChanged();
    }

    private void removeMemberInDB(MemberGroupDisplay member) {
        StringBuilder buf = new StringBuilder();

        buf.append(Imps.GroupMembers.USERNAME);
        buf.append(" LIKE ");
        android.database.DatabaseUtils.appendValueToSql(buf, "%" + member.getUsername() + "%");

      /*  Uri memberUri = ContentUris.withAppendedId(Imps.GroupMembers.CONTENT_URI, mLastChatId);
        ContentResolver cr = mContext.getContentResolver();
        cr.delete(memberUri, buf.toString(), null);*/

        StringBuffer sb = new StringBuffer();
        sb.append(ConferenceConstant.REMOVE_MEMBER_GROUP_BY_ADMIN);
        sb.append(member.getNickname());
        try {
            session.sendMessage(sb.toString(), false);
           // session.removeMemberGroup();
           //session.notifycationMemberLeft(member.getUsername());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
