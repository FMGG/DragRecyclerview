package com.hangsu.dragrecyclerview;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DataGroup extends Data{

    private List<Data> mChild = new ArrayList<>();
    private String mCategory;

    public void addChild(@NonNull Data data){
        data.setParent(this);
        mChild.add(data);
    }

    public void addChild(int location,@NonNull Data data){
        data.setParent(this);
        mChild.add(location,data);
    }

    public Data removeChild(int location){
        Data data = mChild.remove(location);
        data.setParent(null);
        return data;
    }

    public boolean removeChild(@NonNull Data data){
        data.setParent(null);
        return mChild.remove(data);
    }


    public int getChildCount(){
        return mChild.size();
    }


    public Data getChild(int position){
        return mChild.get(position);
    }


    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public int getCheckedCount(){
        if(mChild != null){
            int i = 0;
            for(Data data:mChild){
                if(data.isChecked()){
                    i++;
                }
            }
            return i;
        }
        return 0;
    }

}
