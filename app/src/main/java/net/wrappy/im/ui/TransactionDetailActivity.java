package net.wrappy.im.ui;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thefinestartist.finestwebview.FinestWebView;

import net.wrappy.im.R;
import net.wrappy.im.model.ModelHistoryTransaction;

public class TransactionDetailActivity extends AppCompatActivity {

    private LinearLayout MainLayout;

    private ModelHistoryTransaction transaction;
    private String txUrl = "https://rinkeby.etherscan.io/tx/";
    ImageView icon,wallet_tx_coin;
    TextView title,number,time,confirmNumber;
    RelativeLayout transaction_detail_btn;
    ImageButton iconBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //change default font

//        MainLayout = new LinearLayout(this);
//        MainLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(R.layout.activity_transaction_detail);
//        MainLayout.setBackgroundColor(0xffffffff);


        transaction = new ModelHistoryTransaction();
        Bundle b = getIntent().getExtras();
        transaction.setNumber(b.getString("ethereumnumber"));
        transaction.setTitle(b.getString("type"));
        transaction.setTime(b.getString("time"));
        transaction.setConfirmNumber(b.getLong("confirmNumber"));
        transaction.setToUSD(b.getString("toUSD"));
        transaction.setTXHash(b.getString("txHash"));
        String type = b.getString("type");
        wallet_tx_coin = (ImageView) findViewById(net.wrappy.im.R.id.wallet_tx_coin);


        icon = (ImageView) findViewById(net.wrappy.im.R.id.transaction_detail_icon);
        title = (TextView) findViewById(net.wrappy.im.R.id.transaction_detail_title);
        number = (TextView) findViewById(net.wrappy.im.R.id.transaction_detail_number);
        confirmNumber = (TextView) findViewById(net.wrappy.im.R.id.transaction_detail_confirm);
        time = (TextView) findViewById(net.wrappy.im.R.id.transaction_detail_time);
        transaction_detail_btn = (RelativeLayout) findViewById(net.wrappy.im.R.id.transaction_detail_btn);
        try {
            if (b.getString("coin").equalsIgnoreCase("prd")) {
                wallet_tx_coin.setImageResource(net.wrappy.im.R.drawable.ic_proteusion);
                number.setTextColor(getResources().getColor(net.wrappy.im.R.color.textproteusion));
            }
        }catch (Exception ex){}
        if(type.equalsIgnoreCase("Received")) {
            icon.setImageResource(net.wrappy.im.R.drawable.ic_receive_pink2x);
        } else {
            icon.setImageResource(net.wrappy.im.R.drawable.ic_send_pink2x);
        }
        title.setText(transaction.getTitle());
        number.setText(transaction.getNumber());
        time.setText(transaction.getTime());
        if (transaction.getConfirmNumber() > 12){
            confirmNumber.setText("12+");
        }else{
            confirmNumber.setText(String.valueOf(transaction.getConfirmNumber()));
        }
        transaction_detail_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txUrl = txUrl + transaction.getTXHash();
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(txUrl));
//                startActivity(browserIntent);
                new FinestWebView.Builder(TransactionDetailActivity.this).show(txUrl);
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
