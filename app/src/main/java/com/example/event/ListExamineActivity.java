package com.example.event;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.event.View.RecyclerViewSpacesItemDecoration;
import com.example.event.db.xEntity.RoundExamineEntity;
import com.example.event.manager.PatrolManager;

import java.io.Serializable;
import java.util.List;

public class ListExamineActivity extends AppCompatActivity {

    private RecyclerView mRecyView;
    private ListExamineAdapter mAdapter;
    private View conentView;
    private MenuPopWindow mPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_examine);
        View customView = LayoutInflater.from(this).inflate(R.layout.actionbar_back, new RelativeLayout(this), false);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setCustomView(customView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ((TextView)customView.findViewById(R.id.tv_back)).setText("调查记录");
        ((ImageView)customView.findViewById(R.id.iv_tools)).setVisibility(View.VISIBLE);
        ((ImageView)customView.findViewById(R.id.iv_tools)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow = new MenuPopWindow(ListExamineActivity.this);
                mPopupWindow.showPopupWindow(v);
            }
        });

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.menu_popup_window, null);
        ((ImageView)customView.findViewById(R.id.iv_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRecyView = (RecyclerView) findViewById(R.id.list_examine);
        mRecyView.addItemDecoration(new RecyclerViewSpacesItemDecoration(10));
        mRecyView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ListExamineAdapter();
        mRecyView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<RoundExamineEntity> mlist = PatrolManager.getInstance().getExam();
        mAdapter.setDatas(mlist);

    }

    private class ListExamineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private List<RoundExamineEntity> mlist;


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_examine_item, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
            MyViewHolder holders = (MyViewHolder) holder;
            final RoundExamineEntity bean=mlist.get(i);
            holders.mOrderNum.setText(bean.getOrderNumber());
            holders.mSheng.setText(bean.getSheng());
            holders.mXian.setText(bean.getXian());
            holders.mDiaoPerson.setText(bean.getExmainPerson());
            holders.mFillPerson.setText(bean.getFillPerson());
            holders.mTvDate.setText(bean.getExamineDate());
            holders.mAddress.setText(bean.getAddress());
            holders.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(bean!=null) {
                        Intent intent = new Intent(ListExamineActivity.this, EventEditActivity.class);
                        intent.putExtra("StateBean", (Serializable) bean);
                        startActivity(intent);
                    }
                }
            });
//            holders.mZhongChinaName.setText(bean.getZhongCName());
//            holders.mShuChinaName.setText(bean.getShuCName());
//            holders.mKeChinaName.setText(bean.getKeCName());
//            holders.mZhongLaName.setText(bean.getZhongLaName());
//            holders.mShuLaName.setText(bean.getShuLaName());
//            holders.mKeName.setText(bean.getKeCName());
//            holders.mLat.setText(bean.getLatitude()+"");
//            holders.mlon.setText(bean.getLongtitude()+"");
//            holders.mPoXiang.setText(bean.getPoXiang());
//            holders.mPoWei.setText(bean.getPoWei());
//            holders.mPoDu.setText(bean.getPoDu());
//            holders.mTreeHigh.setText(bean.getTreeHight()+"");
//            holders.mXiong.setText(bean.getXiongJin()+"");
//            holders.mGuanfu.setText(bean.getGuanFu()+"");
//            holders.mZhiGao.setText(bean.getZhiHight()+"");
//            holders.mSingleXuji.setText(bean.getXuji()+"");
//            holders.mTuType.setText(bean.getTuType());
//            holders.mHigh.setText(bean.getHight()+"");
//            holders.mPhotoOrder.setText(bean.getPhotoOrderNum());
//            holders.mTakePerson.setText(bean.getTakePerson());
//            holders.mTakeDate.setText(bean.getTakeDate());
//            holders.mSingDescribe.setText(bean.getImportDescribe());
//            holders.mBeizhu.setText(bean.getStateDescribe());
        }

        @Override
        public int getItemCount() {
            if (mlist != null && mlist.size() > 0) {
                return mlist.size();
            } else {
                return 0;
            }
        }

        public void setDatas(List<RoundExamineEntity> mlist) {
            this.mlist=mlist;
            notifyDataSetChanged();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private final TextView mOrderNum,mSheng,mXian,mDiaoPerson,mFillPerson,mTvDate,mAddress;
//            private final TextView mZhongChinaName,mShuChinaName,mKeChinaName,mZhongLaName,mShuLaName,mKeName;
//            private final TextView mLat,mlon,mPoXiang,mPoWei,mPoDu,mTreeHigh,mXiong,mGuanfu,mZhiGao,mSingleXuji,mTuType;
//            private final TextView mHigh,mPhotoOrder,mTakePerson,mTakeDate,mSingDescribe,mBeizhu;
            public MyViewHolder(View itemView) {
                super(itemView);
                mOrderNum = (TextView) itemView.findViewById(R.id.tv_number);
                mSheng = (TextView) itemView.findViewById(R.id.tv_sheng);
                mXian = (TextView) itemView.findViewById(R.id.tv_xian);
                mDiaoPerson = (TextView) itemView.findViewById(R.id.tv_diao_person);
                mFillPerson = (TextView) itemView.findViewById(R.id.tv_fiil_person);
                mTvDate = (TextView) itemView.findViewById(R.id.tv_date);
                mAddress = (TextView) itemView.findViewById(R.id.tv_address);
//                mZhongChinaName = (TextView) itemView.findViewById(R.id.zhong_china_name);
//                mShuChinaName = (TextView) itemView.findViewById(R.id.shu_china_name);
//                mKeChinaName = (TextView) itemView.findViewById(R.id.ke_china_name);
//                mZhongLaName = (TextView) itemView.findViewById(R.id.tv_zhong_la);
//                mShuLaName = (TextView) itemView.findViewById(R.id.tv_shu_name);
//                mKeName = (TextView) itemView.findViewById(R.id.tv_ke_name);
//                mLat= (TextView) itemView.findViewById(R.id.tv_lat);
//                mlon = (TextView) itemView.findViewById(R.id.tv_lon);
//                mPoXiang = (TextView) itemView.findViewById(R.id.tv_po_xiang);
//                mPoWei= (TextView) itemView.findViewById(R.id.po_wei);
//                mPoDu= (TextView) itemView.findViewById(R.id.tv_po_du);
//                mTreeHigh = (TextView) itemView.findViewById(R.id.shu_hight);
//                mXiong = (TextView) itemView.findViewById(R.id.tv_xiong);
//                mGuanfu = (TextView) itemView.findViewById(R.id.tv_guanfu);
//                mZhiGao = (TextView) itemView.findViewById(R.id.tv_zhi_high);
//                mSingleXuji = (TextView) itemView.findViewById(R.id.tv_single_xuji);
//                mTuType = (TextView) itemView.findViewById(R.id.tv_tu_type);
//                mHigh = (TextView) itemView.findViewById(R.id.tv_high);
//                mPhotoOrder = (TextView) itemView.findViewById(R.id.tv_photo_order);
//                mTakePerson = (TextView) itemView.findViewById(R.id.tv_take_person);
//                mTakeDate = (TextView) itemView.findViewById(R.id.tv_take_date);
//                mSingDescribe = (TextView) itemView.findViewById(R.id.tv_single_describe);
//                mBeizhu = (TextView) itemView.findViewById(R.id.tv_beizhu);
            }
        }
    }
}
