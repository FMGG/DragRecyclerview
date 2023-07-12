package com.hangsu.dragrecyclerview;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.hangsu.dragrecyclerview.databinding.ActivityMainBinding;
import com.hangsu.dragrecyclerview.logutils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainBinding;

    private DataAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        adapter = new DataAdapter();
        List<Data> dataArrayList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Data data = new Data();
            if (i == 0){
                data.setImg(R.drawable.icon_check_correct);
            }else if (i == 1){
                data.setImg(R.drawable.icon_check_difference);
            }else if (i == 2){
                data.setImg(R.drawable.icon_checkreview);
            }else if (i == 3){
                data.setImg(R.drawable.icon_cloud_ledger);
            }else if (i == 4){
                data.setImg(R.drawable.ic_launcher_background);
            }else {
                data.setImg(R.drawable.icon_damage);
            }
            dataArrayList.add(data);
        }
        adapter.setDataList(dataArrayList);
        adapter.registerObserver(new DataAdapter.DataObserver() {
            @Override
            public void onChecked(boolean isChecked) {
                LogUtil.e("onChecked");
            }

            @Override
            public void onEditChanged(boolean inEdit) {
                LogUtil.e("onEditChanged");
                /*if(inEdit){
                    showEditMode();
                }else {
                    hideEditMode();
                }*/
            }

            @Override
            public void onRestore() {
                LogUtil.e("onRestore");
            }

            @Override
            public void onHideSubDialog() {
                LogUtil.e("onHideSubDialog");
                mainBinding.classifyMain.hideSubContainer();
            }
        });
        mainBinding.classifyMain.setAdapter(adapter);
        mainBinding.classifyMain.setDebugAble(true);
    }
}
