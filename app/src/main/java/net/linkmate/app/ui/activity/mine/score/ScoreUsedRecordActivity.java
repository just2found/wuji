package net.linkmate.app.ui.activity.mine.score;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;

import net.linkmate.app.R;
import net.linkmate.app.base.BaseActivity;
import net.linkmate.app.base.MyOkHttpListener;
import net.linkmate.app.bean.ScoreUsedRecordBean;
import net.linkmate.app.util.business.ScoreOrderUtil;
import net.linkmate.app.view.TipsBar;
import net.linkmate.app.view.adapter.ScoreUsedRecordRVAdapter;
import net.sdvn.common.internet.core.GsonBaseProtocol;
import net.sdvn.common.internet.core.HttpLoader;
import net.sdvn.common.internet.loader.GetScoreStatHttpLoader;
import net.sdvn.common.internet.protocol.scorepay.ScoreStat;
import net.sdvn.common.internet.protocol.scorepay.ScoreUseRecordList;
import net.sdvn.nascommon.utils.Utils;
import net.sdvn.scorepaylib.score.ScoreAPIUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class ScoreUsedRecordActivity extends BaseActivity implements HttpLoader.HttpLoaderStateListener {
    private ImageView ivLeft;
    private TextView tvTitle;
    private TextView tvRight;
    private ImageView ivRight;
    private RelativeLayout rlTitle;
    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;

    private ScoreUsedRecordRVAdapter mAdapter;
    private List<ScoreUsedRecordBean> adapterData = new ArrayList<>();
    private List<ScoreStat.DataBean.ListBean> statBeans = new ArrayList<>();
    private List<ScoreUseRecordList.DataBean.HistoryBean> trueHistoryBeans = new ArrayList<>();

    private String peroid = "month";
    private int currPage = 1;
    private final int pageSize = 10;
    private boolean hasMorePage;
    private RelativeLayout emptyView;
    private RelativeLayout errorView;
    private View mItbIvLeft;
    private View mItbTvRight;

    @Override
    protected View getTopView() {
        return rlTitle;
    }

    @Nullable
    @Override
    protected TipsBar getTipsBar() {
        return findViewById(R.id.tipsBar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge_record);
        bindView(this.getWindow().getDecorView());
        tvTitle.setText(R.string.score_expenses_record);
        tvTitle.setTextColor(getResources().getColor(R.color.title_text_color));
        ivLeft.setVisibility(View.VISIBLE);
        ivLeft.setImageResource(R.drawable.icon_return);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText(R.string.income);
        ivRight.setVisibility(View.GONE);
        initView();
        initRv();
        initTotal();
    }

    private void initView() {
        emptyView = (RelativeLayout) View.inflate(this, R.layout.pager_empty_text, null);
        ((TextView) emptyView.findViewById(R.id.tv_tips)).setText(R.string.no_record);
        errorView = (RelativeLayout) View.inflate(this, R.layout.layout_error_view, null);
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currPage = 1;
                trueHistoryBeans.clear();
                initTotal();
            }
        });
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currPage = 1;
                trueHistoryBeans.clear();
                initTotal();
            }
        });
    }

    private void initRv() {
        mAdapter = new ScoreUsedRecordRVAdapter(adapterData);
        //点击device条目
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ScoreUsedRecordBean bean = adapterData.get(position);
                if (bean.itemType == 2) {
                    new ScoreOrderUtil(ScoreUsedRecordActivity.this, bean,getSupportFragmentManager()).show();
                }
            }
        });
        mAdapter.setEmptyView(emptyView);
        mRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRv.setItemAnimator(null);
        mRv.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                if (hasMorePage) {
                    currPage++;
                    initDatas();
                } else {
                    mAdapter.loadMoreEnd(true);
                }
            }
        }, mRv);
    }

    private void initTotal() {
        GetScoreStatHttpLoader loader = new GetScoreStatHttpLoader(ScoreStat.class);
        loader.setHttpLoaderStateListener(this);
        loader.setParams(peroid, "", 1, 24);
        loader.executor(new MyOkHttpListener<ScoreStat>() {
            @Override
            public void success(Object tag, ScoreStat data) {
                statBeans.clear();
                statBeans.addAll(data.data.list);
                initDatas();
//                initDateOption();
            }

            @Override
            public void error(Object tag, GsonBaseProtocol baseProtocol) {
                super.error(tag, baseProtocol);
            }
        });
    }

    private void initDatas() {
        ScoreAPIUtil.ScoreUseRecord(this, currPage, pageSize, this, ScoreUseRecordList.class,
                new MyOkHttpListener<ScoreUseRecordList>() {
                    @Override
                    public void error(Object tag, GsonBaseProtocol baseProtocol) {
                        super.error(tag, baseProtocol);
                        mAdapter.setEmptyView(errorView);
                    }

                    @Override
                    public void success(Object tag, ScoreUseRecordList data) {
                        mAdapter.setEmptyView(emptyView);
                        hasMorePage = currPage < data.data.totalPage;

                        trueHistoryBeans.addAll(data.data.history);

                        List<ScoreUsedRecordBean> currentBeans = new ArrayList<>();
                        if (data.data.current != null && data.data.current.size() > 0) {
                            currentBeans.add(0, new ScoreUsedRecordBean(-1));
                            for (ScoreUseRecordList.DataBean.HistoryBean bean : data.data.current) {
                                currentBeans.add(new ScoreUsedRecordBean(bean));
                            }
                        }

                        List<ScoreUsedRecordBean> historyBeans = new ArrayList<>();
                        for (ScoreUseRecordList.DataBean.HistoryBean bean : trueHistoryBeans) {
                            ScoreUsedRecordBean e = new ScoreUsedRecordBean(bean);
                            if (!historyBeans.contains(e)) {
                                historyBeans.add(e);
                            }
                        }
                        Collections.sort(historyBeans, new Comparator<ScoreUsedRecordBean>() {
                            @Override
                            public int compare(ScoreUsedRecordBean o1, ScoreUsedRecordBean o2) {
                                return o1.data.billdate == o2.data.billdate ? 0 : (o1.data.billdate - o2.data.billdate > 0 ? -1 : 1);
                            }
                        });
                        a:
                        for (int i = 0; i < historyBeans.size(); i++) {
                            ScoreUsedRecordBean current = historyBeans.get(i);
                            if (i == 0 || !isSameMonth(historyBeans.get(i - 1).data.billdate, current.data.billdate)) {
                                for (ScoreStat.DataBean.ListBean statBean : statBeans) {
                                    if (isSameMonth(statBean.billtime, current.data.billdate)) {
                                        historyBeans.add(i, new ScoreUsedRecordBean(statBean));
                                        continue a;
                                    }
                                }
                                historyBeans.add(i, new ScoreUsedRecordBean(current.data.billdate));
                            }
                        }
                        adapterData.clear();
                        adapterData.addAll(currentBeans);
                        adapterData.addAll(historyBeans);
                        mAdapter.setNewData(adapterData);
                        if (hasMorePage) {
                            mAdapter.loadMoreComplete();
                        } else {
                            mAdapter.loadMoreEnd(true);
                        }
                    }
                });
    }

    public static boolean isSameMonth(long timeMillis1, long timeMillis2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(timeMillis1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(timeMillis2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }

    private void onViewClicked(View view) {
        if (Utils.isFastClick(view)) return;
        switch (view.getId()) {
            case R.id.itb_iv_left:
                onBackPressed();
                break;
            case R.id.itb_tv_right:
                startActivity(new Intent(this, ScoreGetRecordActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void onLoadStart(Disposable disposable) {
        mSrl.setRefreshing(true);
    }

    @Override
    public void onLoadComplete() {
        mSrl.setRefreshing(false);
    }

    @Override
    public void onLoadError() {
        mSrl.setRefreshing(false);
    }

    private void bindView(View bindSource) {
        ivLeft = bindSource.findViewById(R.id.itb_iv_left);
        tvTitle = bindSource.findViewById(R.id.itb_tv_title);
        tvRight = bindSource.findViewById(R.id.itb_tv_right);
        ivRight = bindSource.findViewById(R.id.itb_iv_right);
        rlTitle = bindSource.findViewById(R.id.itb_rl);
        mSrl = bindSource.findViewById(R.id.recharge_recode_srl);
        mRv = bindSource.findViewById(R.id.recharge_recode_rv);
        mItbIvLeft = bindSource.findViewById(R.id.itb_iv_left);
        mItbTvRight = bindSource.findViewById(R.id.itb_tv_right);
        mItbIvLeft.setOnClickListener(v -> {
            onViewClicked(v);
        });
        mItbTvRight.setOnClickListener(v -> {
            onViewClicked(v);
        });
    }
}