package com.example.event;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
            public MyViewHolder(View itemView) {
                super(itemView);
                mOrderNum = (TextView) itemView.findViewById(R.id.tv_number);
                mSheng = (TextView) itemView.findViewById(R.id.tv_sheng);
                mXian = (TextView) itemView.findViewById(R.id.tv_xian);
                mDiaoPerson = (TextView) itemView.findViewById(R.id.tv_diao_person);
                mFillPerson = (TextView) itemView.findViewById(R.id.tv_fiil_person);
                mTvDate = (TextView) itemView.findViewById(R.id.tv_date);
                mAddress = (TextView) itemView.findViewById(R.id.tv_address);

            }
        }
    }
}
