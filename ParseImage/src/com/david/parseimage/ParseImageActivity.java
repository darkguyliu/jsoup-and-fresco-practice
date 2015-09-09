package com.david.parseimage;

import java.util.ArrayList;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;

public class ParseImageActivity extends AppCompatActivity {
    private static final String LOG_TAG = "ParseImageActivity";
    private GridView mGridView;
    public final static int PORTRAIT_GRID_NUM = 3;
    public final static int LANDSCAPE_GRID_NUM = 5;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private GridAdapter mGridViewAdapter;
    private String mReceivedText = null;
    private Uri mUri;
    private RunnableManager mManager;
    private ArrayList<String> mList = new ArrayList<String>();
    private ProgressDialog mDialog = null;
    private LinearLayout mEmptyView;

    private CrawlImagesRunnable.CrawlImagesCallback mImagesCallback = new CrawlImagesRunnable.CrawlImagesCallback() {

        @Override
        public void onCrawlingCompleted(ArrayList<String> imageslist) {
            // TODO Auto-generated method stub
            for ( int i = 0; i < imageslist.size(); i++ ) {
                Log.d(LOG_TAG, "david image=" + imageslist.get(i));
            }
            mList.addAll(imageslist);
            ParseImageActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if ( mDialog != null && mDialog.isShowing() ) {
                        mDialog.dismiss();
                    }
                    if ( mList != null && mList.size() > 0 ) {
                        mGridViewAdapter.setItemsData(mList);
                        mGridViewAdapter.notifyDataSetChanged();
                    } else {
                        if ( mEmptyView != null ) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                        if ( mGridView != null ) {
                            mGridView.setVisibility(View.GONE);
                        }
                    }
                }
            });

        }

        @Override
        public void onCrawlingFailed(String Url, int errorCode) {
            // TODO Auto-generated method stub
            ParseImageActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if ( mDialog != null && mDialog.isShowing() ) {
                        mDialog.dismiss();
                    }
                    if ( mEmptyView != null ) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                    if ( mGridView != null ) {
                        mGridView.setVisibility(View.GONE);
                    }
                }
            });
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse_image);
        mGridView = (GridView) findViewById(R.id.gridview);
        mEmptyView = (LinearLayout) findViewById(R.id.empty_container);
        mGridViewAdapter = new GridAdapter(this, R.layout.griditem, mGridView);
        mGridView.setAdapter(mGridViewAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
                // TODO Auto-generated method stub

            }

        });
        girdConfigurationChange(getResources().getConfiguration());
        Intent receivedIntent = this.getIntent();
        if ( receivedIntent != null
            && Intent.ACTION_SEND.equals(getIntent().getAction()) ) {
            String type = receivedIntent.getType();
            if ( !TextUtils.isEmpty(type) && type.startsWith("text/") ) {
                mReceivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        Log.d(LOG_TAG, "david mReceivedText=" + mReceivedText);
        mUri = Uri.parse(mReceivedText);
        mUri.normalizeScheme();
        Log.d(LOG_TAG, "david mUri.isAbsolute()=" + mUri.isAbsolute()
            + ",mUri.isHierarchical()=" + mUri.isHierarchical()
            + ",mUri.isRelative()=" + mUri.isRelative() + ",mUri.toString()="
            + mUri.toString());
        mManager = new RunnableManager();
        if ( mManager != null && !mManager.isShuttingDown() ) {
            mDialog = ProgressDialog.show(this, "Please wait", "Parsing Images");
            CrawlImagesRunnable mTask = new CrawlImagesRunnable(
                mImagesCallback,
                mUri.toString());
            mManager.addToCrawlingQueue(mTask);

        } else {
            mGridView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.parse_image, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if ( mManager != null ) {
            mManager.cancelAllRunnable();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if ( id == R.id.action_settings ) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        girdConfigurationChange(configuration);
        super.onConfigurationChanged(configuration);
    }

    /**
     * <b>The number of grids in a column</b> (Follow RE_UI_Guideline)
     * <p>
     * 1. <b>Portrait</b>: 3 grids</br> 2. <b>Landscape</b>: If screen width is
     * not enough to put completely grid, move it to the second row. (The grid
     * size in landscape mode is the same as in portrait mode)
     */
    public void girdConfigurationChange(Configuration configuration) {
        Log.d(LOG_TAG, "girdConfigurationChange");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int columns = PORTRAIT_GRID_NUM;
        int baseWidth = Math.min(size.x, size.y);
        int paddingLeft = ( mGridView != null ) ? mGridView.getPaddingLeft()
            : 0;
        int paddingRight = ( mGridView != null ) ? mGridView.getPaddingRight()
            : 0;
        int gap = getResources().getDimensionPixelSize(
            R.dimen.grid_view_spacing);
        int columnWidth = ( baseWidth - ( paddingRight + paddingRight ) - ( ( PORTRAIT_GRID_NUM - 1 ) * gap ) )
            / PORTRAIT_GRID_NUM;

        if ( configuration != null ) {
            int orientation = configuration.orientation;
            mOrientation = configuration.orientation;
            if ( orientation == Configuration.ORIENTATION_LANDSCAPE ) {
                int landscapeWidth = Math.max(size.x, size.y);
                if ( columnWidth > 0 ) {
                    columns = ( landscapeWidth - ( paddingLeft + paddingRight ) - ( ( PORTRAIT_GRID_NUM - 1 ) * gap ) )
                        / columnWidth;
                }

                // Error Handle
                if ( columnWidth <= 0 ) {
                    Log.w(LOG_TAG, "get Landscape error!");
                    columns = LANDSCAPE_GRID_NUM;
                    columnWidth = ( landscapeWidth
                        - ( paddingLeft + paddingRight ) - ( ( columns - 1 ) * gap ) )
                        / columns;
                }
                Log.d(LOG_TAG, "[LANDSCAPE] size = " + columnWidth
                    + ", columns = " + columns);
            } else {
                Log.d(LOG_TAG, "[" + orientation + "] size = " + columnWidth
                    + ", columns = " + columns);
            }
        }

        if ( mGridView != null ) {
            mGridView.setNumColumns(columns);
            int spacing = getResources().getDimensionPixelSize(
                R.dimen.grid_view_spacing);
            mGridView.setHorizontalSpacing(spacing);
            mGridView.setVerticalSpacing(spacing);
            mGridView.setColumnWidth(columnWidth);
            if ( mGridViewAdapter != null ) {
                mGridViewAdapter.setColumnWidth(columnWidth);
            }
        }

    }
}
