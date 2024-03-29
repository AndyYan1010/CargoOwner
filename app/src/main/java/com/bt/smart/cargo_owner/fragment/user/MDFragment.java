package com.bt.smart.cargo_owner.fragment.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bt.smart.cargo_owner.MyApplication;
import com.bt.smart.cargo_owner.NetConfig;
import com.bt.smart.cargo_owner.R;
import com.bt.smart.cargo_owner.adapter.MDetailAdapter;
import com.bt.smart.cargo_owner.messageInfo.MDetailInfo;
import com.bt.smart.cargo_owner.utils.HttpOkhUtils;
import com.bt.smart.cargo_owner.utils.ProgressDialogUtil;
import com.bt.smart.cargo_owner.utils.RequestParamsFM;
import com.bt.smart.cargo_owner.utils.ToastUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

public class MDFragment extends Fragment {
    private View view;
    private ListView lv_de;
    private List<MDetailInfo.DataBean> list;
    private MDetailAdapter adapter;
    private String Tag = "MDFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_md, container, false);
        setViews();
        setListeners();
        return view;
    }

    protected void setViews(){
        lv_de = view.findViewById(R.id.lv_de);
        list = new ArrayList<>();
        getMDetail();
    }

    protected void setListeners(){
        lv_de.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MDeFragment mDeFragment = new MDeFragment();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.md_ll,mDeFragment);
                Bundle bundle = new Bundle();
                try {
                    MDetailInfo.DataBean data = list.get(i);
                    Class cls = data.getClass();
                    Field[] fields = cls.getDeclaredFields();
                    for (int j = 0; j < fields.length; j++) {
                        Field f = fields[j];
                        f.setAccessible(true);
                        System.out.println("属性名:" + f.getName() + " 属性值:" + f.get(data));
                        bundle.putString(f.getName(),String.valueOf(f.get(data)));
                    }
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
                mDeFragment.setArguments(bundle);
                ft.commit();
            }
        });
    }

    private void getMDetail() {
        ProgressDialogUtil.startShow(getContext(), "正在获取详情...");
        RequestParamsFM headParams = new RequestParamsFM();
        headParams.put("X-AUTH-TOKEN", MyApplication.userToken);
        HttpOkhUtils.getInstance().doGetWithOnlyHeader(NetConfig.PAYACCOUNTDRIVER_LIST + "/" + MyApplication.userID, headParams, new HttpOkhUtils.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                ProgressDialogUtil.hideDialog();
                ToastUtils.showToast(getContext(), "网络连接错误");
            }

            @Override
            public void onSuccess(int code, String resbody) {
                ProgressDialogUtil.hideDialog();
                if (code != 200) {
                    ToastUtils.showToast(getContext(), "网络错误" + code);
                    return;
                }
                Gson gson = new Gson();
                Log.i(Tag,resbody);
                MDetailInfo info = gson.fromJson(resbody, MDetailInfo.class);
                Log.i(Tag,info.getMessage());
                if (info.isOk()) {
                    list = info.getData();
                    adapter = new MDetailAdapter(getActivity(),list);
                    lv_de.setAdapter(adapter);
                }
            }
        });
    }
}
