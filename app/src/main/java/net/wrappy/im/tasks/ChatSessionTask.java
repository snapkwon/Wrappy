package net.wrappy.im.tasks;

import android.os.AsyncTask;
import android.os.RemoteException;

import net.wrappy.im.ImApp;
import net.wrappy.im.service.IChatSession;
import net.wrappy.im.service.IChatSessionManager;
import net.wrappy.im.service.IImConnection;

/**
 * Created by Khoa.Nguyen on 10/23/15.
 */
public class ChatSessionTask extends AsyncTask<String, Long, String> {

    private static final int DELETE = 0;
    private static final int LEAVE = 1;
    private int type = 0;

    public ChatSessionTask leave() {
        this.type = LEAVE;
        return this;
    }

    @Override
    protected String doInBackground(String... params) {
        String address = params[0];
        try {
            IImConnection connection = ImApp.getConnection(ImApp.sImApp.getDefaultProviderId(), ImApp.sImApp.getDefaultProviderId());
            IChatSessionManager manager;
            if (connection != null) {
                manager = connection.getChatSessionManager();
                IChatSession session = manager.getChatSession(address);
                if (session == null)
                    session = manager.createChatSession(address, true);

                if (session != null) {
                    if (type == DELETE) {
                        session.delete();
                    } else if (type == LEAVE) {
                        session.leave();
                    }
                }

            }
            return null;

        } catch (RemoteException e) {
            return e.toString();
        }
    }
}
