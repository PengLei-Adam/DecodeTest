package com.example.decodetest.ui.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.decodetest.R;
import com.example.decodetest.media.MediaInfo;
import com.example.decodetest.provider.MediaProvider;

import java.text.DecimalFormat;


/**
 * Created by leip on 2016/7/4.
 */
public abstract class MediaLibraryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MediaLibraryFragment";

    protected static final String URI_KEY = "CONTENT_URI";

    //Two instances for two pages
    public static PhotoLibraryFragment photoFragment;
    public static VideoLibraryFragment videoFragment;//TODO: change type
    
    protected SimpleCursorAdapter cursorAdapter;

    protected ContentResolver cr;

    protected abstract void scanMedia();
    protected abstract void refreshList();

    //method for FragmentPagerAdapter
    public static MediaLibraryFragment newInstance(int page, String title){
        
        Bundle args = new Bundle();

        switch(page){
            case 0:
                if(photoFragment == null)
                    photoFragment = new PhotoLibraryFragment();
                args.putString("FIRST_TITLE", title);

                photoFragment.setArguments(args);
                return photoFragment;
            case 1:
                if(videoFragment == null)
                    videoFragment = new VideoLibraryFragment();
                args.putString("FIRST_TITLE", title);

                videoFragment.setArguments(args);
                return videoFragment;
            default:
                return null;
        }
        
    }


    protected void setAdapterLayout(int layoutId){
        cursorAdapter = new MediaCursorAdapter(getActivity(),
                layoutId, null,
                new String[] {MediaProvider.KEY_FILENAME, MediaProvider.KEY_SIZE,
                        MediaProvider.KEY_WIDTH, MediaProvider.KEY_HEIGHT},
                new int[] {R.id.mediaName, R.id.mediaSize, R.id.mediaWidth, R.id.mediaHeight}
                , 0);

        setListAdapter(cursorAdapter);

        cr = getActivity().getContentResolver();
    }



    protected void addNewItem(Uri uri, MediaInfo _media){

        String where = MediaProvider.KEY_FILENAME + "=\"" + _media.getFileName() + "\"";

        Cursor cursor = cr.query(uri, null, where, null, null);

        if(cursor == null) return;

        if(cursor.getCount() == 0){
            ContentValues values = new ContentValues();

            values.put(MediaProvider.KEY_FILENAME, _media.getFileName());
            values.put(MediaProvider.KEY_CREATED_TIME, _media.getCreateTime().getTime());
            values.put(MediaProvider.KEY_SIZE, _media.getSize());
            values.put(MediaProvider.KEY_WIDTH, _media.getWidth());
            values.put(MediaProvider.KEY_HEIGHT, _media.getHeight());

            cr.insert(uri, values);
        }

        cursor.close();
    }

    protected String findFileNameFromView(View view, int id){
        TextView nameView = (TextView)view.findViewById(id);
        if(nameView != null){
            return nameView.getText().toString();
        }
        else
            return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        cursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        cursorAdapter.swapCursor(null);
    }

    private static class MediaCursorAdapter extends SimpleCursorAdapter{
        public MediaCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        private static final DecimalFormat formatKB = new DecimalFormat("#.##KB");
        private static final DecimalFormat formatMB = new DecimalFormat("#.##MB");
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView textSize = (TextView)view.findViewById(R.id.mediaSize);

            if(textSize != null){
                int size = Integer.parseInt(textSize.getText().toString());

                textSize.setText((size < 1 << 20) ? formatKB.format(size/1024f) : formatMB.format(size/1048576f));
            }
            return view;
        }
    }
}
