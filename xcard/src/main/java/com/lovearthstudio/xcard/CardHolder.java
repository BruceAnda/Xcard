package com.lovearthstudio.xcard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by zhaoliang on 16/4/6.
 */
public abstract class CardHolder extends RecyclerView.ViewHolder implements RecyclerView.OnChildAttachStateChangeListener {

    protected Context mContext;

    protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy年-MM月dd日-HH时mm分ss秒");


    public CardHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
    }

    @Override
    public void onChildViewAttachedToWindow(View view) {

    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {

    }

    public abstract void bindData(JSONObject jo);

}
