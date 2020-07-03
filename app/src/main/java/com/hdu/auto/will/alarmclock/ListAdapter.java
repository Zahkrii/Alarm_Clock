package com.hdu.auto.will.alarmclock;

import android.annotation.SuppressLint;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class ListAdapter extends BaseQuickAdapter<Alarm, BaseViewHolder> {

    private TextView tView1;
    private TextView tView2;
    private TextView tView3;

    private List<Alarm> _alarmList;

    public ListAdapter(List<Alarm> alarmList) {
        super(R.layout.item_list, alarmList);
        _alarmList = alarmList;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, Alarm alarm) {
        initView(baseViewHolder);
        initData(baseViewHolder);
    }

    private void initView(BaseViewHolder viewHolder) {
        tView1 = viewHolder.getView(R.id.time_txtview);
        tView2 = viewHolder.getView(R.id.title_txtview);
        tView3 = viewHolder.getView(R.id.date_txtview);
    }

    @SuppressLint("SetTextI18n")
    private void initData(BaseViewHolder holder) {
        tView1.setText(_alarmList.get(holder.getLayoutPosition()).getTitle());
        tView2.setText(_alarmList.get(holder.getLayoutPosition()).getContent());
        if (_alarmList.get(holder.getLayoutPosition()).getDateCount() == -1)
            tView3.setText("Only Once");
        else if (_alarmList.get(holder.getLayoutPosition()).getDateCount() == 0)
            tView3.setText("Everyday");
        else {
            int[] tmp = _alarmList.get(holder.getLayoutPosition()).getWeek();
            String s = "";
            int n = 0;
            for (int i = 0; i < 7; i++) {
                if (tmp[i] == 1) {
                    n = i + 1;
                    s += "周 " + n + "，";
                }
            }
            s = s.substring(0, s.length() - 1);
            if (s.equals("周 0"))
                tView3.setText("Only Once");
            else
                tView3.setText(s);
        }

    }
}
