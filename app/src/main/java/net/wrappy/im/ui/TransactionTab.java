package net.wrappy.im.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.db.Balance;
import net.wrappy.im.GethService.db.BalanceRepo;
import net.wrappy.im.R;
import net.wrappy.im.adapter.RecycleHistoryAdapter;
import net.wrappy.im.helper.layout.LayoutHelper;
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

import static android.widget.RelativeLayout.CENTER_VERTICAL;
import static android.widget.RelativeLayout.TRUE;

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
    private ImageButton fab;
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

//                txtPeer.setText("Peers: " + wallet.getPeerCount());
                double ratio = wallet.getCurrentBlock() / (double) wallet.getHighestBlock();
                DecimalFormat percentFormat= new DecimalFormat("#.##%");
             //   txtBlock.setText("| Blocks: " + wallet.getCurrentBlock() + " (" + percentFormat.format(ratio) + ")");
            }

            if (intent.getAction().equals(TRANSACTION_TAB_FINISHED)) {
                long peers =  intent.getExtras().getLong(PEER_COUNT);
                long currentBlock = intent.getExtras().getLong(CURRENT_BLOCK);

                wallet.setCurrentBlock(currentBlock);
                wallet.setPeerCount(peers);

              //  txtPeer.setText("Peers: " + wallet.getPeerCount());
               // txtBlock.setText("| Blocks: " + wallet.getCurrentBlock() + " (100%)");

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
        args =   getIntent().getExtras();

        viewMain = new FrameLayout(this);
        viewMain.setBackgroundColor(0xffffffff);
        viewMain.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(viewMain);

        TextView viewBarContentHeader = new TextView(this);
        viewBarContentHeader.setPadding(0,LayoutHelper.dp(5),0,0);
        if(args.getInt("namecoin") == 0)
        {
            viewBarContentHeader.setText("Ethereum");
        }
        else
        {
            viewBarContentHeader.setText("Proteusion Coin");
        }
        viewBarContentHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        viewBarContentHeader.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout viewBarContentDetail = new LinearLayout(this);
        viewBarContentDetail.setOrientation(LinearLayout.HORIZONTAL);
        viewBarContentDetail.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
     /*   txtPeer = new TextView(this);
        txtPeer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        txtPeer.setTextColor(getResources().getColor(R.color.color_text_2));
        txtPeer.setText("Peers: 0" );
        txtPeer.setTextFont(AppFonts.FONT_LIGHT);
        txtPeer.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        viewBarContentDetail.addView(txtPeer);
        txtBlock = new FontTextView(this);
        txtBlock.setTextColor(getResources().getColor(R.color.color_text_2));
        txtBlock.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        txtBlock.setText("| Blocks: 0");
        txtBlock.setTextFont(AppFonts.FONT_LIGHT);
        txtBlock.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        txtBlock.setPadding(LayoutHelper.dp(10),0,0,0);
        viewBarContentDetail.addView(txtBlock);*/

        MainLayout = new FrameLayout(this);
        MainLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        MainLayout.setPadding(0,LayoutHelper.dp(56),0,0);
        viewMain.addView(MainLayout);

        LinearLayout mainLayoutContainer = new LinearLayout(this);
        mainLayoutContainer.setOrientation(LinearLayout.VERTICAL);
        mainLayoutContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        MainLayout.addView(mainLayoutContainer);

        LinearLayout linearLayoutV = new LinearLayout(this);
        linearLayoutV.setOrientation(LinearLayout.VERTICAL);

        fab =new ImageButton(this);
        fab.setBackground(getResources().getDrawable(R.drawable.ic_launcher));

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

        MainLayout.addView(fab, LayoutHelper.createFrame(LayoutHelper.dp(150), LayoutHelper.dp(150), Gravity.RIGHT|Gravity.BOTTOM,0,0,LayoutHelper.dp(2),LayoutHelper.dp(2)));
        //    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2f);


        FrameLayout headerContent = new FrameLayout(this);
        FrameLayout Frameheader = new FrameLayout(this);
        mainLayoutContainer.addView(Frameheader);
        //headerContent.setBackground(getResources().getDrawable(R.drawable.bg_header_wallet));
        //   headerContent.setLayoutParams(params1);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        ImageView icon = new ImageView(this);
        if(args.getInt("namecoin") == 0)
        {
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        }
        else
        {
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        }

        LinearLayout Leftcontent = new LinearLayout(this);
        Leftcontent.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(LayoutHelper.dp(20),LayoutHelper.dp(20),LayoutHelper.dp(20),LayoutHelper.dp(20));
        Bundle b = getIntent().getExtras();
        // String ETHnumber = b.getString("ETHnumber");
        //String ETHvalue = b.getString("ETHvalue");
        // ETHrate = b.getDouble("ETHrate");

        EthereumNumber= new TextView(this);
        EthereumNumber.setText("0.0");
        EthereumNumber.setTextSize(45);

        EthereumValue= new TextView(this);
        EthereumValue.setTextSize(15);
        EthereumValue.setText("0 USD");

        if(args.getInt("namecoin") == 0)
        {
        //    EthereumNumber.setTextColor(getResources().getColor(R.color.textethereum));
          //  EthereumValue.setTextColor(getResources().getColor(R.color.textethereum));
        }
        else
        {
           // EthereumNumber.setTextColor(getResources().getColor(R.color.textproteusion));
           // EthereumValue.setTextColor(getResources().getColor(R.color.textproteusion));
        }

        Frameheader.addView(headerContent , LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

        headerContent.addView(content , LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        content.addView(icon , LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP));
        content.addView(Leftcontent , LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP));

        Leftcontent.addView(EthereumNumber , LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP));
        Leftcontent.addView(EthereumValue , LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP));


        //  LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2.8f);
        RelativeLayout SearchContent = new RelativeLayout(this);
        mainLayoutContainer.addView(SearchContent);
        LinearLayout historyLayout = new LinearLayout(this);
        historyLayout.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams historyParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        historyParam.addRule(CENTER_VERTICAL,RelativeLayout.TRUE);
        historyLayout.setLayoutParams(historyParam);
        SearchContent.addView(historyLayout);
        SearchType= new TextView(this);
      //  SearchType.setTextColor(getResources().getColor(R.color.textethereum));
        SearchType.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        SearchType.setText("HISTORY");
        SearchType.setPadding(LayoutHelper.dp(10),LayoutHelper.dp(5),0,LayoutHelper.dp(5));

      //  SearchContent.setBackgroundColor(getResources().getColor(R.color.drowdowncolor));
        SearchContent.setPadding(LayoutHelper.dp(5),0,LayoutHelper.dp(5),0);
        LinearLayout.LayoutParams historyFontTest = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        historyParam.addRule(CENTER_VERTICAL,RelativeLayout.TRUE);
        historyFontTest.gravity = Gravity.CENTER_VERTICAL;
        SearchType.setLayoutParams(historyFontTest);
        historyLayout.addView(SearchType);

        progressBarWaiting = new ProgressBar(this);
        //progressBarWaiting.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.textethereum), android.graphics.PorterDuff.Mode.MULTIPLY);
        RelativeLayout.LayoutParams paramsProgess = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsProgess.leftMargin = LayoutHelper.dp(10);
        progressBarWaiting.setLayoutParams(paramsProgess);
        historyLayout.addView(progressBarWaiting);

        final LinearLayout btnAcitivities = new LinearLayout(this);
        btnAcitivities.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_VERTICAL,RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_END,TRUE);
        btnAcitivities.setLayoutParams(params);
        TextView btnActivitiesTitle = new TextView(this);
        btnActivitiesTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btnActivitiesTitle.setText("Activity");
        btnActivitiesTitle.setPadding(0,LayoutHelper.dp(5),0,LayoutHelper.dp(5));
   //     btnActivitiesTitle.setTextColor(getResources().getColor(R.color.textethereum));
        btnActivitiesTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        btnAcitivities.addView(btnActivitiesTitle);
        ImageButton icDown = new ImageButton(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        icDown.setLayoutParams(layoutParams);
     //   icDown.setImageResource(R.drawable.ic_drowdown);
        icDown.setBackgroundColor(Color.TRANSPARENT);
        btnAcitivities.addView(icDown);

        btnAcitivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(TransactionTab.this,btnAcitivities);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_wallet_activities, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId()==R.id.wallet_sent) {

                            if (arrWallet!=null) if (arrWallet.size() > 0) {
                                List<ModelHistoryTransaction> modelFiler = new ArrayList<ModelHistoryTransaction>();
                                for (int i=0; i < arrWallet.size(); i++) {
                                    ModelHistoryTransaction transaction = arrWallet.get(i);
                                    if (transaction.getTitle().equalsIgnoreCase("Sent")) {
                                        modelFiler.add(transaction);
                                    }
                                }
                                adapter.updateList(modelFiler);
                            }
                        }
                        if (item.getItemId()==R.id.wallet_receiver) {
                            if (arrWallet!=null) if (arrWallet.size() > 0) {
                                List<ModelHistoryTransaction> modelFiler = new ArrayList<ModelHistoryTransaction>();
                                for (int i=0; i < arrWallet.size(); i++) {
                                    ModelHistoryTransaction transaction = arrWallet.get(i);
                                    if (!transaction.getTitle().equalsIgnoreCase("Sent")) {
                                        modelFiler.add(transaction);
                                    }
                                }
                                adapter.updateList(modelFiler);
                            }
                        }
                        if (item.getItemId() == R.id.wallet_all) {
                            adapter.updateList(arrWallet);
                        }

                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        SearchContent.addView(btnAcitivities);
        // LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
        Listview = new RecyclerView(this);
        //  Listview.setLayoutParams(params3);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);

        arrWallet = new ArrayList<ModelHistoryTransaction>();

        Listview.setLayoutManager(layoutManager);
        if(args.getInt("namecoin") == 0)
        {
            adapter=new RecycleHistoryAdapter(arrWallet, "ETH");
        }
        else
        {
            adapter=new RecycleHistoryAdapter(arrWallet, "PRO");
        }

    //    Listview.addItemDecoration(
              //  new DividerItemDecoration(3));
        Listview.setAdapter(adapter);
        linearLayoutV.addView(Listview ,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT,Gravity.TOP ));

        mainLayoutContainer.addView(linearLayoutV,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT) );

        adapter.setOnItemClickListener(new RecycleHistoryAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                Bundle bundle = new Bundle();
                bundle.putString("type",arrWallet.get(position).getTitle());
                if(args.getInt("namecoin") == 1)
                {
                    bundle.putString("coin","prd");
                }
                bundle.putString("ethereumnumber", arrWallet.get(position).getNumber());
                try {
                    SimpleDateFormat formatDate = new SimpleDateFormat("MM/dd/yyyy HH:mm a");
                    bundle.putString("time", formatDate.format(arrWallet.get(position).getDate())
                            + " (" + arrWallet.get(position).getTime() + ")");
                }catch(Exception e){
                    bundle.putString("time", "");
                }
            /*    bundle.putLong("confirmNumber",arrWallet.get(position).getConfirmNumber());
                bundle.putString("toUSD",arrWallet.get(position).getToUSD());
                bundle.putString("txHash",arrWallet.get(position).getTXHash());
                Intent intent = new Intent(TransactionTab.this, TransactionDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);*/
            }

        });

        try {
            KeyManager keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
            hexAddress = keyManager.getAccounts().get(0).getAddress().getHex();
        }catch(Exception e){
            e.printStackTrace();
        }
        balanceRepo = new BalanceRepo();
        balance = new Balance();
        balance.setChain("4");
        balance.setAddress(hexAddress);
        balance.setTokenAddress(PRDToken.contractAddress);
        balance = balanceRepo.getWalletInfo(balance);
      //  txtBlock.setText("| Blocks: " + balance.getBlock() + " (loading...)");

        wallet = new WalletInfo();
        wallet.setETHAddress(hexAddress);
        wallet.setCurrentBlock(balance.getBlock());

        updateUITask = new UpdateUITasks();
        updateUITask.execute();

        historytask = new ReceiveTransactionList();
        historytask.execute();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(TRANSACTION_TAB);
        mIntentFilter.addAction(TRANSACTION_TAB_FINISHED);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
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
