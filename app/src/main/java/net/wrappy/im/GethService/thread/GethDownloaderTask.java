package net.wrappy.im.GethService.thread;

import android.content.Context;
import android.util.Log;

import net.wrappy.im.GethService.GethLight;
import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.db.Balance;
import net.wrappy.im.GethService.db.BalanceRepo;
import net.wrappy.im.GethService.model.SyncData;

import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Header;
import org.ethereum.geth.NewHeadHandler;
import org.ethereum.geth.SyncProgress;

/**
 * Created by sonntht on 23/10/2017.
 */

public class GethDownloaderTask implements Runnable  {
    private static final String TAG = GethDownloaderTask.class.getSimpleName();

    public interface ThreadCallback {
        void taskStarted();
        void taskUpdateWalletInfo(SyncData syncData);
        void taskProgressingStatus(SyncData syncData);
        void taskFinished(SyncData syncData);
    }

    private ThreadCallback mCallback;
    private String datadir;
    private GethLight eth;
    private KeyManager keyManager;
    private Context context;
    private PRDToken PRO;
    private SyncData syncData;
    String ETHnumber = "0";
    String PROnumber = "0";
    BalanceRepo balanceRepo;
    Balance balance;
    private volatile boolean mIsReady = false;
    long staringBlock = 0;

    public GethDownloaderTask(ThreadCallback callback, Context cxt) {
        Log.v(TAG, "Constructor");

        this.mCallback = callback;
        context = cxt;
    }

    @Override
    public void run() {
        Log.v(TAG, "run() - Begin");
        mCallback.taskStarted();
        datadir = context.getFilesDir().getAbsolutePath() + "/.ethereum/rinkeby";
        try {
            //-- Setup node
            keyManager = KeyManager.newKeyManager(context.getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);

            eth = GethLight.getRinkebyInstance(datadir, keyManager);

            PRO = new PRDToken(eth.getClient());
            syncData = new SyncData();
            balanceRepo = new BalanceRepo();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            NewHeadHandler handler = new NewHeadHandler() {
                @Override
                public void onError(String error) {
                }

                @Override
                public void onNewHead(final Header header) {
                    try{
                        if(header.getNumber() > 0) {
                            SyncProgress ethereumSyncProgress = eth.getClient().syncProgress(eth.getMainContext());
                            if ((ethereumSyncProgress != null && header.getNumber() == ethereumSyncProgress.getCurrentBlock()) || ethereumSyncProgress == null ) {
                                balance = new Balance();
                                balance.setAddress(keyManager.getAccounts().get(0).getAddress().getHex());
                                balance.setChain("4");
                                balance.setTokenAddress(PRDToken.contractAddress);
                                balance.setBlock(header.getNumber());

                                syncData.setPeers(eth.getNode().getPeersInfo().size());
                                syncData.setCurrentBlock(header.getNumber());
                                if(ethereumSyncProgress == null)
                                {
                                    syncData.setHighestBlock(header.getNumber());
                                }
                                else {
                                    syncData.setHighestBlock(ethereumSyncProgress.getHighestBlock());
                                }

                                if(syncData.getCurrentBlock() <  syncData.getHighestBlock()) {
                                    // TransactionTab
                                    mCallback.taskProgressingStatus(syncData);
                                    //WalletFragment
                                    mCallback.taskUpdateWalletInfo(syncData);
                                }else {
                                    mCallback.taskFinished(syncData);
                                }

                                ETHnumber = eth.getClient().getBalanceAt(eth.getMainContext(), keyManager.getAccounts().get(0).getAddress(), -1).toString();
                                balance.setBalance(ETHnumber);
                                if (header.getNumber() > PRDToken.BLOCK) {
                                    PROnumber = PRO.balanceOf(Geth.newCallOpts(), keyManager.getAccounts().get(0).getAddress()).toString();
                                    balance.setTokenBalance(PROnumber);
                                }
                                balanceRepo.insertOrUpdate(balance);
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            };
            eth.getClient().subscribeNewHead(eth.getMainContext(), handler, 16);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.v(TAG, "run() - End");
    }

    public EthereumClient get_eth()
    {
        return eth.getClient();
    }

    public KeyManager get_account()
    {
        return keyManager;
    }

    public org.ethereum.geth.Context get_context()
    {
        return eth.getMainContext();
    }

    public PRDToken get_PROTOKEN()
    {
        return PRO;
    }

    public void stopNode(){
        try {
            eth.stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
