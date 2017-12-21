package net.wrappy.im.ui.adapters;

import android.content.Context;
import android.content.Intent;
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
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.ui.widgets.LetterAvatar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hp on 12/1/2017.
 */

public class ContactAdapter
        extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private ArrayList<WpKMemberDto> wpKMemberDtos;
    private Context mContext;

    public ContactAdapter(Context context, ArrayList<WpKMemberDto> wpKMemberDtos) {
        this.wpKMemberDtos = wpKMemberDtos;
        mContext = context;
    }

    public void setData(ArrayList<WpKMemberDto> wpKMemberDtos) {
        this.wpKMemberDtos = wpKMemberDtos;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(wpKMemberDtos.get(position));
    }

    @Override
    public int getItemCount() {
        return wpKMemberDtos == null ? 0 : wpKMemberDtos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.line1)
        TextView line1;
        @BindView(R.id.line2)
        TextView line2;
        @BindView(R.id.avatar)
        ImageView mAvatar;
        @BindView(R.id.message_container)
        View container;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final WpKMemberDto wpKMemberDto) {
            line1.setText(wpKMemberDto.getIdentifier());
            line2.setText(wpKMemberDto.getEmail());
            int padding = 24;
            mAvatar.setVisibility(View.VISIBLE);
            LetterAvatar lavatar = new LetterAvatar(mContext, wpKMemberDto.getIdentifier(), padding);
            mAvatar.setImageDrawable(lavatar);
            if (!TextUtils.isEmpty(wpKMemberDto.getReference())) {
                GlideHelper.loadBitmapToCircleImage(mContext, mAvatar, RestAPI.getAvatarUrl(wpKMemberDto.getReference()));
            }
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wpKMemberDto.getXMPPAuthDto() != null) {
                        Intent intent = ConversationDetailActivity.getStartIntent(mContext);
                        intent.putExtra("address", wpKMemberDto.getXMPPAuthDto().getAccount());
                        intent.putExtra("nickname", wpKMemberDto.getIdentifier());
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }
}