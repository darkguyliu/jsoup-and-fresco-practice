package com.david.parseimage;

import java.util.ArrayList;

import com.facebook.drawee.view.SimpleDraweeView;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private Activity mActivity;
    private GridView mGridView;
    private int mItemResId = 0;
    private LayoutInflater mInflater;
    private int mColumnWidth = 0;
    private ArrayList<String> mList = new ArrayList<String>();

    public GridAdapter(Activity activity, int itemResId, GridView gridView) {
        mActivity = activity;
        mContext = mActivity.getBaseContext();
        mGridView = gridView;
        init(itemResId);
    }

    private void init(int itemResId) {
        this.mItemResId = itemResId;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setColumnWidth(int width) {
        mColumnWidth = width;
    }

    private class GridItemHolder {
        SimpleDraweeView image;
        View mask;
        RelativeLayout grid_wrapper;
        int thumbnailIndex;
        String filePath;
    }

    public void setItemsData(ArrayList<String> list) {
        mList.addAll(list);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if ( mList != null ) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public String getItem(int position) {
        // TODO Auto-generated method stub
        if ( mList != null && mList.size() > position ) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        GridItemHolder holder = null;

        if ( convertView == null
            || ! ( convertView.getTag() instanceof GridItemHolder ) ) {
            holder = new GridItemHolder();
            convertView = mInflater.inflate(mItemResId, null);
            RelativeLayout grid_wrapper = (RelativeLayout) convertView.findViewById(R.id.griditem_layout);
            // ImageView image = (ImageView)
            // convertView.findViewById(R.id.image);
            SimpleDraweeView image = (SimpleDraweeView) convertView.findViewById(R.id.image);

            View mask = convertView.findViewById(R.id.mask);

            holder.image = image;
            holder.grid_wrapper = grid_wrapper;
            holder.mask = mask;
            convertView.setTag(holder);

        } else {
            holder = (GridItemHolder) convertView.getTag();
        }
        holder.grid_wrapper.setLayoutParams(new AbsListView.LayoutParams(
            mColumnWidth,
            mColumnWidth));
        holder.mask.setVisibility(View.GONE);
        holder.image.setBackgroundColor(mContext.getResources()
            .getColor(R.color.thumbnail_default_background));
        // holder.image.setImageResource(R.drawable.re_default_content_s);
        holder.image.setScaleType(ScaleType.CENTER);

        String url = getItem(position);
        Log.d("david", "david url=" + url);
        
        if ( !TextUtils.isEmpty(url) ) {
            Uri uri = Uri.parse(url);

            Uri uri0 = Uri.parse("http://download.mobile01.com/mobile/image/news/headline/a6fb3c9bf28b96569efe1222e7bf7de8.jpg");
            Uri uri1 = Uri.parse("http://download.mobile01.com/mobile/image/news/headline/e8ef875e47955112a9f2a2443c069701.jpg");
            /*
            if(position%2 == 0){
                holder.image.setImageURI(uri0);

            }else{
                holder.image.setImageURI(uri1);

            }
            */
            holder.image.setImageURI(uri);

        
        }
        return convertView;
    }
}
