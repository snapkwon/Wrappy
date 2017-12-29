package net.wrappy.im.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import net.wrappy.im.GethService.GethLightService;
import net.wrappy.im.GethService.KeyManager;
import net.wrappy.im.GethService.WalletInfo;
import net.wrappy.im.GethService.contracts.PRDToken;
import net.wrappy.im.GethService.db.Balance;
import net.wrappy.im.GethService.db.BalanceRepo;
import net.wrappy.im.R;
import net.wrappy.im.adapter.RecycleWalletAdapter;
import net.wrappy.im.model.ModelWaletListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PCPV on 11/28/2017.
 */

public class WalletFragment extends Fragment {

    private LocalBroadcastManager mLocalBroadcastManager;
    private Intent serviceIntent;

    private RecyclerView lv;
    private RecycleWalletAdapter adapter;
    private List<ModelWaletListView> arrWallet;
    FrameLayout mainViewContainer;
    private ModelWaletListView eth;
    private ModelWaletListView prd;
    TextView wallet_fragment_dola;
    ImageView imgBarCodeAddress;
    String hexAddress = "";


    final int TYPE_ETH = 0;
    final int TYPE_PRO = 1;


    public static final String MAIN_WALLET_STARTED = "MAIN_WALLET_STARTED";
    public static final String MAIN_WALLET_IN_PROGRESS = "MAIN_WALLET_IN_PROGRESS";

    private WalletInfo walletInfo;
    private KeyManager keyManager;
    private WalletTasks walletTask;
    private BalanceRepo balanceRepo;
    private Balance balance;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MAIN_WALLET_STARTED)) {

            }

            if (intent.getAction().equals(MAIN_WALLET_IN_PROGRESS)) {
                if (walletTask == null) {
                    walletTask = new WalletTasks();
                    walletTask.execute();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        copyFile("static-nodes.json");

        serviceIntent = new Intent(getActivity(), GethLightService.class);
        //getParentActivity().startService(serviceIntent);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(MAIN_WALLET_STARTED);
        mIntentFilter.addAction(MAIN_WALLET_IN_PROGRESS);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

    }

    private void copyFile(String filename) {
        AssetManager assetManager = getActivity().getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(getActivity().getFilesDir() + WalletInfo.GETH_PATH, filename);
            outFile.getParentFile().mkdirs();
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch (IOException e) {
            //Log.e("tag", "Failed to copy asset file: " + filename, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        buffer = null;
    }

    @Override
    public void onResume() {
        if (!isServiceRunning(GethLightService.class)) {
            getActivity().startService(serviceIntent);
        }

        if (walletTask == null) {
            walletTask = new WalletTasks();
            walletTask.execute();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);

        if (isServiceRunning(GethLightService.class)) {
            getActivity().stopService(serviceIntent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.wallet_fragment, container, false);
        //getActivity().startService(serviceIntent);
        lv = (RecyclerView) view.findViewById(R.id.recyclerviewcoins);
        wallet_fragment_dola = (TextView) view.findViewById(R.id.wallet_fragment_dola);
        imgBarCodeAddress = (ImageView) view.findViewById(R.id.imgBarCodeAddress);
        imgBarCodeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WalletQrCodeDetailActivity.class);
                getActivity().startActivity(intent);
            }
        });
        try {
            keyManager = KeyManager.newKeyManager(getActivity().getFilesDir().getAbsolutePath() + WalletInfo.KEYSTORE_PATH);
            hexAddress = keyManager.getAccounts().get(0).getAddress().getHex();
        } catch (Exception e) {
            e.printStackTrace();
        }

        balanceRepo = new BalanceRepo();
        balance = new Balance();
        balance.setChain("4");
        balance.setAddress(hexAddress);
        balance.setTokenAddress(PRDToken.contractAddress);

        walletInfo = new WalletInfo();
        walletInfo.setETHAddress(hexAddress);
        initContentListWallet();

        adapter.setOnItemClickListener(new RecycleWalletAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                {
                    Bundle b = new Bundle();
                    b.putInt("namecoin", arrWallet.get(position).getTypeCoin());
                    b.putString("number", arrWallet.get(position).getNumber());
                    b.putString("value", arrWallet.get(position).getPriceCoin());
                    if (arrWallet.get(position).getTypeCoin() == TYPE_PRO) {
                        try {
                            b.putDouble("rate", walletInfo.getPROPrice());
                        } catch (Exception e) {
                            b.putDouble("rate", 0);
                        }
                    } else {
                        try {
                            b.putDouble("rate", Double.parseDouble(walletInfo.coinInfo.getString("price_usd")));
                        } catch (Exception e) {
                            b.putDouble("rate", 0);
                        }
                    }
                    Intent intent = new Intent(getActivity(), TransactionTab.class);
                    intent.putExtras(b);
                    getActivity().startActivity(intent);
                }
            }

        });

        return view;
    }


    private void initContentListWallet() {
        arrWallet = new ArrayList<>();
        prd = new ModelWaletListView();
        prd.setNameCoin(getString(R.string.PRO));
        prd.setTypeCoin(TYPE_PRO);
        prd.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_proteusion));
        prd.setPriceCoin("0");
        prd.setPercent("0");
        prd.setValue("0");
        prd.setNumber("0");
        prd.setTextCoin(getActivity().getResources().getColor(R.color.textproteusion));
        arrWallet.add(prd);
        eth = new ModelWaletListView();
        eth.setNameCoin(getString(R.string.ETH));
        eth.setTypeCoin(TYPE_ETH);
        eth.setIcon(getActivity().getResources().getDrawable(R.drawable.ic_ethereum));
        eth.setTextCoin(getActivity().getResources().getColor(R.color.textethereum));
        arrWallet.add(eth);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        lv.setLayoutManager(layoutManager);
        adapter = new RecycleWalletAdapter(arrWallet);
        lv.setBackgroundColor(0xaabbcc);

        lv.addItemDecoration(
                new DividerItemDecoration(getActivity(), layoutManager.getOrientation()));
        lv.setAdapter(adapter);
    }


    private class WalletTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            walletInfo.updateCoinInfo();
            balance = balanceRepo.getWalletInfo(balance);
            walletInfo.setETHBalance(balance.getBalance());
            walletInfo.setPROBalance(balance.getTokenBalance());
            return null;
        }

        @Override
        protected void onPostExecute(Void arrs) {

            updateUIInfo();
            if (walletTask != null) {
                walletTask.cancel(true);
                walletTask = null;
            }
        }
    }

    private String formatNumber(Double number, String format) {
        return String.format(format, number);
    }


    private void updateUIInfo() {
        List<ModelWaletListView> arr = new ArrayList<ModelWaletListView>();
        try {
            eth.setPercent(formatNumber(walletInfo.getETHChange(), "%.2f"));
            String ethBalance = formatNumber(walletInfo.getETHBalance(), "%.4f");
            eth.setNumber(ethBalance);
            eth.setValue(ethBalance + " ETH");
            eth.setPriceCoin(formatNumber(walletInfo.getETHPrice(), "%.2f"));

            prd.setPercent(formatNumber(walletInfo.getPROChange(), "%.2f"));
            String proBalance = formatNumber(walletInfo.getPROBalance(), "%.0f");
            ;
            prd.setNumber(proBalance);
            prd.setValue(proBalance + " PRO");
            prd.setPriceCoin(formatNumber(walletInfo.getPROPrice(), "%.2f"));

            arr.add(prd);
            arr.add(eth);
            if (arr.size() > 0) {
                SetListCoin(arr);
            }
            Double price = walletInfo.getETHPrice() + walletInfo.getPROPrice();
            wallet_fragment_dola.setText(String.format("$%.1f", price));
        } catch (Exception ex) {
        }
    }

    public void SetListCoin(List<ModelWaletListView> list) {
        arrWallet.clear();
        for (ModelWaletListView item : list) {
            arrWallet.add(item);
        }
        adapter.updateList(arrWallet);

    }
}


