package net.wrappy.im.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.glide.GlideHelper;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.ui.ConversationDetailActivity;
import net.wrappy.im.ui.widgets.LetterAvatar;
import net.wrappy.im.util.BundleKeyConstant;

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
        holder.bind(wpKMemberDtos.get(position), position);
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

        @BindView(R.id.image_section)
        TextView imagesection;

        @BindView(R.id.linesection)
        FrameLayout linesection;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final WpKMemberDto wpKMemberDto, int position) {
            line1.setText(wpKMemberDto.getIdentifier());
            line2.setVisibility(View.INVISIBLE);
            int padding = 24;
            mAvatar.setVisibility(View.VISIBLE);
            if (wpKMemberDto.getAvatar() != null && !TextUtils.isEmpty(wpKMemberDto.getAvatar().getReference())) {
                GlideHelper.loadBitmapToCircleImage(mContext, mAvatar, RestAPI.getAvatarUrl(wpKMemberDto.getAvatar().getReference()));
            } else {
                LetterAvatar lavatar = new LetterAvatar(mContext, wpKMemberDto.getIdentifier(), padding);
                mAvatar.setImageDrawable(lavatar);
            }

            imagesection.setVisibility(View.GONE);

            linesection.setVisibility(View.GONE);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wpKMemberDto.getXMPPAuthDto() != null) {
                        Intent intent = ConversationDetailActivity.getStartIntent(mContext);
                        intent.putExtra(BundleKeyConstant.ADDRESS_KEY, wpKMemberDto.getXMPPAuthDto().getAccount());
                        intent.putExtra(BundleKeyConstant.NICK_NAME_KEY, wpKMemberDto.getIdentifier());
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }
}