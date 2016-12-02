package com.lovearthstudio.xcard;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Fixme：Xcard类 完成RecycleView的初始化与添加到给定的布局上，以后可能在该类中扩展更多功能
 * 使用：
 *
 * @author zhaoliang 2016-7-29日 下午
 *         维护:qq2668645098
 */
public class Xcard extends XRecyclerView {
    private static final String TAG = Xcard.class.getName();
    public static final int CARDTYPE_NOTYPE = 3;

    private static final int UPDATE_DATA = 1;
    private static final int PULL = 2;
    private static final int PUSH = 3;
    private static final int DATASRC_ERROR = 4;
    private static final int NETWORK_ERROR = 5;
    private static final int SERVER_ERROR = 6;

    private static final int PULL_NOMORE = 8;
    private static final int PUSH_NOMORE = 9;
    private static final int LOAD_NOMORE = 10;
    private static final int HEAD_NOMORE = 11;
    private static final int FOOT_NOMORE = 12;

    private static final int AUTO_LODE = 110;
    private static final long DELAY_MILLIS = 5000;
    private static final int AUTO_PULL = 111;
    private static final int AUTO_PUSH = 112;
    private static final int LOAD_DATA = 10001;

    private boolean pull = false;
    private boolean push = false;

    /**
     * 整个页面的channel,mRid.
     */
    private String mName = "";
    private String mChannel = "";
    private String mInc = "";
    private String mDataSrc = "";
    private String mSort = "desc";//desc/asc
    private String mDefaultCard = "default";
    private int mLoadLimit = 20;
    private int mPullLimit = 10;
    private int mPushLimit = 10;
    private int mLoadInterval = -1;
    private int mPushInterval = -1;
    private int mPullInterval = -1;

    public long mRid = 0;
    /**
     * 设置发送数据的数据结构
     *
     * @param recyclerView
     */
    private CardAdapter mAdapter = null;
    private Map<String, Integer> mCards;

    public Xcard(Context context) {
        this(context, null);
    }

    public Xcard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setHolderCallBack(CardAdapter.HolderCallBack mHolderCallBack) {
        mAdapter.setHolderCallBack(mHolderCallBack);
    }

    private int getViewType(String typename) {
        if (mCards.containsKey(typename)) {
            return mCards.get(typename);
        } else {
            return CARDTYPE_NOTYPE;
        }
    }

    public Xcard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         *  创建自己的adapter
         * */
        mAdapter = new CardAdapter(getContext(), new JSONArray());
        setAdapter(mAdapter);
        mAdapter.xRecyclerView = this;
        /**
         *  不要动下面这一句话
         *  下面在attr中读出各个属性的值，注意，如果属性没有指明，那么读出来的变量时null
         * */
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.xcard);
        mName = ta.getString(R.styleable.xcard_name);
        mChannel = ta.getString(R.styleable.xcard_channel);
        mInc = ta.getString(R.styleable.xcard_incName);
        mDataSrc = ta.getString(R.styleable.xcard_dataSrc);
        mSort = ta.getString(R.styleable.xcard_sort);
        mLoadLimit = ta.getInt(R.styleable.xcard_loadLimit, 20);
        mPushLimit = ta.getInt(R.styleable.xcard_pushLimit, 10);
        mPullLimit = ta.getInt(R.styleable.xcard_pullLimit, 10);
        mLoadInterval = ta.getInt(R.styleable.xcard_autoLoadInterval, 5);
        mPushInterval = ta.getInt(R.styleable.xcard_autoPushInterval, 5);
        mPullInterval = ta.getInt(R.styleable.xcard_autoPullInterval, 5);
        mDefaultCard = ta.getString(R.styleable.xcard_defaultCard);
        mAdapter.mArtLimit = ta.getInt(R.styleable.xcard_artLimit, 40);
        if ("".equals(mSort) || mSort == null) {
            mSort = "desc";
        }
        if ("".equals(mName) || mName == null) {
            mName = "unknown";
        }
        if ("".equals(mChannel) || mChannel == null) {
            mChannel = "unknown";
        }
        if ("".equals(mInc) || mInc == null) {
            mInc = "inc";
        }
        /**
         * 把传进来的cardname1,cardname2,cardname3转换成一个map
         * */
        mCards = new HashMap<>();
        String acceptAttr = ta.getString(R.styleable.xcard_acceptCards);
        String[] acceptArray = acceptAttr.split(",");
        for (int i = 0; i < acceptArray.length; i++) {
            mCards.put(acceptArray[i], i + 10001);
        }


        /**
         * 不要动下面这一句话
         * */
        ta.recycle();

        setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        /**
         * listview
         * https://github.com/jianghejie/XRecyclerView
         * the loading effect we use the AVLoadingIndicatorView . and it is built in(make a little change). we provide all the effect in AVLoadingIndicatorView library besides we add a system style. you can call
         */
        /**
         * 下面这句可以改变刷新按钮的箭头样式
         * */
        //xRecyclerView.setArrowImageView(R.drawable.iconfont_downgrey);
        /**
         * if you don't want the refresh and load more featrue(in that case,you probably dont'n need the lib neither),you can call
         * 如果设为false,根本不能下拉，没有下拉动画
         * */
        //xRecyclerView.setPullRefreshEnabled(false);
        /**
         * 或者你想加两个Header
         * **/
//        View header =   LayoutInflater.from(this).inflate(R.layout.recyclerview_header, (ViewGroup)findViewById(android.R.id.content),false);
//        View header1 =   LayoutInflater.from(this).inflate(R.layout.recyclerview_header1, (ViewGroup)findViewById(android.R.id.content),false);
//        mRecyclerView.addHeaderView(header);
//        mRecyclerView.addHeaderView(header1);

        EventBus.getDefault().register(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        setLayoutManager(linearLayoutManager);
        setHasFixedSize(true);
        setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        setLoadingMoreProgressStyle(ProgressStyle.SquareSpin);

        if (mPullInterval > 0)
            mHandler.sendEmptyMessageDelayed(AUTO_PULL, mPullInterval);

        if (mPushInterval > 0)
            mHandler.sendEmptyMessageDelayed(AUTO_PUSH, mPushInterval);

        setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                /*** 这里面有一个特殊情况，如果从上到下是升序排列，并且最上面那一项是1，那么要转换成load操作
                 * */
                if (mAdapter.minTid == 1 && "asc".equals(mSort)) {
                    load();
                } else {
                    pull();
                }
            }

            @Override
            public void onLoadMore() {
                if (mAdapter.minTid == 1 && "desc".equals(mSort)) {
                    load();
                } else {
                    push();
                }
            }
        });
        mHandler.sendEmptyMessageDelayed(LOAD_DATA, 300);
    }

    public static void init(int width, int hight) {
        /* 屏幕宽高 */
        Constant.screenWidth = width;
        Constant.screenHight = hight;
        /**
         *  fixme: 如何获取resource目录下的dimen.width
         * */
        Constant.cardMargin = 14;
        Constant.cardPadding = 10;
        Constant.cardItemWidth = Constant.screenWidth - Constant.cardMargin * 2 - Constant.cardPadding * 2;
        Constant.cardWidth = Constant.screenWidth - Constant.cardMargin * 2;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("from", "article");
                jsonObject.put("to", "toast");
                jsonObject.put("type", "NetError");
                jsonObject.put("text", "网络错误!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            switch (msg.what) {
                case UPDATE_DATA:
                    mAdapter.notifyDataSetChanged();
                    refreshComplete();
                    loadMoreComplete();
                    break;
                case PULL:
                    mAdapter.notifyDataSetChanged();
                    refreshComplete();
                    break;
                case PUSH:
                    mAdapter.notifyDataSetChanged();
                    loadMoreComplete();
                    break;
                case DATASRC_ERROR:
                    // app错误
                    EventBus.getDefault().post(jsonObject);
                    refreshComplete();
                    loadMoreComplete();
                    break;
                case NETWORK_ERROR:
                    EventBus.getDefault().post(jsonObject);
                    refreshComplete();
                    loadMoreComplete();
                    break;
                case SERVER_ERROR:
                    //Toast.makeText(getContext(), "服务器错误", Toast.LENGTH_SHORT).show();
                    //服务器错误
                    EventBus.getDefault().post(jsonObject);
                    refreshComplete();
                    loadMoreComplete();
                    Log.i(TAG, "xxxx server_error loadMoreComplete()");
                    break;
                case PULL_NOMORE:
                    //Toast.makeText(getContext(), "已经是最新了", Toast.LENGTH_SHORT).show();
                    refreshComplete();
                    Log.i(TAG, "xxxx pull refreshComplete()");
                    break;
                case PUSH_NOMORE:
                    //Toast.makeText(getContext(), "已经是最旧了", Toast.LENGTH_SHORT).show();
                    loadMoreComplete();
                    Log.i(TAG, "xxxx push loadMoreComplete()");
                    break;
                case LOAD_NOMORE:
                    //Toast.makeText(getContext(), "本地没有数据", Toast.LENGTH_SHORT).show();
                    break;
                case HEAD_NOMORE:
                    //Toast.makeText(getContext(), "没有置顶文章", Toast.LENGTH_SHORT).show();
                    loadMoreComplete();
                    Log.i(TAG, "xxxx head loadMoreComplete()");
                    break;
                case FOOT_NOMORE:
                    //Toast.makeText(getContext(), "没有置地文章", Toast.LENGTH_SHORT).show();
                    break;
                /**
                 * 自动加载的逻辑
                 */
                case AUTO_LODE:
                    load();
                    mHandler.sendEmptyMessageDelayed(AUTO_LODE, DELAY_MILLIS);
                    break;
                case AUTO_PULL:
                    pull();
                    mHandler.sendEmptyMessageDelayed(AUTO_PULL, DELAY_MILLIS);
                    break;
                case AUTO_PUSH:
                    push();
                    mHandler.sendEmptyMessageDelayed(AUTO_PUSH, DELAY_MILLIS);
                    break;
                case LOAD_DATA:
                    load();
                    break;
            }
        }
    };

    /**
     * xcard的load函数，在xcard生成的时候调用
     */
    public void load() {
        try {
            JSONObject joRequest = new JSONObject();
            joRequest.put("from", mName);
            joRequest.put("to", mDataSrc);
            joRequest.put("channel", mChannel);
            joRequest.put("action", "load");
            joRequest.put("inc", 0);
            joRequest.put("rid", mRid);
            joRequest.put("sort", mSort);
            joRequest.put("limit", mLoadLimit);
            EventBus.getDefault().post(joRequest);
            Log.e("xcard EventBus-post:", joRequest.toString());
            /**如果打开循环,那么开启*/
            if (mLoadInterval > 0) {
                mHandler.sendEmptyMessageDelayed(AUTO_LODE, mLoadInterval);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * xcard的push函数，在xcard上推的时候调用
     */
    private void push() {
        Log.i(TAG, "push!");
        try {
            JSONObject joRequest = new JSONObject();
            joRequest.put("from", mName);
            joRequest.put("to", mDataSrc);
            joRequest.put("channel", mChannel);
            joRequest.put("action", "push");
            if ("desc".equals(mSort)) {
                joRequest.put("inc", mAdapter.minTid);
            } else {
                joRequest.put("inc", mAdapter.maxTid);
            }
            joRequest.put("rid", mRid);
            joRequest.put("sort", mSort);
            joRequest.put("limit", mPushLimit);
            EventBus.getDefault().post(joRequest);
            Log.e("xcard EventBus-post:", joRequest.toString());
            /**如果打开循环,那么开启*/
            if (mPushInterval > 0) {
                mHandler.sendEmptyMessageDelayed(AUTO_PUSH, mPushInterval);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * xcard的push函数，在xcard上推的时候调用
     */
    private void pull() {
        try {
            JSONObject joRequest = new JSONObject();
            joRequest.put("from", mName);
            joRequest.put("to", mDataSrc);
            joRequest.put("channel", mChannel);
            joRequest.put("action", "pull");
            if ("desc".equals(mSort)) {
                joRequest.put("inc", mAdapter.maxTid);
            } else {
                joRequest.put("inc", mAdapter.minTid);
            }
            joRequest.put("rid", mRid);
            joRequest.put("sort", mSort);
            joRequest.put("limit", mPullLimit);
            EventBus.getDefault().post(joRequest);
            Log.e("xcard EventBus-post:", joRequest.toString() + "***" + mSort);
            /**如果打开循环,那么开启*/
            if (mPullInterval > 0) {
                mHandler.sendEmptyMessageDelayed(AUTO_PULL, mPullInterval);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果事件不是发给自己的，那么return.
     * 这个地方是xcard的接收事件的总入口
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(JSONObject joReply) {
        String to = joReply.optString("to");
        if (!to.equals(mName)) {
            return;
        }
        Log.e(TAG, "EventBus-onEvent:xxxx@" + mChannel + joReply.toString());
        switch (joReply.optInt("status")) {
            case 0://成功
            {
                String action = joReply.optString("action");
                onResponse(action, joReply.optJSONObject("result"));
                break;
            }
            case 1://网络错误
                mHandler.sendEmptyMessage(NETWORK_ERROR);
                break;
            case 6://ARTSDK内部例外
                mHandler.sendEmptyMessage(DATASRC_ERROR);
                break;
            case 2://ArticleSDKn内部不可识别的错误
                mHandler.sendEmptyMessage(DATASRC_ERROR);
                break;
            case 3://ArticleSDK数据解析错误,这个错误需要调试才能知道是客户端的错还是服务器端的错
                mHandler.sendEmptyMessage(DATASRC_ERROR);
                break;
            case 4://ArticleSDK数据解析错误,这个错误需要调试才能知道是客户端的错还是服务器端的错
                mHandler.sendEmptyMessage(SERVER_ERROR);
                break;
            case 5://ArticleSDK数据解析错误,这个错误需要调试才能知道是客户端的错还是服务器端的错
                if (pull) {
                    mHandler.sendEmptyMessage(PULL_NOMORE);
                }
                if (push) {
                    mHandler.sendEmptyMessage(PUSH_NOMORE);
                }
                break;
            default:
                mHandler.sendEmptyMessage(DATASRC_ERROR);
                break;
        }
    }

    /**
     * 在我们的经历中，xcard这里最容易出问题，如果收到的数据不是严格按照线性顺序排列的，那么xcard就要立刻报错
     * 根据来着的action(影响回来的)
     */
    public void onResponse(final String action, JSONObject result) {
        try {
            Boolean load = action.equals("load");
            Boolean pull = action.equals("pull");
            Boolean push = action.equals("push");
            Boolean prev = action.equals("prev");
            Boolean post = action.equals("post");
            Boolean head = action.equals("head");
            Boolean foot = action.equals("foot");
            /**
             * 如果文章为空,那么发送相应的空消息
             * */
            JSONArray articles = result.optJSONArray("data");
            if (articles == null || articles.length() == 0) {
                if (load) mHandler.sendEmptyMessage(LOAD_NOMORE);
                if (pull) mHandler.sendEmptyMessage(PULL_NOMORE);
                if (push) mHandler.sendEmptyMessage(PUSH_NOMORE);
                if (prev) mHandler.sendEmptyMessage(PULL_NOMORE);
                if (post) mHandler.sendEmptyMessage(PUSH_NOMORE);
                if (head) mHandler.sendEmptyMessage(HEAD_NOMORE);
                if (foot) mHandler.sendEmptyMessage(FOOT_NOMORE);
                return;
            }

            for (int i = 0; i < articles.length(); i++) {
                JSONObject joItem = (JSONObject) articles.get(i);
                if (!joItem.has(mInc)) {
                    Log.e(TAG, "no inc field with name " + mInc);
                    break;
                }
                if (joItem.has("inc")) {
                    Log.e(TAG, "inc is keyword preserved by xcard ");
                    break;
                }
                long inc = joItem.getInt(mInc);
                //fixme：把自增字段转换为inc命名以方便adapter处理
                joItem.put("inc", inc);
                /**
                 * 下面的字段处理inc字段不正常的情况
                 *  head和foot行为不参与inc比较
                 * */
                if ((pull || prev) && "desc".equals(mSort) && inc < mAdapter.maxTid) {
                    Log.e(TAG, "smaller inc " + inc + " < " + mAdapter.maxTid + " found!");
                    break;
                }
                if ((push || post) && "desc".equals(mSort) && inc > mAdapter.minTid) {
                    Log.e(TAG, "greater inc " + inc + " > " + mAdapter.minTid + " found!");
                    break;
                }
                if ((pull || prev) && "asc".equals(mSort) && inc > mAdapter.minTid) {
                    Log.e(TAG, "greater inc " + inc + " > " + mAdapter.maxTid + " found!");
                    break;
                }
                if ((push || post) && "asc".equals(mSort) && inc < mAdapter.maxTid) {
                    Log.e(TAG, "smaller inc " + inc + " > " + mAdapter.minTid + " found!");
                    break;
                }
                /**
                 * 如果来的数据没有指明card类型，那么以默认card代替.有些卡片有type,但是type字段为空""
                 * */
                if (!joItem.has("type") || joItem.has("type") && joItem.optString("type").equals("")) {
                    System.out.println("------------:每个卡片的type2:" + joItem.optString("type"));
                    joItem.put("vtype", getViewType(mDefaultCard));
                } else {
                    System.out.println("------------:每个卡片的type:" + joItem.optString("type"));
                    joItem.put("vtype", getViewType(joItem.optString("type")));
                }
            }
            /**这个地方,我们把服务器回来的数据和result合并*/
            if (action.equals("pull")) {
                mAdapter.pullData(articles);
                mHandler.sendEmptyMessage(PULL);
            } else if (action.equals("push")) {
                mAdapter.pushData(articles);
                mHandler.sendEmptyMessage(PUSH);
            } else {
                mAdapter.loadData(articles);
                mHandler.sendEmptyMessage(UPDATE_DATA);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置channel
     *
     * @param channel
     */
    public void setChannel(String channel) {
        this.mChannel = channel;
    }
}
