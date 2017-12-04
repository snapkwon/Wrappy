package net.wrappy.im.GethService;

/**
 * Created by sonntht on 21/10/2017.
 */

import org.ethereum.geth.Account;
import org.ethereum.geth.Accounts;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class KeyManager {

    private static final String KEYSTORE_DIRNAME = "/keystore";
    private KeyStore keystore;

    private KeyManager(String datadir) {
        File keystoreDir = new File(datadir);
        if (!keystoreDir.exists()) keystoreDir.mkdir();
        keystore = Geth.newKeyStore(keystoreDir.getAbsolutePath(), Geth.LightScryptN,
                Geth.LightScryptP);
    }

    public String getkeystore ()
    {
        String key = keystore.toString();
        return key;
    }
    public static KeyManager newKeyManager(String datadir) {
        return new KeyManager(datadir);
    }

    public Account newAccount(String passphrase) throws Exception {
        return keystore.newAccount(passphrase);
    }

    public Account newUnlockedAccount(String passphrase) throws Exception {
        Account ret = newAccount(passphrase);
        unlockAccount(ret, passphrase);
        return ret;
    }

    public List<Account> getAccounts() throws Exception {
        List<Account> ret = new ArrayList<>();
        Accounts accounts = keystore.getAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            ret.add(accounts.get(i));
        }
        return ret;
    }

    public boolean accountExists(Account account) {
        if (account == null) return false;
        return keystore.hasAddress(account.getAddress());
    }

    // removes the private key with the given address from memory.
    public void lockAccount(Account account) throws Exception {
        keystore.lock(account.getAddress());
    }

    // unlockAccountDuring with 0 as duration
    public void unlockAccount(Account account, String passphrase) throws Exception {
        keystore.unlock(account, passphrase);
    }

    // seconds == 0 : until program exits
    public void unlockAccountDuring(Account account, String passphrase, long seconds)
            throws Exception {
        keystore.timedUnlock(account, passphrase, (long) (seconds * Math.pow(10, 9)));
    }

    public void deleteAccount(Account account, String passphrase) throws Exception {
        keystore.deleteAccount(account, passphrase);
    }

    public void updateAccountPassphrase(Account account, String passphrase, String newPassphrase)
            throws Exception {
        keystore.updateAccount(account, passphrase, newPassphrase);
    }

    public KeyStore getKeystore() {
        return keystore;
    }
}