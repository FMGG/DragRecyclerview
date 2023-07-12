package com.hangsu.dragrecyclerview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Observable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anarchy.classify.adapter.BaseMainAdapter;
import com.anarchy.classify.adapter.BaseSubAdapter;
import com.anarchy.classify.simple.PrimitiveSimpleAdapter;
import com.hangsu.dragrecyclerview.databinding.ItemIDataFolderBinding;
import com.hangsu.dragrecyclerview.logutils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataAdapter extends PrimitiveSimpleAdapter<DataGroup, DataAdapter.ViewHolder> {
    private final static String IReaderAdapterLog="IReaderAdapterLog";
    private List<Data> dataList;
    private boolean mMockSourceChanged;
    private List<DataGroup> mLastDataGroup;
    private List<Data> mCheckedData = new ArrayList<>();
    private boolean mEditMode;
    private boolean mSubEditMode;
    private int[] mDragPosition = new int[2];

    //文件夹内，最大的容纳数量
    private final int FOLDER_MAX_NUM = 4;
    private DataObservable mObservable = new DataObservable();
    private SubObserver mSubObserver = new SubObserver(mObservable);
    private DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if(mObservable.isRegister(mSubObserver)) mObservable.unregisterObserver(mSubObserver);
            mSubEditMode = false;
        }
    };


    public void registerObserver(DataObserver observer) {
        mObservable.registerObserver(observer);
    }


    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }


    @Override
    protected void onDragStart(ViewHolder viewHolder, int parentIndex, int index) {
        if (!mEditMode) {
            //如果当前不为可编辑状态
            Data data = index == -1 ? dataList.get(parentIndex) : ((DataGroup) dataList.get(parentIndex)).getChild(index);
            if (data != null) {
                data.setChecked(true);
                mCheckedData.add(data);
                mObservable.notifyItemCheckChanged(true);
            }
        }
    }

    @Override
    protected void onDragAnimationEnd(ViewHolder viewHolder, int parentIndex, int index) {
        if (!mEditMode) {
            setEditMode(true);
        }
    }

    @Override
    protected void onSubDialogShow(Dialog dialog, int parentPosition) {
        //此处弹出次级别dialog
        dialog.setOnDismissListener(mDismissListener);
        //当次级窗口显示时需要修改标题
        final ViewGroup contentView = dialog.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        final EditText editText = contentView.findViewById(R.id.edit_title);
        Button btConfirm = contentView.findViewById(R.id.bt_Confirm);
        final DataGroup mockDataGroup = (DataGroup) dataList.get(parentPosition);
        mSubObserver.setBindResource(mockDataGroup, getMainAdapter(),getSubAdapter(),parentPosition);
        if(!mObservable.isRegister(mSubObserver)) mObservable.registerObserver(mSubObserver);
        //清除之前的状态
        editText.clearFocus();
        btConfirm.setVisibility(View.GONE);
        editText.setText(String.valueOf(mockDataGroup.getCategory()));
        editText.setOnFocusChangeListener((v, hasFocus) -> btConfirm.setVisibility(hasFocus ? View.VISIBLE : View.GONE));
        btConfirm.setOnClickListener(v -> {
            editText.clearFocus();
            String categoryName = editText.getText().toString();
            if (!TextUtils.isEmpty(categoryName)){
                ((DataGroup) dataList.get(parentPosition)).setCategory(categoryName);
            }
        });
    }

    /**
     * 判断前后文件夹名是否一致：
     * 不一致，刷新Adapter，modify DB
     * 一致：hidden SubDialog
     * @param dialog
     * @param parentPosition
     */
    @Override
    protected void onSubDialogCancel(Dialog dialog, int parentPosition) {
        Log.i(IReaderAdapterLog,"onSubDialogCancel");
        super.onSubDialogCancel(dialog, parentPosition);
    }

    static class SubObserver extends DataObserver {
        final DataObservable mObservable;
        DataGroup mGroup;
        BaseSubAdapter mSubAdapter;
        BaseMainAdapter mMainAdapter;
        int parentPosition;

        SubObserver(@NonNull DataObservable observable) {
            mObservable = observable;
        }

        void setBindResource(DataGroup source,BaseMainAdapter mainAdapter , BaseSubAdapter subAdapter, int parentPosition) {
            mGroup = source;
            mSubAdapter = subAdapter;
            mMainAdapter = mainAdapter;
            this.parentPosition = parentPosition;
            updateBind(true);
        }


        @Override
        public void onChecked(boolean isChecked) {
            updateBind(false);
        }

        private void updateBind(boolean force) {

        }


    }

    /**
     * 返回当前拖拽的view 在adapter中的位置
     *
     * @return 返回int[0] 主层级位置 如果为 -1 则当前没有拖拽的item
     * int[1] 副层级位置 如果为 -1 则当前没有拖拽副层级的item
     */
    @NonNull
    public int[] getCurrentDragAdapterPosition() {
        mDragPosition[0] = getMainAdapter().getDragPosition();
        mDragPosition[1] = getSubAdapter().getDragPosition();
        return mDragPosition;
    }

    /**
     * @return 如果当前拖拽的为单个书籍 则返回 其他情况返回null
     */
    @Nullable
    Data getCurrentSingleDragData() {
        int[] position = getCurrentDragAdapterPosition();
        if (position[0] == -1) return null;
        if (position[1] == -1) {
            Data data = dataList.get(position[0]);
            if (data instanceof DataGroup) return null;
            return data;
        } else {
            return ((DataGroup) dataList.get(position[0])).getChild(position[1]);
        }
    }

    /*public void removeAllCheckedBook() {
        if (mCheckedData.size() == 0) return;
        for (Data data : mCheckedData) {
            if (data.getParent() != null) {
                DataGroup parent = data.getParent();
                parent.removeChild(data);
                if (parent.getChildCount() == 0) {
                    dataList.remove(parent);
                }
            } else {
                dataList.remove(data);
            }
        }
        notifyDataSetChanged();
        getSubAdapter().notifyDataSetChanged();
        mObservable.notifyItemRestore();
        mObservable.notifyItemHideSubDialog();
    }*/

    /**
     * 设置是否在可编辑状态下
     *
     * @param editMode
     */
    public void setEditMode(boolean editMode) {
        mEditMode = editMode;
        if (!editMode) {
            if (mCheckedData.size() > 0) {
                for (Data data : mCheckedData) {
                    data.setChecked(false);
                }
                mCheckedData.clear();
            }
            mObservable.notifyItemRestore();
        }
        notifyDataSetChanged();
        getSubAdapter().notifyDataSetChanged();
        mObservable.notifyItemEditModeChanged(editMode);
    }

    public List<DataGroup> getDataGroup() {
        LogUtil.e("获取分组");
        if (dataList == null) return null;
        if (mLastDataGroup != null && !mMockSourceChanged) {
            return mLastDataGroup;
        } else {
            List<DataGroup> result = new ArrayList<>();
            for (Data data : dataList) {
                if (data instanceof DataGroup) {
                    result.add((DataGroup) data);
                }
            }
            mMockSourceChanged = false;
            mLastDataGroup = result;
            return result;
        }
    }


    /**
     * 创建view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    protected ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_i_data_folder, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 用于显示{@link com.anarchy.classify.simple.widget.InsertAbleGridView} 的item布局
     *
     * @param parent       父View
     * @param convertView  缓存的View 可能为null
     * @param mainPosition 主层级位置
     * @param subPosition  副层级位置
     * @return
     */
    @Override
    public View getView(ViewGroup parent, View convertView, int mainPosition, int subPosition) {
        View result;
        if (convertView != null) {
            result = convertView;
        } else {
            result = new View(parent.getContext());
        }
        try {
            int img = ((DataGroup) dataList.get(mainPosition)).getChild(subPosition).getImg();
            result.setBackgroundResource(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 返回主层级数量
     *
     * @return
     */
    @Override
    protected int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    /**
     * 副层级的数量，用于主层级上的显示效果
     *
     * @param parentPosition
     * @return
     */
    @Override
    protected int getSubItemCount(int parentPosition) {
        if(parentPosition < dataList.size()) {
            Data mockData = dataList.get(parentPosition);
            if (mockData instanceof DataGroup) {
                int subCount = ((DataGroup) mockData).getChildCount();
                return subCount;
            }
        }
        return 0;
    }

    /**
     * 返回副层级的数据源
     *
     * @param parentPosition
     * @return
     */
    @Override
    protected DataGroup getSubSource(int parentPosition) {
        Data mockData = dataList.get(parentPosition);
        if (mockData instanceof DataGroup) return (DataGroup) mockData;
        return null;
    }

    /**
     * 能否弹出次级窗口
     *
     * @param position    主层级点击的位置
     * @param pressedView 点击的view
     * @return
     */
    @Override
    protected boolean canExplodeItem(int position, View pressedView) {
        return dataList.get(position) instanceof DataGroup;
    }

    /**
     * 在主层级触发move事件 在这里进行数据改变
     *
     * @param selectedPosition 当前选择的item位置
     * @param targetPosition   要移动到的位置
     */
    @Override
    protected void onMove(int selectedPosition, int targetPosition) {
        dataList.add(targetPosition, dataList.remove(selectedPosition));
        mMockSourceChanged = true;
    }

    /**
     * 副层级数据移动处理
     *
     * @param iReaderMockDataGroup 副层级数据源
     * @param selectedPosition     当前选择的item位置
     * @param targetPosition       要移动到的位置
     */
    @Override
    protected void onSubMove(DataGroup iReaderMockDataGroup, int selectedPosition, int targetPosition) {
        iReaderMockDataGroup.addChild(targetPosition, iReaderMockDataGroup.removeChild(selectedPosition));
    }

    /**
     * 两个选项能否合并
     *
     * @param selectPosition
     * @param targetPosition
     * @return
     */
    @Override
    protected boolean canMergeItem(int selectPosition, int targetPosition) {
        Data select = dataList.get(selectPosition);
        Data target = dataList.get(targetPosition);
        if (target instanceof DataGroup){
            DataGroup group = (DataGroup) target;
            //如果文件中已经填满，就无法继续添加
            if (group.getChildCount() >= FOLDER_MAX_NUM){
                return false;
            }
        }
        return !(select instanceof DataGroup);
    }

    /**
     * 合并数据处理
     *
     * @param selectedPosition
     * @param targetPosition
     */
    @Override
    protected void onMerged(int selectedPosition, int targetPosition) {
        Data target = dataList.get(targetPosition);
        Data select = dataList.remove(selectedPosition);
        if (target instanceof DataGroup) {
            ((DataGroup) target).addChild(0, select);
        } else {
            //合并成为文件夹状态
            DataGroup group = new DataGroup();
            group.addChild(select);
            group.addChild(target);
            group.setCategory(generateNewCategoryTag());
            targetPosition = dataList.indexOf(target);
            dataList.remove(targetPosition);
            dataList.add(targetPosition, group);
        }
        mMockSourceChanged = true;
    }

    /**
     * 生成新的分类标签
     *
     * @return 新的分类标签
     */
    private String generateNewCategoryTag() {
        //生成默认分类标签
        List<DataGroup> mDataGroups = getDataGroup();
        if (mDataGroups.size() > 0) {
            int serialNumber = 1;
            int[] mHoldNumber = null;
            for (DataGroup temp : mDataGroups) {
                if (temp.getCategory().startsWith("分类")) {
                    //可能是自动生成的标签
                    String pendingStr = temp.getCategory().substring(2);
                    if (!TextUtils.isEmpty(pendingStr) && TextUtils.isDigitsOnly(pendingStr)) {
                        //尝试转换为整数
                        try {
                            int serialCategory = Integer.parseInt(pendingStr);
                            if (mHoldNumber == null) {
                                mHoldNumber = new int[1];
                                mHoldNumber[0] = serialCategory;
                            } else {
                                mHoldNumber = Arrays.copyOf(mHoldNumber, mHoldNumber.length + 1);
                                mHoldNumber[mHoldNumber.length - 1] = serialCategory;
                            }
                        } catch (NumberFormatException e) {
                            //nope
                        }
                    }
                }
            }
            if (mHoldNumber != null) {
                //有自动生成的标签
                Arrays.sort(mHoldNumber);
                for (int serial : mHoldNumber) {
                    if (serial < serialNumber) continue;
                    if (serial == serialNumber) {
                        //已经被占用 自增1
                        serialNumber++;
                    } else {
                        break;
                    }
                }
            }
            return "分类" + serialNumber;
        } else {
            return "分类1";
        }
    }

    /**
     * 从副层级移除的元素
     *
     * @param iReaderMockDataGroup 副层级数据源
     * @param selectedPosition     将要冲副层级移除的数据
     * @return 返回的数为添加到主层级的位置
     */
    @Override
    protected int onLeaveSubRegion(int parentPosition, DataGroup iReaderMockDataGroup, int selectedPosition) {
        LogUtil.e("onLeaveSubRegion  selectedPosition:"+selectedPosition);
        if(mObservable.isRegister(mSubObserver)) mObservable.unregisterObserver(mSubObserver);
        //从副层级移除并添加到主层级第一个位置上
        Data mockData = iReaderMockDataGroup.removeChild(selectedPosition);
        dataList.add(0, mockData);
        if (iReaderMockDataGroup.getChildCount() == 0) {
            int p = dataList.indexOf(iReaderMockDataGroup);
            dataList.remove(p);
        }
        mMockSourceChanged = true;
        return 0;
    }


    /**
     * 主层级数据绑定
     *
     * @param holder
     * @param position
     */
    @Override
    protected void onBindMainViewHolder(ViewHolder holder, int position) {
        holder.bind(dataList.get(position), mEditMode);
    }

    /**
     * 副层级数据绑定
     *
     * @param holder
     * @param mainPosition
     * @param subPosition
     */
    @Override
    protected void onBindSubViewHolder(ViewHolder holder, int mainPosition, int subPosition) {
        holder.bind(((DataGroup) dataList.get(mainPosition)).getChild(subPosition), mEditMode);
    }


    @Override
    protected void onItemClick(ViewHolder viewHolder, int parentIndex, int index) {
        if (mEditMode) {
            final Data mockData = index == -1 ? dataList.get(parentIndex) : ((DataGroup) dataList.get(parentIndex)).getChild(index);
            if (!(mockData instanceof DataGroup)) {
                //执行check动画
                mockData.setChecked(!mockData.isChecked());
                mCheckedData.add(mockData);
                //通知
                mObservable.notifyItemCheckChanged(mockData.isChecked());
                if (index != -1) {
                    notifyItemChanged(parentIndex);
                }
            }
        }
    }

    static class ViewHolder extends PrimitiveSimpleAdapter.ViewHolder {
        private ItemIDataFolderBinding mBinding;

        ViewHolder(View itemView) {
            super(itemView);
            mBinding = ItemIDataFolderBinding.bind(itemView);
        }

        ItemIDataFolderBinding getBinding() {
            return mBinding;
        }

        void bind(Data data, boolean inEditMode) {
            /*if (inEditMode) {
                if (iReaderMockData instanceof DataGroup) {
                    Log.i(Constants.CLASSIFY_VIEW_INIT,"IReaderMockDataGroup");
                    int count = ((DataGroup) iReaderMockData).getCheckedCount();
                    if (count > 0) {
                        mBinding.iDataFolderCheckBox.setVisibility(View.VISIBLE);
                        mBinding.iDataFolderCheckBox.setText(count + "");
                        mBinding.iDataFolderCheckBox.setBackgroundDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_number_bg));
                    } else {
                        mBinding.iDataFolderCheckBox.setVisibility(View.GONE);
                    }
                } else {
                    Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), iReaderMockData.isChecked() ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                    mBinding.iDataFolderCheckBox.setText("");
                    mBinding.iDataFolderCheckBox.setVisibility(View.VISIBLE);
                    mBinding.iDataFolderCheckBox.setBackgroundDrawable(drawable);
                }
            } else {
                mBinding.iDataFolderCheckBox.setVisibility(View.GONE);
            }*/
            //@TODO 群组或者非群组判断
            if (data instanceof DataGroup) { //群组
                if (((DataGroup) data).getChildCount() > 1){
                    mBinding.iDataFolderGrid.setVisibility(View.VISIBLE);
                    mBinding.iDataFolderContent.setVisibility(View.GONE);
                }else { //就1个 如果文件夹内只剩下一个文件，就取消文件夹状态
                    mBinding.iDataFolderGrid.setVisibility(View.INVISIBLE);
                    mBinding.iDataFolderContent.setBackgroundResource(((DataGroup) data).getChild(0).getImg());
                    mBinding.iDataFolderContent.setVisibility(View.VISIBLE);
                }
            } else { //非群组
                mBinding.iDataFolderGrid.setVisibility(View.INVISIBLE);
                mBinding.iDataFolderContent.setBackgroundResource(data.getImg());
                mBinding.iDataFolderContent.setVisibility(View.VISIBLE);
            }
        }
    }

    static class DataObservable extends Observable<DataObserver> {

        public boolean isRegister(DataObserver observer){
            return mObservers.contains(observer);
        }


        public void notifyItemCheckChanged(boolean isChecked) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChecked(isChecked);
            }
        }

        public void notifyItemEditModeChanged(boolean editMode) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onEditChanged(editMode);
            }
        }

        public void notifyItemRestore() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onRestore();
            }
        }

        public void notifyItemHideSubDialog(){
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onHideSubDialog();
            }
        }
    }

    public static abstract class DataObserver {
        public void onChecked(boolean isChecked) {

        }


        public void onEditChanged(boolean inEdit) {

        }

        public void onRestore() {

        }

        public void onHideSubDialog(){

        }
    }
}
