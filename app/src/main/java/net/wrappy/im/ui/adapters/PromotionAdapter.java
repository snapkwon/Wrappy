package net.wrappy.im.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.model.Promotions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ben on 02/01/2018.
 */

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.ViewHolder> {

    Context context;
    ArrayList<Promotions> list;

    public PromotionAdapter(Context context, ArrayList<Promotions> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.promotion_item,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.txtPromotionName.setText(list.get(position).getName());
        String description = list.get(position).getTime();
        holder.txtPromotionTime.setText(Html.fromHtml(description));
        holder.txtPromotionCoin.setText("+ " + String.valueOf(list.get(position).getNumber()) + " PTS");
    }

    @Override
    public int getItemCount() {
        if (list==null) {
            return 1;
        }
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txtPromotionName)
        TextView txtPromotionName;
        @BindView(R.id.txtPromotionTime)
        TextView txtPromotionTime;
        @BindView(R.id.txtPromotionCoin)
        TextView txtPromotionCoin;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
