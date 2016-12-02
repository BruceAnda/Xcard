package com.lovearthstudio.xcard;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by zhaoliang on 16/4/6.
 * 这个adapter是整个recycleview的list的adapter.
 * fixme: 未来应该给它一个优秀的名字:CardAdapter
 */
public class CardAdapter extends RecyclerView.Adapter<CardHolder> {

    private String TAG = CardAdapter.class.getName();
    public Context mContext;

    public JSONArray jsonArray;

    /**
     * 整个列表的最小文章id，这个id不包括插入的辅助card的id
     */
    public long minTid;
    /**
     * 整个列表的最大文章id，这个id不包括插入的辅助card的id
     */
    public long maxTid;
    /**
     * jsonArray容纳的文章数目
     */
    public int mArtLimit = 20;

    SensorManager sensorManager;

    public XRecyclerView xRecyclerView;

    public CardAdapter(Context context, JSONArray jsonArray) {
        this.mContext = context;
        this.jsonArray = jsonArray;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /*这个函数是XRcycleView触发的,当它发现holder缓冲池里数目<4(在本项目初始)的时候,就会去创建新的viewholder,如果超过了,它就去缓冲池里获取旧的*/
    @Override
    public CardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("RecyclerView", "onCreateViewHolder" + viewType);
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return mHolderCallBack.createViewHolder(inflater, parent, viewType);
    }

    /*这个函数是XRcycleView触发的,它自动会把创建的或者回收的holder对象绑定 adapter里jsonarray第position的数据*/

    @Override
    public void onBindViewHolder(CardHolder holder, int position) {
        try {
            Log.e("RecyclerView", "onBindViewHolder:" + holder.getClass().getName() + "\n" + jsonArray.get(position).toString() + position);
            holder.bindData(jsonArray.optJSONObject(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }


    /*这个是baseAdapter的成员函数,专门用来做多种视图的,这个position是内容array的索引
    * recycleView的回收机制就依赖于这个viewtype.保证你需要图片的时候给图片控件,需要视频播放的时候给视频播放控件.
    * */
    @Override
    public int getItemViewType(int position) {
        JSONObject jsonObject = jsonArray.optJSONObject(position);
        int type = jsonObject.optInt("vtype");
        return type;

    }

    /**
     * 重新计算数据的最大值和最小值，注意，0是忽略的
     */
    private void updateMinMax() {
        /**
         * 接下来，遍历整个数组，求最小值和最大值
         * 如果出现Inc违例，就要报出来
         * */
        long new_inc_min = Long.MAX_VALUE;
        long new_inc_max = 0;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                //fixme: 这个地方是jsonObject
                JSONObject joItem = (JSONObject) jsonArray.get(i);
                long inc = joItem.optInt("inc");
                if (inc == 0) continue;
                if (inc > new_inc_max)
                    new_inc_max = inc;
                if (inc < new_inc_min)
                    new_inc_min = inc;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        minTid = new_inc_min;
        maxTid = new_inc_max;
    }


    /**
     * 把数据加载在jsonArray里
     **/

    public void loadData(JSONArray articles) {
        try {
            jsonArray = articles;
            maxTid = ((JSONObject) jsonArray.get(0)).optLong("inc");
            minTid = ((JSONObject) jsonArray.get(jsonArray.length() - 1)).optLong("inc");
            Log.i(TAG, "xcard load.load article count " + articles.length() + " to " + jsonArray.length() + "articles with tid [" + minTid + "," + maxTid + "]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateMinMax();
        notifyDataSetChanged();
    }


    /**
     * 把数据加载在jsonArray队头.
     **/

    public void pullData(JSONArray articles) {
        Log.e(TAG, "$$$$ pullData" + articles.length() + " !");
        try {
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (articles.length() < mArtLimit) {
                        articles.put(jsonArray.get(i));
                    } else {
                        break;
                    }
                }
            }
            jsonArray = articles;
            maxTid = ((JSONObject) jsonArray.get(0)).optLong("inc");
            minTid = ((JSONObject) jsonArray.get(jsonArray.length() - 1)).optLong("inc");

            Log.i(TAG, "xcard pull.load article count " + articles.length() + " to " + jsonArray.length() + "articles with tid [" + minTid + "," + maxTid + "]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateMinMax();

        notifyDataSetChanged();
    }

    /**
     * 把数据加载在jsonArray结尾
     * 代码到这里一定保证articles里有值，部位空
     **/

    public void pushData(JSONArray articles) {
        try {
            for (int i = 0; i < articles.length(); i++) {
                jsonArray.put(articles.get(i));
                if (jsonArray.length() > mArtLimit) {
                    jsonArray.remove(0);
                }
            }
            maxTid = ((JSONObject) jsonArray.get(0)).optLong("inc");
            minTid = ((JSONObject) jsonArray.get(jsonArray.length() - 1)).optLong("inc");

            Log.i(TAG, "xcard push.load article count " + articles.length() + " to " + jsonArray.length() + "articles with tid [" + minTid + "," + maxTid + "]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateMinMax();

        notifyDataSetChanged();
        int new_position = jsonArray.length() - articles.length();
        if (new_position < 0)
            new_position = 0;
        /**
         * 这个地方需要注意，如果用xRecyclerView.smoothScrollToPosition(new_position),
         * 这个scroll的过程是有动画滑动效果的.这个效果会让人眼花缭乱
         * 用xRecyclerView.scrollToPosition就会自动scroll到new_position的那个card置顶
         * 我们先scrolltoPosition,然后再scrollby一个偏移。
         * 这样能精确地呈现出无缝滑动的效果。
         * 参考：http://blog.csdn.net/tyzlmjj/article/details/49227601
         * 参考：http://www.loongwind.com/archives/189.html
         *
         * */
        //xRecyclerView.smoothScrollToPosition(new_position);
        RecyclerView.LayoutManager layoutManager = xRecyclerView.getLayoutManager();
        int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        int top = xRecyclerView.getChildAt(firstVisibleItemPosition).getTop();
        xRecyclerView.scrollToPosition(new_position);
        xRecyclerView.scrollBy(0, top);
    }

    private HolderCallBack mHolderCallBack;

    public void setHolderCallBack(HolderCallBack holderCallBack) {
        this.mHolderCallBack = holderCallBack;
    }

    public interface HolderCallBack {

        CardHolder createViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);
    }
}
