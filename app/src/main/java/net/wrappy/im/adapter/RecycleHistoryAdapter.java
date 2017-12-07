package net.wrappy.im.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.model.ModelHistoryTransaction;

import java.util.List;

/**
 * Created by PCPV on 11/29/2017.
 */

public class RecycleHistoryAdapter extends RecyclerView.Adapter<RecycleHistoryAdapter.RecyclerViewHolder> {

    List<ModelHistoryTransaction> myArray=null;
    private static ClickListener clickListener;
    String typeofCoin;

    public RecycleHistoryAdapter(List<ModelHistoryTransaction> listData,String typeofCoin) {
        this.myArray = listData;
        this.typeofCoin = typeofCoin;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecycleHistoryAdapter.clickListener = clickListener;
    }

    public void updateList(List<ModelHistoryTransaction> data) {
        Log.e("TTT",data.toString());
        myArray = data;
        notifyDataSetChanged();
    }

    @Override
    public RecycleHistoryAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.history_list_transaction_adapter, parent, false);
        return new RecycleHistoryAdapter.RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecycleHistoryAdapter.RecyclerViewHolder holder, int position) {
        ModelHistoryTransaction history =myArray.get(position);
        String type = history.getTitle();
        holder.imIcon.setImageDrawable(history.getIcon());
        holder.txtTitle.setText(history.getTitle().toString());
        holder.txtTime.setText(history.getTime().toString());
        holder.txtNumber.setText(history.getNumber() + " " + typeofCoin);
    }

    @Override
    public int getItemCount() {
        return myArray.size();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }


    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imIcon;
        public TextView txtTitle;
        public TextView txtTime;
        public TextView txtNumber;
        public RecyclerViewHolder(View itemView) {
            super(itemView);

            imIcon = (ImageView) itemView.findViewById(net.wrappy.im.R.id.IconTransaction);

            txtTitle=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVTitle);
            txtTitle.setPadding(5, 10,10,0);
            txtTime=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVTime);
            txtTime.setPadding(5,10,10,5);
            txtNumber=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVNumber);
            txtNumber.setPadding(0,5,0,10);

            // itemView.setOnTouchListener(itemTouchListener);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(position, v);
            }
        }
    }
}

