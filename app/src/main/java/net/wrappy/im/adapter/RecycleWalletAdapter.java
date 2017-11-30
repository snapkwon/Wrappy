package net.wrappy.im.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.R;
import net.wrappy.im.model.ModelWaletListView;

import java.util.List;

/**
 * Created by PCPV on 11/29/2017.
 */

public class RecycleWalletAdapter extends RecyclerView.Adapter<RecycleWalletAdapter.RecyclerViewHolder> {

    List<ModelWaletListView> myArray=null;
    private static ClickListener clickListener;
    public RecycleWalletAdapter(List<ModelWaletListView> listData) {
        this.myArray = listData;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        RecycleWalletAdapter.clickListener = clickListener;
    }

    public void updateList(List<ModelWaletListView> data) {
        myArray = data;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.wallet_list_adapter, parent, false);
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder,final int position) {

        ModelWaletListView wallet=myArray.get(position);

        holder.imIcon.setImageDrawable(wallet.getIcon());
        holder.txtName.setText(wallet.getNameCoin().toString());
        holder.txtName.setTextColor(wallet.getTextColor());
        holder.txtPercent.setTextColor(wallet.getTextColor());
        holder.txtPrice.setText("$"+wallet.getPriceCoin());
        holder.txtPercent.setText(wallet.getPercent()+ "%");
        holder.txtValue.setText(wallet.getValue());
    }



    @Override
    public int getItemCount() {
        return myArray.size();
    }


    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public View.OnTouchListener itemTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(0xffdcdcdc);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // CANCEL triggers when you press the view for too long
                    // It prevents UP to trigger which makes the 'pressed' background permanent which isn't what we want
                case MotionEvent.ACTION_OUTSIDE:
                    // OUTSIDE triggers when the user's finger moves out of the view
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(0xffffffff);
                    break;
                default:
                    break;
            }

            return true;
        }

    };

    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imIcon;
        public TextView txtName;
        public TextView txtPrice;
        public TextView txtPercent;
        public TextView txtValue;
        public RecyclerViewHolder(View itemView) {
            super(itemView);

            imIcon = (ImageView) itemView.findViewById(net.wrappy.im.R.id.IconCoin);


            txtName=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVNameCoin);
            txtName.setPadding(0, 10,10,5);
            txtPrice=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVPrice);
            txtPrice.setPadding(10,10,10,5);
            txtPercent=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVPercent);
            txtPercent.setPadding(0,5,10,10);
            txtValue=(TextView)
                    itemView.findViewById(net.wrappy.im.R.id.TVValue);
            txtValue.setPadding(10,5,10,10);

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
