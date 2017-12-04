package net.wrappy.im.GethService;

/**
 * Created by sonntht on 21/10/2017.
 */

import net.wrappy.im.GethService.exception.GethLightServiceException;
import net.wrappy.im.GethService.model.Balance;
import net.wrappy.im.GethService.model.Transaction;

import org.ethereum.geth.Account;
import org.ethereum.geth.Context;
import org.ethereum.geth.EthereumClient;
import org.ethereum.geth.Geth;
import org.ethereum.geth.Node;
import org.ethereum.geth.SyncProgress;

import java.util.List;

public class GethLight {

    private String datadir;
    private Context mainContext;
    private Node node;
    private ChainConfig chainConfig;
    private EthereumClient client;
    private KeyManager keyManager;
    private Account mainAccount;
    private static GethLight gethLight;

    private GethLight() {
    }

    private static void setMainAccountAtIndex(GethLight context, int keyManagerIndex)
            throws Exception {
        if (context.keyManager == null)
            throw new GethLightServiceException("no key manager configured");
        context.mainAccount = context.keyManager.getAccounts().get(keyManagerIndex);
    }

    /*
    * Set the main account by default if none has been defined.
    * By default the main account is the first one in the key manager.
    * If no key manager is referenced, main account is null.
    * If there is no accounts in key manager, main account is null.
    */
    private static void setDefaultMainAccount(GethLight eth) throws Exception {
        if (eth.mainAccount == null && eth.keyManager != null) {
            List<Account> accounts = eth.keyManager.getAccounts();
            if (accounts.size() > 0) {
                eth.mainAccount = accounts.get(0);
            }
        }
    }

    private static void setMainAccount(GethLight eth, Account mainAccount) {
        if (eth.keyManager != null && !eth.keyManager.accountExists(mainAccount)) {
            throw new GethLightServiceException("given account doesn't exist in key manager");
        }
        eth.mainAccount = mainAccount;
    }

    public void start() throws Exception {
        node.start();
        client = node.getEthereumClient();
    }

    public void stop() throws Exception {
        node.stop();
    }

    public boolean isSyncing() throws Exception {
        return client.syncProgress(mainContext) != null;
    }

    public boolean isSynced() throws Exception {
        SyncProgress progress = client.syncProgress(mainContext);
        return progress.getCurrentBlock() >= progress.getHighestBlock();
    }

    public Transaction newTransaction() throws Exception {
        return new Transaction(this);
    }

    public Balance getBalance() throws Exception {
        return getBalanceOf(mainAccount);
    }

    public Balance getBalanceOf(Account account) throws Exception {
        return Balance.of(client.getPendingBalanceAt(mainContext, account.getAddress()).getInt64());
    }

    public Balance getBalanceAtBlock(long number) throws Exception {
        return Balance.of(client.getBalanceAt(mainContext, mainAccount.getAddress(), number).getInt64());
    }

    public ChainConfig getChainConfig() {
        return chainConfig;
    }

    public Context getMainContext() {
        return mainContext;
    }

    public EthereumClient getClient() {
        return client;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public Node getNode() {
        return node;
    }

    /**
     * Set a key manager to the context
     * If the current main account is not in the given key manager, it became the default account of
     * key manager.
     *
     * @param keyManager the new key manager
     * @throws Exception Exceptions can be thrown by Geth through JNI
     */
    public void setKeyManager(KeyManager keyManager) throws Exception {
        this.keyManager = keyManager;
        if (!this.keyManager.accountExists(this.mainAccount)) this.mainAccount = null;
        if (this.mainAccount == null) setDefaultMainAccount(this);
    }

    public Account getMainAccount() {
        return mainAccount;
    }

    public void setMainAccount(Account mainAccount) {
        setMainAccount(this, mainAccount);
    }

    public void setMainAccountAtIndex(int keyManagerIndex) throws Exception {
        setMainAccountAtIndex(this, keyManagerIndex);
    }

    public static GethLight getRinkebyInstance(String datadir, KeyManager keyManager) throws Exception {
        if (gethLight == null) {
            gethLight = new GethLight.Builder(datadir)
                    .withChainConfig(ChainConfig.getRinkebyConfig())
                    .withKeyManager(keyManager)
                    .build();
            gethLight.start();
        }

        return gethLight;
    }

    public static class Builder {

        GethLight build;

        /**
         * Parametrized Builder with the default values :
         * <ul>
         * <li>Context : @{@link Builder#withCancelContext()}</li>
         * </ul>
         *
         * @param datadir Directory where to store Geth files
         */
        public Builder(String datadir) {
            build = new GethLight();
            withDefaultContext();
            withDatadirPath(datadir);
        }

        /**
         * Release resources when execution is over.
         *
         * @return reference on the parametrized builder
         */
        public Builder withDefaultContext() {
            build.mainContext = Geth.newContext();
            return this;
        }

        /**
         * Release resources when execution is over or when a cancel is asked
         *
         * @return reference on the parametrized builder
         */
        public Builder withCancelContext() {
            build.mainContext = Geth.newContext().withCancel();
            return this;
        }

        /**
         * Release resources when execution is over or when it reach the deadline (golang context).
         *
         * @param seconds     //TODO
         * @param nanoseconds //TODO
         * @return reference on the parametrized builder
         */
        public Builder withDeadlineContext(long seconds, long nanoseconds) {
            build.mainContext = Geth.newContext().withDeadline(seconds, nanoseconds);
            return this;
        }

        /**
         * Release resources when execution is over or when @seconds passed since the function call.
         *
         * @param seconds number of seconds to wait before canceling the call
         * @return reference on the parametrized builder
         */
        public Builder withTimeoutContext(long seconds) {
            build.mainContext = Geth.newContext().withTimeout(seconds);
            return this;
        }

        /**
         * Set path where node files will be saved (node key, blockchain db, ...)
         *
         * @param datadir string path where to save node files
         * @return reference on the parametrized builder
         */
        public Builder withDatadirPath(String datadir) {
            build.datadir = datadir;
            return this;
        }

        public Builder onPublicNetwork(ChainConfig.NETWORK network) {
            switch (network) {
                case HOMESTEAD:
                    return onMainnet();
                case ROPSTEN:
                    return onTestnet();
                case RINKEBY:
                    return onRinkeby();
                default:
                    throw new IllegalArgumentException("Unknwon network id : " + network);
            }
        }

        public Builder onMainnet() {
            build.chainConfig = ChainConfig.getMainnetConfig();
            return this;
        }

        public Builder onTestnet() {
            build.chainConfig = ChainConfig.getTestnetConfig();
            return this;
        }

        public Builder onRinkeby() {
            build.chainConfig = ChainConfig.getRinkebyConfig();
            return this;
        }

        public Builder withChainConfig(ChainConfig chainConfig) {
            build.chainConfig = chainConfig;
            return this;
        }

        public Builder withKeyManager(KeyManager keyManager) {
            build.keyManager = keyManager;
            return this;
        }

        public Builder withMainAccount(Account mainAccount) {
            setMainAccount(build, mainAccount);
            return this;
        }

        public Builder withMainAccountAtIndex(int keyManagerIndex) throws Exception {
            setMainAccountAtIndex(build, keyManagerIndex);
            return this;
        }

        public GethLight build() throws Exception {
            setDefaultMainAccount(build);
            build.node = Geth.newNode(build.datadir, build.chainConfig.nodeConfig);
            return build;
        }
    }
}