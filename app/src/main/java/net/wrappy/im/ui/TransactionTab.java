package net.wrappy.im.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.db.Balance;
import net.wrappy.im.GethService.db.BalanceRepo;
import net.wrappy.im.R;
import net.wrappy.im.adapter.RecycleHistoryAdapter;
import net.wrappy.im.model.ModelHistoryTransaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TransactionTab extends AppCompatActivity {

    public static final String TRANSACTION_TAB = "TRANSACTION_TAB";
    public static final String PEER_COUNT = "PEER_COUNT";
    public static final String CURRENT_BLOCK = "CURRENT_BLOCK";
    public static final String HIGHEST_BLOCK = "HIGHEST_BLOCK";
    public static final String TRANSACTION_TAB_FINISHED = "TRANSACTION_TAB_FINISHED";

    public final String ETHEREUM_GET = "https://rinkeby.etherscan.io/api?module=account&action=txlist&address=%s&startblock=0&endblock=%s&page=1&offset=100&sort=desc&apikey=FNTR6ZU3WHPVBM6FA1J249UKSHSEPVG5T8";

    public final String ETHEREUM_GET_BALANCE = "https://rinkeby.etherscan.io/api?module=account&action=balance&address=%s&tag=latest&apikey=FNTR6ZU3WHPVBM6FA1J249UKSHSEPVG5T8";

    private FrameLayout MainLayout;
    private RecyclerView Listview ;
    private TextView EthereumNumber;
    private TextView EthereumValue;
    private TextView SearchType;
    private RecycleHistoryAdapter adapter;
    private List<ModelHistoryTransaction> arrWallet;
    private Double ETHrate;
    private Button fab;
    FrameLayout viewMain;
    TextView txtPeer;
    TextView txtBlock;
    ReceiveTransactionList historytask;
    ProgressBar progressBarWaiting;
    WalletInfo wallet;
    String hexAddress = "";
    UpdateUITasks updateUITask;
    BalanceRepo balanceRepo;
    Balance balance;

    Bundle args ;

    private LocalBroadcastManager mLocalBroadcastManager;

    private final BroadcastReceiver mBroadcastReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TRANSACTION_TAB)) {
                long peers =  intent.getExtras().getLong(PEER_COUNT);
                long currentBlock = intent.getExtras().getLong(CURRENT_BLOCK);
                long highestBlock = intent.getExtras().getLong(HIGHEST_BLOCK);

                wallet.setCurrentBlock(currentBlock);
                wallet.setHighestBlock(highestBlock);
                wallet.setPeerCount(peers);

                txtPeer.setText("Peers: " + wallet.getPeerCount());
                double ratio = wallet.getCurrentBlock() / (double) wallet.getHighestBlock();
                DecimalFormat percentFormat= new DecimalFormat("#.##%");
                txtBlock.setText("| Blocks: " + wallet.getCurrentBlock() + " (" + percentFormat.format(ratio) + ")");
            }

            if (intent.getAction().equals(TRANSACTION_TAB_FINISHED)) {
                long peers =  intent.getExtras().getLong(PEER_COUNT);
                long currentBlock = intent.getExtras().getLong(CURRENT_BLOCK);

                wallet.setCurrentBlock(currentBlock);
                wallet.setPeerCount(peers);

                txtPeer.setText("Peers: " + wallet.getPeerCount());
                txtBlock.setText("| Blocks: " + wallet.getCurrentBlock() + " (100%)");

            }

            if(updateUITask == null) {
                updateUITask = new UpdateUITasks();
                updateUITask.execute();
            }

            if(historytask == null) {
                historytask = new ReceiveTransactionList();
                historytask.execute();
            }
        }
    };

    public void SetListTransaction(List<ModelHistoryTransaction> list)
    {
        if (list!=null && arrWallet!=null) {
            if (list.size() > arrWallet.size()) {
                arrWallet.clear();
                for(ModelHistoryTransaction item : list)
                {
                    arrWallet.add(item);
                }
                adapter.updateList(arrWallet);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_tab);
        fab = (Button)this.findViewById(R.id.btnSendCoins) ;

        args =   getIntent().getExtras();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle arg = new Bundle();
                if(args.getInt("namecoin") == 0) {
                    arg.putString("nameCoin", "Ethereum");
                }
                else
                {
                    arg.putString("nameCoin", "Proteusion");
                }
                Intent intent =  new Intent(TransactionTab.this, Send_Ethereum_Activity.class);
                intent.putExtras(arg);
                startActivity(intent);
            }
        });

    }

    private class UpdateUITasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            wallet.updateCoinInfo();
            balance = balanceRepo.getWalletInfo(balance);
            return null;
        }

        @Override
        protected void onPostExecute(Void arrs) {
            wallet.setETHBalance(balance.getBalance());
            wallet.setPROBalance(balance.getTokenBalance());

            if(args.getInt("namecoin") == 0){

                EthereumNumber.setText(wallet.getETHBalance().toString() );
                EthereumValue.setText( String.format("%.4f",wallet.getETHPrice()) + " USD");
            }
            else {
                EthereumNumber.setText(wallet.getPROBalance().toString());
                EthereumValue.setText(String.format( "%.4f",wallet.getPROPrice())  + " USD");
            }

            if(updateUITask != null) {
                updateUITask.cancel(true);
                updateUITask = null;
            }
        }
    }

    public String getUrlRespondHistoryEthereum(String addressHex, long latest) {
        String url = String.format(ETHEREUM_GET, addressHex, String.valueOf(latest));
        return url;
    }
    public String getETHEREUM_GET_BALANCE(String addressHex) {
        String url = String.format(ETHEREUM_GET_BALANCE,addressHex);
        return url;
    }

    private String getTimeAgo(long blockTime){
        Date now = new Date();

        if (now.getTime() > blockTime) {
            long time = now.getTime() - blockTime;
            if (time < 60*1000*60) {
                if (TimeUnit.MILLISECONDS.toMinutes(time) > 1) {
                    return TimeUnit.MILLISECONDS.toMinutes(time) + " minutes ago";
                } else {
                    return TimeUnit.MILLISECONDS.toMinutes(time) + " minute ago";
                }
            } else if(time <= dayToMiliseconds(1)){
                if (TimeUnit.MILLISECONDS.toHours(time) > 1) {
                    return TimeUnit.MILLISECONDS.toHours(time) + " hours ago";
                } else {
                    return TimeUnit.MILLISECONDS.toHours(time) + " hour ago";
                }
            }else{
                return getDateCurrentTimeZone(blockTime);
            }
        } else {
            return getDateCurrentTimeZone(blockTime);
        }
    }

    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            DateFormat sdf = new SimpleDateFormat("MM dd, yyyy");
            Date netDate = (new Date(timestamp));
            return sdf.format(netDate);
        }catch (Exception e) {
        }
        return "";
    }

    private Long dayToMiliseconds(int days){
        Long result = Long.valueOf(days * 24 * 60 * 60 * 1000);
        return result;
    }

    private class ReceiveTransactionList extends AsyncTask<List<ModelHistoryTransaction>, Void, List<ModelHistoryTransaction>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarWaiting.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<ModelHistoryTransaction> doInBackground(List<ModelHistoryTransaction>... params) {

            List<ModelHistoryTransaction> arr = new ArrayList<>();
            HttpURLConnection urlConnection = null;
            try{

                URL url;
                if(args.getInt("namecoin") == 0)
                {
                    url = new URL(getUrlRespondHistoryEthereum(hexAddress, wallet.getCurrentBlock()));
                }
                else
                {
                    url = new URL(getUrlRespondHistoryEthereum(PRDToken.contractAddress, wallet.getCurrentBlock()));
                }
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(19000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(true);

                int resp = urlConnection.getResponseCode();

                switch (resp) {
                    case HttpURLConnection.HTTP_OK:
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        in.close();

                        JSONObject object = new JSONObject(result.toString());
                        JSONArray jsonArray = object.getJSONArray("result");
                        if(args.getInt("namecoin") == 0)
                        {
                            if (jsonArray != null) if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    try {

                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String form = jsonObject.getString("from");
                                        String to = jsonObject.getString("to");
                                        String weivalue = jsonObject.getString("value");
                                        if (!weivalue.equalsIgnoreCase("0")) {
                                            ModelHistoryTransaction model = new ModelHistoryTransaction();
                                            if (hexAddress.equalsIgnoreCase(form)) {
                                                model.setTitle("Sent");
                                                model.setAddress(jsonObject.getString("to"));
                                             //   model.setIcon(getResources().getDrawable(R.drawable.ic_send_pink));
                                                model.setNumber(WalletInfo.convertWeiToEther(jsonObject.getString("value")));
                                                model.setConfirmNumber(jsonObject.getInt("confirmations"));
                                                model.setDate(new Date(jsonObject.getLong("timeStamp") * 1000));
                                                model.setTXHash(jsonObject.getString("hash"));
                                            }
                                            if (hexAddress.equalsIgnoreCase(to)) {
                                                model.setTitle("Received");
                                                model.setAddress(jsonObject.getString("from"));
                                                //model.setIcon(getResources().getDrawable(R.drawable.ic_receive_pink));
                                                model.setNumber(WalletInfo.convertWeiToEther(jsonObject.getString("value")));
                                                model.setConfirmNumber(jsonObject.getInt("confirmations"));
                                                model.setDate(new Date(jsonObject.getLong("timeStamp") * 1000));
                                                model.setTXHash(jsonObject.getString("hash"));
                                            }
                                            try {
                                                model.setTime(getTimeAgo(model.getDate().getTime()));
                                            } catch (Exception exx) {
                                                model.setTime("");
                                            }
                                            arr.add(model);
                                        }
                                    } catch (Exception px) {
                                        px.getLocalizedMessage();
                                    }
                                }
                                // return arr;
                            }
                        }
                        else
                        {
                            if (jsonArray!=null) if (jsonArray.length() > 0){
                                for (int i=0;i<jsonArray.length();i++){
                                    try {
                                        ModelHistoryTransaction model = new ModelHistoryTransaction();
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String form = jsonObject.getString("from");
                                        String to = jsonObject.getString("to");
                                        String inputValue = jsonObject.getString("input");
                                        PRDToken.TransferResponse transferResponse = PRDToken.getInputData(inputValue);
                                        String jsonAddress = transferResponse.to.getHex();
                                        String jsonValue = transferResponse.value.string();
                                        if (hexAddress.equalsIgnoreCase(form)) {
                                            model.setTitle("Sent");
                                            model.setAddress(jsonAddress);
                                          //  model.setIcon(getResources().getDrawable(R.drawable.ic_send_pink));
                                            model.setNumber(jsonValue);
                                            model.setConfirmNumber(jsonObject.getInt("confirmations"));
                                            model.setDate(new Date(jsonObject.getLong("timeStamp")*1000));
                                            model.setTXHash(jsonObject.getString("hash"));
                                            try {
                                                model.setTime(getTimeAgo(model.getDate().getTime()));
                                            }catch (Exception exx){
                                                model.setTime("");
                                            }
                                            arr.add(model);
                                        }
                                        if (hexAddress.equalsIgnoreCase(jsonAddress)) {
                                            model.setTitle("Received");
                                            model.setAddress(jsonObject.getString("from"));
                                           // model.setIcon(getResources().getDrawable(R.drawable.ic_receive_pink));
                                            model.setNumber(jsonValue);
                                            model.setConfirmNumber(jsonObject.getInt("confirmations"));
                                            model.setDate(new Date(jsonObject.getLong("timeStamp")*1000));
                                            model.setTXHash(jsonObject.getString("hash"));
                                            try {
                                                model.setTime(getTimeAgo(model.getDate().getTime()));
                                            }catch (Exception exx){
                                                model.setTime("");
                                            }
                                            arr.add(model);
                                        }
                                    }catch (Exception px) { px.getLocalizedMessage(); }
                                }
                            }
                        }


                        break;
                    default:
                        // return arr;
                        break;
                }

            }catch (Exception e){}
            finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return arr;
        }

        @Override
        protected void onPostExecute(List<ModelHistoryTransaction> list) {
            if(list.size() > 0) {
                SetListTransaction(list);
            }
            if(historytask != null) {
                historytask.cancel(true);
                historytask = null;
            }
            progressBarWaiting.setVisibility(View.GONE);
        }
    }

}
