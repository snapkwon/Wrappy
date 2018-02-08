/*
 * Copyright (C) 2007-2008 Esmertec AG. Copyright (C) 2007-2008 The Android Open
 * Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.wrappy.im;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import net.ironrabbit.type.CustomTypefaceManager;
import net.sqlcipher.database.SQLiteDatabase;
import net.wrappy.im.GethService.db.WalletDBHelper;
import net.wrappy.im.crypto.otr.OtrAndroidKeyManagerImpl;
import net.wrappy.im.crypto.otr.OtrChatManager;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.helper.RestAPIListener;
import net.wrappy.im.model.Contact;
import net.wrappy.im.model.ImConnection;
import net.wrappy.im.model.ImErrorInfo;
import net.wrappy.im.model.RegistrationAccount;
import net.wrappy.im.model.WpKMemberDto;
import net.wrappy.im.plugin.xmpp.XmppAddress;
import net.wrappy.im.provider.Imps;
import net.wrappy.im.provider.ImpsProvider;
import net.wrappy.im.provider.Store;
import net.wrappy.im.service.Broadcaster;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IConnectionCreationListener;
import net.wrappy.im.service.IContactListManager;
import net.wrappy.im.service.IImConnection;
import net.wrappy.im.service.IRemoteImService;
import net.wrappy.im.service.ImServiceConstants;
import net.wrappy.im.service.NetworkConnectivityReceiver;
import net.wrappy.im.service.RemoteImService;
import net.wrappy.im.service.StatusBarNotifier;
import net.wrappy.im.service.adapters.ChatSessionManagerAdapter;
import net.wrappy.im.tasks.MigrateAccountTask;
import net.wrappy.im.ui.LauncherActivity;
import net.wrappy.im.ui.legacy.ImPluginHelper;
import net.wrappy.im.ui.legacy.ProviderDef;
import net.wrappy.im.ui.legacy.adapter.ConnectionListenerAdapter;
import net.wrappy.im.util.Debug;
import net.wrappy.im.util.Languages;
import net.wrappy.im.util.LogCleaner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.cacheword.PRNGFixes;
import info.guardianproject.iocipher.VirtualFileSystem;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ImApp extends MultiDexApplication implements ICacheWordSubscriber {

    public static final String LOG_TAG = "Wrappy";

    public static final String EXTRA_INTENT_SEND_TO_USER = "Send2_U";
    public static final String EXTRA_INTENT_PASSWORD = "password";

    public static final String EXTRA_INTENT_PROXY_TYPE = "proxy.type";
    public static final String EXTRA_INTENT_PROXY_HOST = "proxy.host";
    public static final String EXTRA_INTENT_PROXY_PORT = "proxy.port";

    public static final String IMPS_CATEGORY = "net.wrappy.im.service.IMPS_CATEGORY";
    public static final String ACTION_QUIT = "net.wrappy.im.service.QUIT";

    public static final int SMALL_AVATAR_WIDTH = 48;
    public static final int SMALL_AVATAR_HEIGHT = 48;

    public static final int DEFAULT_AVATAR_WIDTH = 196;
    public static final int DEFAULT_AVATAR_HEIGHT = 196;

    public static final String HOCKEY_APP_ID = "3cd4c5ff8b666e25466d3b8b66f31766";

    public static final String DEFAULT_TIMEOUT_CACHEWORD = "-1"; //one day

    public static final String CACHEWORD_PASSWORD_KEY = "pkey";
    public static final String CLEAR_PASSWORD_KEY = "clear_key";

    public static final String NO_CREATE_KEY = "nocreate";

    public static final String PREFERENCE_KEY_TEMP_PASS = "temppass";

    //ACCOUNT SETTINGS Imps defaults
    public static final String DEFAULT_XMPP_RESOURCE = "ChatSecureZom";
    public static final int DEFAULT_XMPP_PRIORITY = 20;
    public static final String DEFAULT_XMPP_OTR_MODE = "auto";

    private Locale locale = null;

    public static ImApp sImApp;

    private static IRemoteImService mImService;

    // HashMap<Long, IImConnection> mConnections;
    MyConnListener mConnectionListener;
    HashMap<Long, ProviderDef> mProviders;

    Broadcaster mBroadcaster;

    /**
     * A queue of messages that are waiting to be sent when service is
     * connected.
     */
    ArrayList<Message> mQueue = new ArrayList<Message>();

    /**
     * A flag indicates that we have called tomServiceStarted start the service.
     */
//    private boolean mServiceStarted;
    private static Context mApplicationContext;

    private CacheWordHandler mCacheWord;

    public static Executor sThreadPoolExecutor = null;

    private static WalletDBHelper walletDBHelper;

    private SharedPreferences settings = null;

    private boolean mThemeDark = false;

    private boolean mNeedsAccountUpgrade = false;

    public static final int EVENT_SERVICE_CONNECTED = 100;
    public static final int EVENT_CONNECTION_CREATED = 150;
    public static final int EVENT_CONNECTION_LOGGING_IN = 200;
    public static final int EVENT_CONNECTION_LOGGED_IN = 201;
    public static final int EVENT_CONNECTION_LOGGING_OUT = 202;
    public static final int EVENT_CONNECTION_DISCONNECTED = 203;
    public static final int EVENT_CONNECTION_SUSPENDED = 204;
    public static final int EVENT_USER_PRESENCE_UPDATED = 300;
    public static final int EVENT_UPDATE_USER_PRESENCE_ERROR = 301;

    private static final String[] PROVIDER_PROJECTION = {Imps.Provider._ID, Imps.Provider.NAME,
            Imps.Provider.FULLNAME,
            Imps.Provider.SIGNUP_URL,};

    private static final String[] ACCOUNT_PROJECTION = {Imps.Account._ID, Imps.Account.PROVIDER,
            Imps.Account.NAME, Imps.Account.USERNAME,
            Imps.Account.PASSWORD, Imps.Account.ACCOUNT_NAME,};

    static final void log(String log) {
        Log.d(LOG_TAG, log);
    }

    /**
     * protected void attachBaseContext(Context base) {
     * super.attachBaseContext(base);
     * MultiDex.install(this);
     * }
     */

    @Override
    public ContentResolver getContentResolver() {
        if (mApplicationContext == null || mApplicationContext == this) {
            return super.getContentResolver();
        }

        return mApplicationContext.getContentResolver();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Preferences.setup(this);
        Languages.setup(MainActivity.class, R.string.use_system_default);
        Languages.setLanguage(this, Preferences.getLanguage(), false);

        sImApp = this;
//        if (!BuildConfig.DEBUG) {
//            Fabric.with(this, new Crashlytics());
//        }

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        Debug.onAppStart();

        PRNGFixes.apply(); //Google's fix for SecureRandom bug: http://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html

        // load these libs up front to shorten the delay after typing the passphrase
        SQLiteDatabase.loadLibs(getApplicationContext());
        VirtualFileSystem.get().isMounted();

        // mConnections = new HashMap<Long, IImConnection>();
        mApplicationContext = this;

        //initTrustManager();

        mBroadcaster = new Broadcaster();

        setAppTheme(null, null);

        // ChatSecure-Push needs to do initial setup as soon as Cacheword is ready
        mCacheWord = new CacheWordHandler(this, this);
        mCacheWord.connectToService();

        if (sThreadPoolExecutor == null) {
            int corePoolSize = 20;
            int maximumPoolSize = 120;
            int keepAliveTime = 30;
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
            sThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkConnectivityReceiver(), intentFilter);

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        walletDBHelper = new WalletDBHelper(this.getApplicationContext());
//        WalletDatabaseManager.initializeInstance(walletDBHelper);


        RestAPI.initIon(getApplicationContext());

    }

    public void logout() {
        ShortcutBadger.applyCount(ImApp.sImApp.getApplicationContext(), 0);
        OtrChatManager.endAllSessions();
        resetDB();
        ImPluginHelper.getInstance(sImApp).reset();
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void logoutConnection() {
        try {
            IImConnection connection = getConnection(sImApp.getDefaultProviderId(), sImApp.getDefaultAccountId());
            ((ChatSessionManagerAdapter) connection.getChatSessionManager()).closeAllChatSessions();
            connection.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetDB() {
        if (mCacheWord != null) {
            logoutConnection();
            mDefaultProviderId = -1;
            mDefaultAccountId = -1;
            mDefaultUsername = null;
            mDefaultOtrFingerprint = null;
            mDefaultNickname = null;
            String tempPassphrase = settings.getString(ImApp.PREFERENCE_KEY_TEMP_PASS, null);
            Store.clear(this);
            settings.edit().clear().apply();
            serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);
            mApplicationContext.stopService(serviceIntent);

            settings.edit().putString(ImApp.PREFERENCE_KEY_TEMP_PASS, tempPassphrase).apply();

            Imps.reset(getContentResolver());
            // Clear all notification
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null) {
                nMgr.cancelAll();
            }
        }
    }

    public boolean isThemeDark() {
        return mThemeDark;
    }

    public void setAppTheme(Activity activity) {
        setAppTheme(activity, null);
    }

    public void setAppTheme(Activity activity, Toolbar toolbar) {

        mThemeDark = settings.getBoolean("themeDark", false);

        if (mThemeDark) {
            setTheme(R.style.AppThemeDark);


            if (activity != null) {
                activity.setTheme(R.style.AppThemeDark);

            }

        } else {
            setTheme(R.style.AppTheme);


            if (activity != null) {
                activity.setTheme(R.style.AppTheme);

            }


        }

        Configuration config = getResources().getConfiguration();
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        if (mImService != null) {
            boolean debugOn = settings.getBoolean("prefDebug", false);
            try {
                mImService.enableDebugLogging(debugOn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void resetLanguage(Activity activity, String language) {
        if (!TextUtils.equals(language, Preferences.getLanguage())) {
            /* Set the preference after setting the locale in case something goes
             * wrong. If setting the locale causes an Exception, it should not be set in
             * the preferences, otherwise this will be stuck in a crash loop. */
            Languages.setLanguage(activity, language, true);
            Preferences.setLanguage(language);
            Languages.forceChangeLanguage(activity);

            CustomTypefaceManager.loadFromAssets(activity, language.equals("bo"));


        }
    }

    Intent serviceIntent;

    public synchronized void startImServiceIfNeed() {
        startImServiceIfNeed(false);
    }

    public synchronized void startImServiceIfNeed(boolean isBoot) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            log("start ImService");

        if (mImService == null) {

            serviceIntent = new Intent(this, RemoteImService.class);
//        serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, isBoot);

            mApplicationContext.startService(serviceIntent);

            mConnectionListener = new MyConnListener(new Handler());

            mApplicationContext
                    .bindService(serviceIntent, mImServiceConn, Context.BIND_AUTO_CREATE);

        }

    }

    public boolean hasActiveConnections() {
        try {
            return !mImService.getActiveConnections().isEmpty();
        } catch (RemoteException re) {
            return false;
        }

    }

    public void stopImServiceIfInactive() {

        //todo we don't wnat to do this right now
        /**
         if (!hasActiveConnections()) {
         if (Log.isLoggable(LOG_TAG, Log.DEBUG))
         log("stop ImService because there's no active connections");

         forceStopImService();

         }*/
    }


    public void forceStopImService() {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG))
            log("stop ImService");


        if (mImServiceConn != null) {
            try {
                if (mImService != null)
                    mImService.shutdownAndLock();
            } catch (RemoteException re) {

            }
            mApplicationContext.unbindService(mImServiceConn);

            mImService = null;
        }

        // Intent serviceIntent = new Intent(this, RemoteImService.class);
        serviceIntent.putExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, true);
        mApplicationContext.stopService(serviceIntent);


    }

    private ServiceConnection mImServiceConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                log("service connected");

            mImService = IRemoteImService.Stub.asInterface(service);
            //   fetchActiveConnections();

            synchronized (mQueue) {
                for (Message msg : mQueue) {
                    msg.sendToTarget();
                }
                mQueue.clear();
            }
            Message msg = Message.obtain(null, EVENT_SERVICE_CONNECTED);
            mBroadcaster.broadcast(msg);

            /*
            if (mKillServerOnStart)
            {
                forceStopImService();
            }*/
        }

        public void onServiceDisconnected(ComponentName className) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                log("service disconnected");

            // mConnections.clear();
            mImService = null;
        }
    };

    public boolean serviceConnected() {
        return mImService != null;
    }

    public static long insertOrUpdateAccount(ContentResolver cr, long providerId, long accountId, String nickname, String username,
                                             String pw) {
        return insertOrUpdateAccount(cr, providerId, accountId, nickname, username, pw, null, null);
    }

    public static long insertOrUpdateAccount(ContentResolver cr, long providerId, long accountId, String nickname, String username,
                                             String pw, String account_name) {
        return insertOrUpdateAccount(cr, providerId, accountId, nickname, username, pw, account_name, null);
    }

    public static long insertOrUpdateAccount(ContentResolver cr, long providerId, long accountId, String nickname, String username,
                                             String pw, String account_name, RegistrationAccount account) {
        String selection = Imps.Account.PROVIDER + "=? AND (" + Imps.Account._ID + "=?" + " OR " + Imps.Account.USERNAME + "=?)";
        String[] selectionArgs = {Long.toString(providerId), Long.toString(accountId), username};

        Cursor c = cr.query(Imps.Account.CONTENT_URI, ACCOUNT_PROJECTION, selection, selectionArgs,
                null);
        if (c != null && c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndexOrThrow(Imps.Account._ID));

            ContentValues values = new ContentValues(4);
            values.put(Imps.Account.PROVIDER, providerId);

            if (!TextUtils.isEmpty(nickname))
                values.put(Imps.Account.NAME, nickname);

            if (!TextUtils.isEmpty(username))
                values.put(Imps.Account.USERNAME, username);

            if (!TextUtils.isEmpty(pw))
                values.put(Imps.Account.PASSWORD, pw);

            if (!TextUtils.isEmpty(account_name))
                values.put(Imps.Account.ACCOUNT_NAME, account_name);

            if (account != null && !TextUtils.isEmpty(account.getEmail()))
                values.put(Imps.Account.ACCOUNT_EMAIL, account.getEmail());

            if (account != null && !TextUtils.isEmpty(account.getPhone()))
                values.put(Imps.Account.ACCOUNT_PHONE, account.getPhone());

            if (account != null && !TextUtils.isEmpty(account.getEmail()))
                values.put(Imps.Account.ACCOUNT_GENDER, account.getGender());

            Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, id);
            cr.update(accountUri, values, null, null);

            c.close();
            return id;
        } else {
            ContentValues values = new ContentValues(6);
            values.put(Imps.Account.PROVIDER, providerId);
            values.put(Imps.Account.NAME, nickname);
            values.put(Imps.Account.USERNAME, username);
            values.put(Imps.Account.PASSWORD, pw);
            //values.put(Imps.Account.ACCOUNT_NAME, account_name);
            if (account != null) {
                values.put(Imps.Account.ACCOUNT_EMAIL, account.getEmail());
                values.put(Imps.Account.ACCOUNT_PHONE, account.getPhone());
                values.put(Imps.Account.ACCOUNT_GENDER, account.getGender());
            }

            if (pw != null && pw.length() > 0) {
                values.put(Imps.Account.KEEP_SIGNED_IN, true);
            }

            Uri result = cr.insert(Imps.Account.CONTENT_URI, values);
            if (c != null)
                c.close();
            return ContentUris.parseId(result);
        }
    }

    private void loadImProviderSettings() {

        mProviders = new HashMap<Long, ProviderDef>();
        ContentResolver cr = getContentResolver();

        String selectionArgs[] = new String[1];
        selectionArgs[0] = ImApp.IMPS_CATEGORY;

        Cursor c = cr.query(Imps.Provider.CONTENT_URI, PROVIDER_PROJECTION, Imps.Provider.CATEGORY
                        + "=?", selectionArgs,
                null);
        if (c == null) {
            return;
        }

        try {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String providerName = c.getString(1);
                String fullName = c.getString(2);
                String signUpUrl = c.getString(3);

                if (mProviders == null) // mProviders has been reset
                    break;
                mProviders.put(id, new ProviderDef(id, providerName, fullName, signUpUrl));
            }
        } finally {
            c.close();
        }
    }

    public long getProviderId(String name) {
        loadImProviderSettings();
        for (ProviderDef provider : mProviders.values()) {
            if (provider.mName.equals(name)) {
                return provider.mId;
            }
        }
        return -1;
    }

    public ProviderDef getProvider(long id) {
        loadImProviderSettings();
        return mProviders.get(id);
    }

    public static IImConnection createConnection(long providerId, long accountId) throws RemoteException {

        if (mImService == null) {
            // Service hasn't been connected or has died.
            return null;
        }

        IImConnection conn = mImService.createConnection(providerId, accountId);

        return conn;
    }

    public static IImConnection getConnection(long providerId, long accountId) {

        try {

            if (providerId == -1 || accountId == -1)
                throw new RuntimeException("getConnection() needs valid values: " + providerId + "," + accountId);

            if (mImService != null) {
                IImConnection im = mImService.getConnection(providerId, accountId);

                if (im != null) {

                    im.getState();

                } else {
                    im = createConnection(providerId, accountId);

                }

                return im;
            } else
                return null;
        } catch (RemoteException re) {
            return null;
        }
    }


    public Collection<IImConnection> getActiveConnections() {

        try {
            return mImService.getActiveConnections();
        } catch (RemoteException re) {
            return null;
        }
    }

    public void callWhenServiceConnected(Handler target, Runnable callback) {
        Message msg = Message.obtain(target, callback);
        if (serviceConnected() && msg != null) {
            msg.sendToTarget();
        } else {
            startImServiceIfNeed();
            synchronized (mQueue) {
                mQueue.add(msg);
            }
        }
    }

    public static void deleteAccount(ContentResolver resolver, long accountId, long providerId) {

        IImConnection conn = getConnection(providerId, accountId);

        Uri accountUri = ContentUris.withAppendedId(Imps.Account.CONTENT_URI, accountId);
        resolver.delete(accountUri, null, null);

        Uri providerUri = ContentUris.withAppendedId(Imps.Provider.CONTENT_URI, providerId);
        resolver.delete(providerUri, null, null);

        Uri.Builder builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
        ContentUris.appendId(builder, providerId);
        ContentUris.appendId(builder, accountId);
        resolver.delete(builder.build(), null, null);

        clearApplicationData();
        // mApplicationContext.deleteDatabase(ImpsProvider.ENCRYPTED_DATABASE_NAME);
        // mApplicationContext.deleteDatabase(ImpsProvider.UNENCRYPTED_DATABASE_NAME);

       /* SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear().commit();*/


    }

    public static void clearApplicationData() {
        File cache = mApplicationContext.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }


    public void registerForBroadcastEvent(int what, Handler target) {
        mBroadcaster.request(what, target, what);
    }

    public void unregisterForBroadcastEvent(int what, Handler target) {
        mBroadcaster.cancelRequest(what, target, what);
    }

    public void registerForConnEvents(Handler handler) {
        mBroadcaster.request(EVENT_CONNECTION_CREATED, handler, EVENT_CONNECTION_CREATED);
        mBroadcaster.request(EVENT_CONNECTION_LOGGING_IN, handler, EVENT_CONNECTION_LOGGING_IN);
        mBroadcaster.request(EVENT_CONNECTION_LOGGED_IN, handler, EVENT_CONNECTION_LOGGED_IN);
        mBroadcaster.request(EVENT_CONNECTION_LOGGING_OUT, handler, EVENT_CONNECTION_LOGGING_OUT);
        mBroadcaster.request(EVENT_CONNECTION_SUSPENDED, handler, EVENT_CONNECTION_SUSPENDED);
        mBroadcaster.request(EVENT_CONNECTION_DISCONNECTED, handler, EVENT_CONNECTION_DISCONNECTED);
        mBroadcaster.request(EVENT_USER_PRESENCE_UPDATED, handler, EVENT_USER_PRESENCE_UPDATED);
        mBroadcaster.request(EVENT_UPDATE_USER_PRESENCE_ERROR, handler,
                EVENT_UPDATE_USER_PRESENCE_ERROR);
    }

    public void unregisterForConnEvents(Handler handler) {
        mBroadcaster.cancelRequest(EVENT_CONNECTION_CREATED, handler, EVENT_CONNECTION_CREATED);
        mBroadcaster.cancelRequest(EVENT_CONNECTION_LOGGING_IN, handler,
                EVENT_CONNECTION_LOGGING_IN);
        mBroadcaster.cancelRequest(EVENT_CONNECTION_LOGGED_IN, handler, EVENT_CONNECTION_LOGGED_IN);
        mBroadcaster.cancelRequest(EVENT_CONNECTION_LOGGING_OUT, handler,
                EVENT_CONNECTION_LOGGING_OUT);
        mBroadcaster.cancelRequest(EVENT_CONNECTION_SUSPENDED, handler, EVENT_CONNECTION_SUSPENDED);
        mBroadcaster.cancelRequest(EVENT_CONNECTION_DISCONNECTED, handler,
                EVENT_CONNECTION_DISCONNECTED);
        mBroadcaster.cancelRequest(EVENT_USER_PRESENCE_UPDATED, handler,
                EVENT_USER_PRESENCE_UPDATED);
        mBroadcaster.cancelRequest(EVENT_UPDATE_USER_PRESENCE_ERROR, handler,
                EVENT_UPDATE_USER_PRESENCE_ERROR);
    }

    void broadcastConnEvent(int what, long providerId, ImErrorInfo error) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            log("broadcasting connection event " + what + ", provider id " + providerId);
        }
        android.os.Message msg = android.os.Message.obtain(null, what, (int) (providerId >> 32),
                (int) providerId, error);
        mBroadcaster.broadcast(msg);
    }

    public void dismissChatNotification(long providerId, String username) {
        if (mImService != null) {
            try {
                mImService.dismissChatNotification(providerId, username);
            } catch (RemoteException e) {
            }
        }
    }

    public void dismissNotification(long providerId) {
        if (mImService != null) {
            try {
                mImService.dismissNotifications(providerId);
            } catch (RemoteException e) {
            }
        }
    }

    /**
     * private void fetchActiveConnections() {
     * if (mImService != null)
     * {
     * try {
     * // register the listener before fetch so that we won't miss any connection.
     * mImService.addConnectionCreatedListener(mConnCreationListener);
     * synchronized (mConnections) {
     * for (IBinder binder : (List<IBinder>) mImService.getActiveConnections()) {
     * IImConnection conn = IImConnection.Stub.asInterface(binder);
     * long providerId = conn.getProviderId();
     * if (!mConnections.containsKey(providerId)) {
     * mConnections.put(providerId, conn);
     * conn.registerConnectionListener(mConnectionListener);
     * }
     * }
     * }
     * } catch (RemoteException e) {
     * Log.e(LOG_TAG, "fetching active connections", e);
     * }
     * }
     * }
     */

    private final IConnectionCreationListener mConnCreationListener = new IConnectionCreationListener.Stub() {
        public void onConnectionCreated(IImConnection conn) throws RemoteException {
            long providerId = conn.getProviderId();
            conn.registerConnectionListener(mConnectionListener);

            /**
             synchronized (mConnections) {
             if (!mConnections.containsKey(providerId)) {
             mConnections.put(providerId, conn);
             conn.registerConnectionListener(mConnectionListener);
             }
             }*/
            broadcastConnEvent(EVENT_CONNECTION_CREATED, providerId, null);
        }
    };

    private final class MyConnListener extends ConnectionListenerAdapter {
        public MyConnListener(Handler handler) {
            super(handler);
        }

        @Override
        public void onConnectionStateChange(IImConnection conn, int state, ImErrorInfo error) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                log("onConnectionStateChange(" + state + ", " + error + ")");
            }

            try {

                //fetchActiveConnections();

                int what = -1;
                long providerId = conn.getProviderId();
                switch (state) {
                    case ImConnection.LOGGED_IN:
                        what = EVENT_CONNECTION_LOGGED_IN;
                        break;

                    case ImConnection.LOGGING_IN:
                        what = EVENT_CONNECTION_LOGGING_IN;
                        break;

                    case ImConnection.LOGGING_OUT:
                        // NOTE: if this logic is changed, the logic in ImConnectionAdapter.ConnectionAdapterListener must be changed to match
                        what = EVENT_CONNECTION_LOGGING_OUT;

                        break;

                    case ImConnection.DISCONNECTED:
                        // NOTE: if this logic is changed, the logic in ImConnectionAdapter.ConnectionAdapterListener must be changed to match
                        what = EVENT_CONNECTION_DISCONNECTED;
                        //     mConnections.remove(providerId);
                        // stop the service if there isn't an active connection anymore.
                        stopImServiceIfInactive();

                        break;

                    case ImConnection.SUSPENDED:
                        what = EVENT_CONNECTION_SUSPENDED;
                        break;
                }
                if (what != -1) {
                    broadcastConnEvent(what, providerId, error);
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onConnectionStateChange", e);
            }
        }

        @Override
        public void onUpdateSelfPresenceError(IImConnection connection, ImErrorInfo error) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                log("onUpdateUserPresenceError(" + error + ")");
            }
            try {
                long providerId = connection.getProviderId();
                broadcastConnEvent(EVENT_UPDATE_USER_PRESENCE_ERROR, providerId, error);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onUpdateUserPresenceError", e);
            }
        }

        @Override
        public void onSelfPresenceUpdated(IImConnection connection) {
            if (Log.isLoggable(LOG_TAG, Log.DEBUG))
                log("onUserPresenceUpdated");

            try {
                long providerId = connection.getProviderId();
                broadcastConnEvent(EVENT_USER_PRESENCE_UPDATED, providerId, null);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onUserPresenceUpdated", e);
            }
        }
    }

    public IChatSession getChatSession(long providerId, long accountId, String remoteAddress) {

        IImConnection conn = getConnection(providerId, accountId);

        IChatSessionManager chatSessionManager = null;
        if (conn != null) {
            try {
                chatSessionManager = conn.getChatSessionManager();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "error in getting ChatSessionManager", e);
            }
        }

        if (chatSessionManager != null) {
            try {
                return chatSessionManager.getChatSession(remoteAddress);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "error in getting ChatSession", e);
            }
        }

        return null;
    }

    public void maybeInit(Activity activity) {
        startImServiceIfNeed();
        setAppTheme(activity, null);
        ImPluginHelper.getInstance(this).loadAvailablePlugins();
    }


    public boolean setDefaultAccount(long providerId, long accountId) {

        final Uri uri = Imps.Provider.CONTENT_URI_WITH_ACCOUNT;
        String[] PROVIDER_PROJECTION = {
                Imps.Provider._ID,
                Imps.Provider.ACTIVE_ACCOUNT_ID,
                Imps.Provider.ACTIVE_ACCOUNT_USERNAME,
                Imps.Provider.ACTIVE_ACCOUNT_NICKNAME,
                Imps.Provider.ACTIVE_ACCOUNT_USERNAME

        };

        final Cursor cursorProviders = getContentResolver().query(uri, PROVIDER_PROJECTION,
                Imps.Provider.ACTIVE_ACCOUNT_ID + "=" + accountId
                        + " AND " + Imps.Provider.CATEGORY + "=?"
                        + " AND " + Imps.Provider.ACTIVE_ACCOUNT_USERNAME + " NOT NULL" /* selection */,
                new String[]{ImApp.IMPS_CATEGORY} /* selection args */,
                Imps.Provider.DEFAULT_SORT_ORDER);

        if (cursorProviders != null && cursorProviders.getCount() > 0) {
            cursorProviders.moveToFirst();
            mDefaultProviderId = cursorProviders.getLong(0);
            mDefaultAccountId = cursorProviders.getLong(1);
            mDefaultUsername = cursorProviders.getString(2);
            mDefaultNickname = cursorProviders.getString(3);

            settings.edit().putLong("defaultAccountId", mDefaultAccountId).commit();

            Cursor pCursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(mDefaultProviderId)}, null);

            Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                    pCursor, getContentResolver(), mDefaultProviderId, false /* don't keep updated */, null /* no handler */);

            mDefaultUsername = mDefaultUsername + '@' + settings.getDomain();
            mDefaultOtrFingerprint = OtrAndroidKeyManagerImpl.getInstance(this).getLocalFingerprint(mDefaultUsername);

            settings.close();
            cursorProviders.close();

            return true;
        }


        if (cursorProviders != null)
            cursorProviders.close();

        return false;
    }

    public boolean initAccountInfo() {

        long lastAccountId = settings.getLong("defaultAccountId", -1);

        if (mDefaultProviderId == -1 || mDefaultAccountId == -1) {

            final Uri uri = Imps.Provider.CONTENT_URI_WITH_ACCOUNT;
            String[] PROVIDER_PROJECTION = {
                    Imps.Provider._ID,
                    Imps.Provider.ACTIVE_ACCOUNT_ID,
                    Imps.Provider.ACTIVE_ACCOUNT_USERNAME,
                    Imps.Provider.ACTIVE_ACCOUNT_NICKNAME,
                    Imps.Provider.ACTIVE_ACCOUNT_KEEP_SIGNED_IN
            };

            final Cursor cursorProviders = getContentResolver().query(uri, PROVIDER_PROJECTION,
                    Imps.Provider.CATEGORY + "=?" + " AND " + Imps.Provider.ACTIVE_ACCOUNT_USERNAME + " NOT NULL" /* selection */,
                    new String[]{ImApp.IMPS_CATEGORY} /* selection args */,
                    Imps.Provider.DEFAULT_SORT_ORDER);

            if (cursorProviders != null && cursorProviders.getCount() > 0) {
                while (cursorProviders.moveToNext()) {

                    long providerId = cursorProviders.getLong(0);
                    long accountId = cursorProviders.getLong(1);
                    String username = cursorProviders.getString(2);
                    String nickname = cursorProviders.getString(3);
                    boolean keepSignedIn = cursorProviders.getInt(4) != 0;

                    Cursor pCursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(providerId)}, null);

                    Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                            pCursor, getContentResolver(), providerId, false /* don't keep updated */, null /* no handler */);

                    username = username + '@' + settings.getDomain();
                    String otrFingerprint = OtrAndroidKeyManagerImpl.getInstance(this).getLocalFingerprint(username);

                    if ((!mNeedsAccountUpgrade)
                            && settings.getDomain().equalsIgnoreCase("dukgo.com") && keepSignedIn) {
                        mNeedsAccountUpgrade = true;
                    }

                    settings.close();

                    if (lastAccountId == -1 && keepSignedIn) {
                        mDefaultProviderId = providerId;
                        mDefaultAccountId = accountId;
                        mDefaultUsername = username;
                        mDefaultNickname = nickname;
                        mDefaultOtrFingerprint = otrFingerprint;
                    } else if (lastAccountId == accountId) {
                        mDefaultProviderId = providerId;
                        mDefaultAccountId = accountId;
                        mDefaultUsername = username;
                        mDefaultNickname = nickname;
                        mDefaultOtrFingerprint = otrFingerprint;
                    } else if (mDefaultProviderId == -1) {
                        mDefaultProviderId = providerId;
                        mDefaultAccountId = accountId;
                        mDefaultUsername = username;
                        mDefaultNickname = nickname;
                        mDefaultOtrFingerprint = otrFingerprint;
                    }

                }
            }

            if (cursorProviders != null)
                cursorProviders.close();
        }

        return true;

    }

    public boolean checkUpgrade() {

        final Uri uri = Imps.Provider.CONTENT_URI_WITH_ACCOUNT;
        String[] PROVIDER_PROJECTION = {
                Imps.Provider._ID,
                Imps.Provider.ACTIVE_ACCOUNT_ID,
                Imps.Provider.ACTIVE_ACCOUNT_USERNAME,
                Imps.Provider.ACTIVE_ACCOUNT_NICKNAME,
                Imps.Provider.ACTIVE_ACCOUNT_KEEP_SIGNED_IN
        };

        final Cursor cursorProviders = getContentResolver().query(uri, PROVIDER_PROJECTION,
                Imps.Provider.CATEGORY + "=?" + " AND " + Imps.Provider.ACTIVE_ACCOUNT_USERNAME + " NOT NULL" /* selection */,
                new String[]{ImApp.IMPS_CATEGORY} /* selection args */,
                Imps.Provider.DEFAULT_SORT_ORDER);

        if (cursorProviders != null && cursorProviders.getCount() > 0) {
            while (cursorProviders.moveToNext()) {

                long providerId = cursorProviders.getLong(0);
                long accountId = cursorProviders.getLong(1);
                String username = cursorProviders.getString(2);
                String nickname = cursorProviders.getString(3);
                boolean keepSignedIn = cursorProviders.getInt(4) != 0;

                Cursor pCursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(providerId)}, null);

                Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                        pCursor, getContentResolver(), providerId, false /* don't keep updated */, null /* no handler */);

                username = username + '@' + settings.getDomain();
                String otrFingerprint = OtrAndroidKeyManagerImpl.getInstance(this).getLocalFingerprint(username);

                if ((!mNeedsAccountUpgrade)
                        && settings.getDomain().equalsIgnoreCase("dukgo.com") && keepSignedIn) {
                    mNeedsAccountUpgrade = true;
                }

                settings.close();

            }
        }

        if (cursorProviders != null)
            cursorProviders.close();


        return true;

    }

    private long mDefaultProviderId = -1;
    private long mDefaultAccountId = -1;
    private String mDefaultUsername = null;
    private String mDefaultOtrFingerprint = null;
    private String mDefaultNickname = null;

    public String getDefaultUsername() {
        return mDefaultUsername;
    }

    public String getDefaultNickname() {
        return mDefaultNickname;
    }

    public String getDefaultOtrKey() {
        return mDefaultOtrFingerprint;
    }

    public long getDefaultProviderId() {
        return mDefaultProviderId;
    }

    public long getDefaultAccountId() {
        return mDefaultAccountId;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        Languages.setLanguage(this, Preferences.getLanguage(), true);

    }


    @Override
    public void onCacheWordUninitialized() {
        // unused
    }

    @Override
    public void onCacheWordLocked() {
        // unused
    }

    @Override
    public void onCacheWordOpened() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "Awaiting ImpsProvider ready");
                // Wait for ImpsProvider to initialize : it listens to onCacheWordOpened as well...
                ImpsProvider.awaitDataReady();

                Log.d(LOG_TAG, "ImpsProvider ready");
                // setupChatSecurePush will disconnect the CacheWordHandler when it's done
            }
        }).start();
    }

    public boolean needsAccountUpgrade() {
        return mNeedsAccountUpgrade;
    }

    public void notifyAccountUpgrade() {
        StatusBarNotifier notifier = new StatusBarNotifier(this);

    }

    public boolean doUpgrade(Activity activity, String newDomain, MigrateAccountTask.MigrateAccountListener listener) {

        long lastAccountId = settings.getLong("defaultAccountId", -1);

        final Uri uri = Imps.Provider.CONTENT_URI_WITH_ACCOUNT;
        String[] PROVIDER_PROJECTION = {
                Imps.Provider._ID,
                Imps.Provider.ACTIVE_ACCOUNT_ID,
                Imps.Provider.ACTIVE_ACCOUNT_USERNAME,
                Imps.Provider.ACTIVE_ACCOUNT_NICKNAME,
                Imps.Provider.ACTIVE_ACCOUNT_KEEP_SIGNED_IN
        };

        final Cursor cursorProviders = getContentResolver().query(uri, PROVIDER_PROJECTION,
                Imps.Provider.CATEGORY + "=?" + " AND " + Imps.Provider.ACTIVE_ACCOUNT_USERNAME + " NOT NULL" /* selection */,
                new String[]{ImApp.IMPS_CATEGORY} /* selection args */,
                Imps.Provider.DEFAULT_SORT_ORDER);

        if (cursorProviders != null && cursorProviders.getCount() > 0) {
            while (cursorProviders.moveToNext()) {

                long providerId = cursorProviders.getLong(0);
                long accountId = cursorProviders.getLong(1);
                String username = cursorProviders.getString(2);
                String nickname = cursorProviders.getString(3);
                boolean keepSignedIn = cursorProviders.getInt(4) != 0;

                Cursor pCursor = getContentResolver().query(Imps.ProviderSettings.CONTENT_URI, new String[]{Imps.ProviderSettings.NAME, Imps.ProviderSettings.VALUE}, Imps.ProviderSettings.PROVIDER + "=?", new String[]{Long.toString(providerId)}, null);

                Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                        pCursor, getContentResolver(), providerId, false /* don't keep updated */, null /* no handler */);

                username = username + '@' + settings.getDomain();

                if (settings.getDomain().equalsIgnoreCase("dukgo.com") && keepSignedIn) {
                    new MigrateAccountTask(activity, this, providerId, accountId, listener).execute(newDomain);
                }

                settings.close();

            }
        }

        if (cursorProviders != null)
            cursorProviders.close();

        mNeedsAccountUpgrade = false;

        return false;
    }

    /*
    * Auto approved contact in list which were loaded from Xmpp server
    * */
    public static void approveSubscription(final Contact contact) {
        long providerId = sImApp.getDefaultProviderId();
        long accountId = sImApp.getDefaultAccountId();
        if (providerId != -1 && accountId != -1) {
            final IImConnection mConn = getConnection(sImApp.getDefaultProviderId(), sImApp.getDefaultAccountId());
            if (mConn != null) {
                try {
                    if (mConn.getState() == ImConnection.LOGGED_IN) {
                        String bareAddress = contact.getAddress().getBareAddress();
                        String nickname = getNickname(bareAddress);
                        if (!TextUtils.isEmpty(nickname) && !bareAddress.contains(nickname.toLowerCase())) {
                            IContactListManager manager = mConn.getContactListManager();
                            manager.approveSubscription(contact);
                        } else {
                            RestAPI.GetDataWrappy(sImApp, RestAPI.getMemberByIdUrl(new XmppAddress(contact.getAddress().getBareAddress()).getUser()), new RestAPIListener() {
                                @Override
                                public void OnComplete(String s) {
                                    try {
                                        IContactListManager manager = mConn.getContactListManager();
                                        if (TextUtils.isEmpty(s)) {
                                            //Remove contact not exist in DB
                                            removeContact(sImApp.getContentResolver(), contact.getAddress().getBareAddress(), mConn);
                                        } else {
                                            updateContact(contact.getAddress().getBareAddress(), (WpKMemberDto) new Gson().fromJson(s, WpKMemberDto.getType()), mConn);
                                            manager.approveSubscription(contact);
                                        }
                                    } catch (RemoteException e1) {
                                        removeContact(sImApp.getContentResolver(), contact.getAddress().getBareAddress(), mConn);
                                        e1.printStackTrace();
                                    }
                                }

                                @Override
                                protected void onError(int errorCode) {
                                    removeContact(sImApp.getContentResolver(), contact.getAddress().getBareAddress(), mConn);
                                }
                            });
                        }
                    }
                } catch (RemoteException e) {
                    LogCleaner.error(ImApp.LOG_TAG, "approve sub error", e);
                    removeContact(sImApp.getContentResolver(), contact.getAddress().getBareAddress(), mConn);
                }
            }
        }
    }


    public static void removeContact(ContentResolver resolver, String address, IImConnection mConn) {
        String selection = Imps.Contacts.USERNAME + "='" + address + "'";
        Uri.Builder builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
        resolver.delete(builder.build(), selection, null);
        try {
            mConn.getContactListManager().removeContact(address);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
    * Auto update contact name in list which were loaded from Xmpp server
    * */
    public static void updateContact(String address, WpKMemberDto wpKMemberDto, IImConnection mConn) {
        updateContact(null, address, wpKMemberDto, mConn);
    }

    public static void updateContact(ContentValues originValues, String address, WpKMemberDto wpKMemberDto, IImConnection mConn) {
        // update the server
        if (sImApp != null && wpKMemberDto != null && mConn != null) {
            String correctAddress = address.toLowerCase();
            String name = wpKMemberDto.getIdentifier();
            String email = wpKMemberDto.getEmail();
            String fullname = wpKMemberDto.getGiven();
            Uri.Builder builder = Imps.Contacts.CONTENT_URI_CONTACTS_BY.buildUpon();
            ContentUris.appendId(builder, sImApp.getDefaultProviderId());
            ContentUris.appendId(builder, sImApp.getDefaultAccountId());
            // update locally
            String selection = Imps.Contacts.USERNAME + "=?";
            String[] selectionArgs = {correctAddress};
            ContentValues values = new ContentValues();
            if (originValues != null) {
                values = originValues;
            }
            if (!TextUtils.isEmpty(email)) {
                values.put(Imps.Contacts.CONTACT_EMAIL, email);
            }
            if (TextUtils.isEmpty(name)) {
                name = new XmppAddress(correctAddress).getUser();
            }
            values.put(Imps.Contacts.NICKNAME,fullname);

            if (!values.containsKey(Imps.Contacts.TYPE)) {
                values.put(Imps.Contacts.TYPE, Imps.ContactsColumns.TYPE_NORMAL);
            }

            Uri queryUri = builder.build();
            Cursor cursor = sImApp.getContentResolver().query(queryUri, new String[]{Imps.Contacts._ID},
                    selection, selectionArgs, null);
            boolean isUpdated = false;

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    long contactId = cursor.getLong(0);
                    Uri uri = ContentUris.withAppendedId(Imps.Contacts.CONTENT_URI, contactId);
                    isUpdated = sImApp.getContentResolver().update(uri, values, null, null) > 0;
                } else {
                    sImApp.getContentResolver().delete(queryUri, selection, selectionArgs);
                }
                cursor.close();
            }
            if (!isUpdated) {
                values.put(Imps.Contacts.USERNAME, correctAddress);
                sImApp.getContentResolver().insert(builder.build(), values);
            }
            String avatar = wpKMemberDto.getAvatar()!=null? wpKMemberDto.getAvatar().getReference() : "";
            String banner = wpKMemberDto.getBanner()!=null? wpKMemberDto.getBanner().getReference() : "";
            Imps.Avatars.updateAvatarBannerToDB(correctAddress, avatar, banner);
        }
    }

    public static String getEmail(String username) {
        String email = "";
        if (username.equals(sImApp.getDefaultUsername())) {
            email = Imps.Account.getString(sImApp.getContentResolver(), Imps.Account.ACCOUNT_EMAIL, ImApp.sImApp.getDefaultAccountId());
        } else {
            email = Imps.Contacts.getString(sImApp.getContentResolver(), Imps.Contacts.CONTACT_EMAIL, username);
        }
        return email;
    }

    public static String getNickname(String address) {
        String nickname = "";
        if (address.equals(sImApp.getDefaultUsername())) {
            nickname = Imps.Account.getString(sImApp.getContentResolver(), Imps.Account.ACCOUNT_NAME, ImApp.sImApp.getDefaultAccountId());
        } else {
            nickname = Imps.Contacts.getString(sImApp.getContentResolver(), Imps.Contacts.NICKNAME, address);
        }
        if (TextUtils.isEmpty(nickname)) {
            nickname = new XmppAddress(address).getUser();
        }
        return nickname;
    }

    public static void broadcastIdentity(String indentity) {
        IImConnection connection = ImApp.getConnection(ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultAccountId());
        if (connection != null) {
            try {
                if (connection.getState() == ImConnection.LOGGED_IN) {
                    connection.broadcastMigrationIdentity(indentity);
                }
            } catch (RemoteException ex) {
                LogCleaner.error(ImApp.LOG_TAG, "approve sub error", ex);
            }
        }
    }

    public CacheWordHandler getCacheWord() {
        return mCacheWord;
    }
}
