package net.wrappy.im.GethService;

/**
 * Created by sonntht on 21/10/2017.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.model.SyncData;
import net.wrappy.im.GethService.thread.GethDownloaderTask;
import net.wrappy.im.ui.Send_Ethereum_Activity;
import net.wrappy.im.ui.TransactionTab;
import net.wrappy.im.ui.WalletFragment;

import org.ethereum.geth.Address;
import org.ethereum.geth.BigInt;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.KeyStore;
import org.ethereum.geth.Signer;
import org.ethereum.geth.TransactOpts;
import org.ethereum.geth.Transaction;


public class GethLightService extends Service implements GethDownloaderTask.ThreadCallback{


    private final static int GAS_PRICE = 21001;
    private final static int GAS_PRICE_WITH_TEXT = 121001;
    private final static int GAS_PRICE_PRO = 121001;
    private static int NETWORK_ID = 4;

    private String datadir;
    private GethLight eth;
    private Context context;

    private static final String TAG = GethLightService.class.getSimpleName();

    private LocalBroadcastManager mLocalBroadcastManager;
    private Thread mGethDownloaderThread;
    private GethDownloaderTask GethDownloaderTask;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        // Get the localBroadcastManager instance, so that it can communicate with the fragment
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Send_Ethereum_Activity.SEND_ACTION);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        context = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        GethDownloaderTask = new GethDownloaderTask(this, context);
        mGethDownloaderThread = new Thread(GethDownloaderTask);

        // Request the geth to start the download
        mGethDownloaderThread.start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");

        // We should here clean up everything we used.
        super.onDestroy();
    }

    @Override
    public void taskStarted() {
        Intent i = new Intent(WalletFragment.MAIN_WALLET_STARTED);
        mLocalBroadcastManager.sendBroadcast(i);
    }

    @Override
    public void taskUpdateWalletInfo(SyncData syncData) {
        Intent i = new Intent(WalletFragment.MAIN_WALLET_IN_PROGRESS);
        mLocalBroadcastManager.sendBroadcast(i);
    }

    @Override
    public void taskProgressingStatus(SyncData syncData) {
        Intent i = new Intent(TransactionTab.TRANSACTION_TAB);
        i.putExtra(TransactionTab.PEER_COUNT, syncData.getPeers());
        i.putExtra(TransactionTab.CURRENT_BLOCK, syncData.getCurrentBlock());
        i.putExtra(TransactionTab.HIGHEST_BLOCK, syncData.getHighestBlock());
        mLocalBroadcastManager.sendBroadcast(i);
    }


    public String sendETH(EthereumClient ethereumClient, org.ethereum.geth.Context context, BigInt amount, final String passphrase , String address, String des  , KeyStore account){
        String status = "";
        try {
            byte[] theByteArray = des.getBytes();
            Transaction tx = new Transaction(
                    ethereumClient.getPendingNonceAt(context,account.getAccounts().get(0).getAddress()),
                    new Address(address),
                    amount,
                    new BigInt(des.isEmpty()?GAS_PRICE:GAS_PRICE_WITH_TEXT),
                    ethereumClient.suggestGasPrice(context),
                    theByteArray); // Random empty transaction
            BigInt chain = new BigInt(NETWORK_ID);

            // Sign a transaction with a single authorization
            Transaction signed = account.signTxPassphrase(account.getAccounts().get(0), passphrase, tx, chain);


            ethereumClient.sendTransaction(context, signed);
            status =  "Submitted transaction successfully " ;

        } catch(Exception e){
            e.printStackTrace();
            status = e.getMessage();
        }
        return status;
    }

    public String sendPRD(String address , BigInt value , final String passphase , final KeyStore account, EthereumClient eth , org.ethereum.geth.Context ctx, PRDToken pro){
        String status = "";
        Transaction transaction ;
        try {

            Signer mySigner = new Signer() {
                @Override
                public Transaction sign(Address address, Transaction transaction) throws Exception {
                   // return account.signTxPrivateKey("eaae8aca6b7d2750b4b0a5654e197e550e468b5869e644307b9f7fdc7af2d617",transaction,new BigInt(NETWORK_ID));
                    return account.signTxPassphrase(account.getAccounts().get(0), passphase , transaction,new BigInt(4));
                }
            };

            TransactOpts auth  = new TransactOpts();
            auth.setFrom( account.getAccounts().get(0).getAddress());
            auth.setContext(ctx);
            auth.setValue(new BigInt(0));
            auth.setGasLimit(GAS_PRICE_PRO);
            auth.setSigner(mySigner);
            auth.setGasPrice(eth.suggestGasPrice(ctx));
            auth.setNonce( eth.getPendingNonceAt(ctx,account.getAccounts().get(0).getAddress()));

            transaction = pro.transfer(auth, new Address(address),value);
            status = "Submitted transaction successfully";
        } catch (Exception e) {
            e.printStackTrace();
            status = e.getMessage();
        }

        return status;
    }

    private final BroadcastReceiver mBroadcastReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Send_Ethereum_Activity.SEND_ACTION)) {
                String namecoin = intent.getExtras().getString(Send_Ethereum_Activity.TYPE_COIN);
                long amount = intent.getExtras().getLong(Send_Ethereum_Activity.AMOUNT);
                String passw = intent.getExtras().getString(Send_Ethereum_Activity.PASSWORD);
                String address = intent.getExtras().getString(Send_Ethereum_Activity.ADDRESS);
                EthereumClient eth = GethDownloaderTask.get_eth();
                KeyManager key = GethDownloaderTask.get_account();
                org.ethereum.geth.Context ctx = GethDownloaderTask.get_context();
                String status="";
                if(namecoin.equals("Ethereum")) {
                    String comment = intent.getExtras().getString(Send_Ethereum_Activity.COMMENT);
                    status = sendETH(eth, ctx, new BigInt(amount), passw, address, comment, key.getKeystore());
                }
                else
                {
                    PRDToken pro = GethDownloaderTask.get_PROTOKEN();
                    status =  sendPRD(address ,  new BigInt(amount) ,passw ,  key.getKeystore(),  eth , ctx,pro);
                }

                Intent i = new Intent(Send_Ethereum_Activity.SEND_STATUS);
                i.putExtra(Send_Ethereum_Activity.STATUS, status);
                mLocalBroadcastManager.sendBroadcast(i);

            }
        }
    };


    @Override
    public void taskFinished(SyncData syncData) {
        Intent i = new Intent(TransactionTab.TRANSACTION_TAB_FINISHED);
        i.putExtra(TransactionTab.PEER_COUNT, syncData.getPeers());
        i.putExtra(TransactionTab.CURRENT_BLOCK, syncData.getCurrentBlock());
        mLocalBroadcastManager.sendBroadcast(i);
    }
}
