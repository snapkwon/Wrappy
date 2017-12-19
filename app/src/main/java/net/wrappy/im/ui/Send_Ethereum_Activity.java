package net.wrappy.im.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.db.Balance;
import net.wrappy.im.GethService.db.BalanceRepo;
import net.wrappy.im.R;
import net.wrappy.im.util.PopupUtils;

import org.ethereum.geth.BigInt;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import me.ydcool.lib.qrmodule.activity.QrScannerActivity;

public class Send_Ethereum_Activity extends AppCompatActivity {

    public static final String SEND_STATUS = "SEND_STATUS";
    public static final String SEND_ACTION = "SEND_ACTION";
    public static final String AMOUNT = "AMOUNT";
    public static final String ADDRESS = "ADDRESS";
    public static final String PASSWORD = "PASSWORD";
    public static final String STATUS = "STATUS";
    public static final String TYPE_COIN = "TYPE_COIN";
    public static final String COMMENT = "COMMENT";

    private TextView txtAvalaibleBalance;
    private EditText edtAddressETH;
    private ImageButton btWalletAddress;
    private EditText edtETHAmount;
    private EditText edtUSDAmount;
    private Button btETH;
    private Button btUSD;
    private EditText edtDescription;
    private Button btSend;
    private ProgressDialog waiting;
    private TextView txtBalance;
    private String password;
    private String amount;
    private String address;
    private String des;
    private Double valueBalance;
    private ImageButton send_action_back;
    private TextView send_action_header;
    String nameCoin;
    String hexAddress;


    final Handler myHandler = new Handler();
    final Timer myTimer = new Timer();
    TimerTask myTask;
    WalletInfo wallet;

    BalanceRepo balanceRepo;
    Balance balance;


    private LocalBroadcastManager mLocalBroadcastManager;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SEND_STATUS)) {
                String status = intent.getExtras().getString(STATUS);
                if (!isFinishing()) {
                    if (!status.isEmpty()) {
                        PopupUtils.showCustomDialog(Send_Ethereum_Activity.this, "", status, R.string.cancel, null);
                    }
                }

            }
        }
    };

    public Double convertPROToUSD(Double pro) {
        try {
            // String ethRate = coinInfo.getString("price_usd");
            return pro * wallet.getPRORate();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    public Double convertUSDToETH(Double usd) {
        try {
            String ethRate = wallet.coinInfo.getString("price_usd");
            return usd / Double.parseDouble(ethRate);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    public Double convertETHToUSD(Double eth) {
        try {
            String ethRate = wallet.coinInfo.getString("price_usd");
            return eth * Double.parseDouble(ethRate);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0.00;

    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    public BigInt convertEtherToWei(float eth) {
        String t = "1000000000000000000";
        while (eth < 1) {
            eth = eth * 10;
            t = removeLastChar(t);
        }

        BigInt amount = new BigInt(Long.parseLong(String.valueOf(Math.round(eth))) * Long.parseLong(t));
        return amount;
    }

    private static final DecimalFormat decimalFormatter = new DecimalFormat("0.00##", new DecimalFormatSymbols(Locale.US));

    public Double getETHBalance(Double eth) {
        try {
            BigDecimal balanceForAccount = new BigDecimal(eth);
            balanceForAccount = balanceForAccount.divide(new BigDecimal("1000000000000000000"));
            return Double.parseDouble(decimalFormatter.format(balanceForAccount));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.00;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        password = "";
        valueBalance = 0.0;

        setContentView(R.layout.activity_send__ethereum);

        ActionBar actionbar = getSupportActionBar();

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_action_arrow_back);
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        //actionBar.setDisplayHomeAsUpEnabled(true);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(SEND_STATUS);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);


        nameCoin = getIntent().getStringExtra("nameCoin");

        if (nameCoin.equals("Ethereum")) {
            if (actionbar != null) {
                actionbar.setTitle(getResources().getString(R.string.ETH));
            }
        } else if (nameCoin.equals("Proteusion")) {
            if (actionbar != null) {
                actionbar.setTitle(getResources().getString(R.string.PRO));
            }
        }

        try {
            KeyManager keyManager = KeyManager.newKeyManager(getApplicationContext().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
            hexAddress = keyManager.getAccounts().get(0).getAddress().getHex();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // wallet = Wallet.getInstance(getFilesDir(), getAssets());
        wallet = new WalletInfo();

        balanceRepo = new BalanceRepo();
        balance = new Balance();
        balance.setChain("4");
        balance.setAddress(hexAddress);
        balance.setTokenAddress(PRDToken.contractAddress);
        balance = balanceRepo.getWalletInfo(balance);

        txtAvalaibleBalance = (TextView) this.findViewById(net.wrappy.im.R.id.textbalance);
        edtAddressETH = (EditText) this.findViewById(net.wrappy.im.R.id.editETHadress);
        btWalletAddress = (ImageButton) this.findViewById(net.wrappy.im.R.id.buttonWalletAddress);
        edtETHAmount = (EditText) this.findViewById(net.wrappy.im.R.id.editETHamount);
        edtUSDAmount = (EditText) this.findViewById(net.wrappy.im.R.id.editUSDamount);
        btETH = (Button) this.findViewById(net.wrappy.im.R.id.buttonETH);
        btUSD = (Button) this.findViewById(net.wrappy.im.R.id.buttonUSD);
        edtDescription = (EditText) this.findViewById(net.wrappy.im.R.id.editdescription);
        btSend = (Button) this.findViewById(net.wrappy.im.R.id.buttonSend);
        txtBalance = (TextView) this.findViewById(net.wrappy.im.R.id.textbalancesendethereum);

        edtAddressETH.setFocusableInTouchMode(true);
        edtAddressETH.setFocusable(true);

        if (nameCoin.equals("Ethereum")) {
            valueBalance = Double.parseDouble(balance.getBalance());
            txtAvalaibleBalance.setText(String.format("%.4f", getETHBalance(valueBalance)) + " ETH");
            btETH.setText("ETH");
            edtETHAmount.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_DECIMAL |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);

        } else if (nameCoin.equals("Proteusion")) {
            valueBalance = Double.parseDouble(balance.getTokenBalance());
            txtAvalaibleBalance.setText(String.valueOf(valueBalance) + " PRO");
            btETH.setText("PRO");
            edtETHAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        }


        btWalletAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Send_Ethereum_Activity.this,List_Wallet_Address_Activity.class);
//                startActivity(intent);
                Intent intent = new Intent(Send_Ethereum_Activity.this, QrScannerActivity.class);
                startActivityForResult(intent, QrScannerActivity.QR_REQUEST_CODE);
            }
        });

        edtETHAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (edtETHAmount.hasFocus()) {
                        if (nameCoin.equals("Ethereum")) {
                            edtUSDAmount.setText(String.valueOf(convertETHToUSD(Double.valueOf(edtETHAmount.getText().toString()))));
                        } else if (nameCoin.equals("Proteusion")) {
                            edtUSDAmount.setText(String.valueOf(convertPROToUSD(Double.valueOf(edtETHAmount.getText().toString()))));
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

     /*   edtUSDAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (edtUSDAmount.hasFocus()) {
                        edtETHAmount.setText(String.valueOf(wallet.convertUSDToETH(Double.valueOf(edtUSDAmount.getText().toString()))));
                    }
                }
                catch (Exception e)
                {

                }
            }
        });*/

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Send_Ethereum_Activity.this);
                LayoutInflater inflater = Send_Ethereum_Activity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_with_edittext, null);
                dialogBuilder.setView(dialogView);

                final EditText edt = (EditText) dialogView.findViewById(R.id.etinputpass);
                edt.setHint(Html.fromHtml("<small><i>" + "Input Password" + "</i></small>"));

                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // if(!edt.getText().toString().isEmpty()) {
                        password = edt.getText().toString();
                        amount = edtETHAmount.getText().toString();
                        address = edtAddressETH.getText().toString();
                        des = edtDescription.getText().toString();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
                        waiting = new ProgressDialog(Send_Ethereum_Activity.this);
                        waiting.setMessage("Loading");
                        waiting.setCancelable(true);
                        waiting.show();
                        String status = "";
                        try {
                            if (!amount.isEmpty()) {
                                if (Double.parseDouble(amount) >= 0) {
                                    if (nameCoin.equals("Ethereum")) {
                                        //status = wallet.sendETH(wallet.convertEtherToWei(Float.parseFloat(amount)), password, nonce, address,des);
                                        Intent i = new Intent(SEND_ACTION);
                                        i.putExtra(TYPE_COIN, nameCoin);
                                        i.putExtra(AMOUNT, Long.valueOf(convertEtherToWei(Float.parseFloat(amount)).toString()));
                                        i.putExtra(PASSWORD, password);
                                        i.putExtra(ADDRESS, address);
                                        i.putExtra(COMMENT, des);
                                        mLocalBroadcastManager.sendBroadcast(i);
                                    } else if (nameCoin.equals("Proteusion")) {
                                        if (true) {
                                            Intent i = new Intent(SEND_ACTION);
                                            i.putExtra(TYPE_COIN, nameCoin);
                                            i.putExtra(AMOUNT, Long.valueOf(amount));
                                            i.putExtra(PASSWORD, password);
                                            i.putExtra(ADDRESS, address);
                                            mLocalBroadcastManager.sendBroadcast(i);
                                        } else {
                                            status = "Amount to send is low.";
                                        }
                                    }

                                } else {
                                    status = "Amount to send is low.";
                                }
                            } else {
                                status = "Amount to send is null";
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!status.isEmpty()) {
                            PopupUtils.showCustomDialog(Send_Ethereum_Activity.this, "", status, R.string.cancel, null);
                        }

                        waiting.cancel();

                    }
                });

                dialogBuilder.setNegativeButton("Cancel", null);
                AlertDialog b = dialogBuilder.create();
                b.show();
                edt.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


            }
        });


    }

    final Runnable myRunnable = new Runnable() {
        public void run() {

            if (Send_Ethereum_Activity.this == null) {
                return;
            }
            try {
                if (nameCoin.equals("Ethereum")) {
                    if (wallet.getETHBalance() > 0 || valueBalance != 0) {
                        Double range = 0.0;
                        range = valueBalance - wallet.getETHBalance();
                        if (range < 0) {
                            valueBalance = wallet.getETHBalance();
                            PopupUtils.showCustomDialog(Send_Ethereum_Activity.this, "", "you received " + String.format("%.4f", Math.abs(range)) + " ETH", R.string.cancel, null);
                            txtAvalaibleBalance.setText(String.valueOf(valueBalance) + " ETH");
                        } else if (range > 0) {
                            valueBalance = wallet.getETHBalance();
                            PopupUtils.showCustomDialog(Send_Ethereum_Activity.this, "", "you sent " + String.format("%.4f", Math.abs(range)) + " ETH", R.string.cancel, null);
                            txtAvalaibleBalance.setText(String.valueOf(valueBalance) + " ETH");
                        }

                    }
                } else if (nameCoin.equals("Proteusion")) {
                    if (wallet.getPROBalance() > 0 || valueBalance != 0) {
                        Double range = 0.0;
                        range = valueBalance - wallet.getPROBalance();
                        if (range < 0) {
                            valueBalance = wallet.getPROBalance();
                            PopupUtils.showCustomDialog(Send_Ethereum_Activity.this, "", "you received " + String.format("%.4f", Math.abs(range)) + " PRO", R.string.cancel, null);
                            txtAvalaibleBalance.setText(String.valueOf(valueBalance) + " PRO");
                        } else if (range > 0) {
                            valueBalance = wallet.getPROBalance();
                            PopupUtils.showCustomDialog(Send_Ethereum_Activity.this, "", "you sent " + String.format("%.4f", Math.abs(range)) + " PRO", R.string.cancel, null);
                            txtAvalaibleBalance.setText(String.valueOf(valueBalance) + " PRO");
                        }

                    }
                }
            } catch (Exception e) {
                //show nothing
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QrScannerActivity.QR_REQUEST_CODE) {
            try {
                edtAddressETH.setText(data.getExtras().getString(QrScannerActivity.QR_RESULT_STR));
            } catch (Exception ex) {
            }
        }
    }

    // updateUI method related to a Runnable
    private void updateUI() {
        myHandler.post(myRunnable); // relate this to a Runnable
    }

    /*private class SendCurrencyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute()
        {

           waiting = new ProgressDialog(Send_Ethereum_Activity.this);
            waiting.setMessage(LocaleController.getString("Loading", R.string.Loading));
            waiting.setCancelable(true);
            waiting.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String status = "";
            try {
                if(!amount.isEmpty()) {
                    if(Double.parseDouble(amount)>=0) {
                        long nonce = wallet.getNonce();
                        if(nameCoin.equals("Ethereum")) {
                            status = wallet.sendETH(wallet.convertEtherToWei(Float.parseFloat(amount)), password, nonce, address,des);
                        }
                        else if(nameCoin.equals("Proteusion"))
                        {
                            if(Double.parseDouble(amount) <= wallet.getPRDBalance(0)) {
                                status = wallet.sendPRD(address, Long.parseLong(amount), password);
                            }
                            else
                            {
                                status = "Amount to send is low.";
                            }
                        }

                    }
                    else
                    {
                        status = "Amount to send is low.";
                    }
                }
                else
                {
                    status = "Amount to send is null";
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            return status;
        }

        @Override
        protected void onPostExecute(String status) {

          //  waiting.cancel();
            if(!status.isEmpty())
            {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Send_Ethereum_Activity.this);
                LayoutInflater inflater = Send_Ethereum_Activity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
                dialogBuilder.setView(dialogView);

                final TextView tvTitle = (TextView) dialogView.findViewById(R.id.texttitlealert);
                tvTitle.setText(status);

                dialogBuilder.setNegativeButton("Cancel", null);
                AlertDialog b = dialogBuilder.create();
                b.show();

            }


        }
    }
*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myTimer != null) {
            myTimer.cancel();
            myTimer.purge();
        }
        if (myTask != null) {
            myTask.cancel();
        }
        //  myHandler.removeCallbacks(myRunnable);

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
