package net.wrappy.im.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import net.wrappy.im.provider.Imps;
import net.wrappy.im.ui.ContactListItem;

/**
 * Created by ben on 11/12/2017.
 */

public class ContactsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    String mSearchString;
    boolean mShowGroups;
    ContactsLoaderDelegate delegate;
    Context context;
    public interface ContactsLoaderDelegate {
        void onCompleteContactsLoader(Cursor newCursor);
    }

    public ContactsLoaderCallbacks(Context context, String mSearchString, boolean mShowGroups, ContactsLoaderDelegate delegate) {
        this.context = context;
        this.mSearchString = mSearchString;
        this.mShowGroups = mShowGroups;
        this.delegate = delegate;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        StringBuilder buf = new StringBuilder();

        if (mSearchString != null) {
            buf.append('(');
            buf.append(Imps.Contacts.NICKNAME);
            buf.append(" LIKE ");
            DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
            buf.append(" OR ");
            buf.append(Imps.Contacts.USERNAME);
            buf.append(" LIKE ");
            DatabaseUtils.appendValueToSql(buf, "%" + mSearchString + "%");
            buf.append(')');
            buf.append(" AND ");
        }

        buf.append('(');
        buf.append(Imps.Contacts.TYPE).append('=').append(Imps.Contacts.TYPE_NORMAL);

        if (mShowGroups) {
            buf.append(" OR ");
            buf.append(Imps.Contacts.TYPE).append('=').append(Imps.Contacts.TYPE_GROUP);
        }

        buf.append(')');

        buf.append(" AND ");
        buf.append('(');
        buf.append(Imps.Contacts.SUBSCRIPTION_TYPE).append("==").append(Imps.Contacts.SUBSCRIPTION_TYPE_BOTH);
        buf.append(" OR ");
        buf.append(Imps.Contacts.SUBSCRIPTION_TYPE).append("==").append(Imps.Contacts.SUBSCRIPTION_TYPE_TO);
        buf.append(')');

        CursorLoader loader = new CursorLoader(context, Imps.Contacts.CONTENT_URI, ContactListItem.CONTACT_PROJECTION,
                buf == null ? null : buf.toString(), null, Imps.Contacts.MODE_AND_ALPHA_SORT_ORDER);
        //    loader.setUpdateThrottle(50L);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
        delegate.onCompleteContactsLoader(newCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        delegate.onCompleteContactsLoader(null);
    }

}
