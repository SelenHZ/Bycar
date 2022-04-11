package com.lzhihua.bycar.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.lzhihua.bycar.R;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//实际发起网络请求的地方
public class NetworkUtil implements IHttpRequest{
    private OkHttpClient client;
    private Context mContext;
    private static Handler mHandler=new Handler();
    private static int cacheSize=10*1024*1024;//10 MB
    private static final int NETWORK_SUCCESS=1;//请求成功
    private static final int NETWORK_FALIED=2;//请求失败

    @Override
    public void init(Context context) {
        this.mContext=context.getApplicationContext();
        mHandler=getmHandler();
    }
    private static Handler getmHandler(){
        if (mHandler==null){
            mHandler=new Handler();
        }
        return mHandler;
    }
    @Override
    public void doGet(String url, NetWorkListener listener) {
        doGet(url,null,null,listener);
    }

    @Override
    public void doGet(String url, Map<String, String> params, NetWorkListener listener) {
        doGet(url,params,null,listener);
    }

    @Override
    public void doGet(String url, Map<String, String> params, NetworkRepo.OkhttpOption okhttpOption, NetWorkListener listener) {
        url=NetworkRepo.appendUri(url,params);
        Request.Builder builder=new Request.Builder().url(url).tag(okhttpOption.getTag());
        builder=configHeaders(builder,okhttpOption);
        Request request=builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleError(e,listener);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handlerResult(response,listener);
            }
        });
    }

    @Override
    public void doPost(String url, Map<String, String> params, NetWorkListener listener) {
        doPost(url,params,null,listener);
    }

    @Override
    public void doPost(String url, Map<String, String> params, NetworkRepo.OkhttpOption okhttpOption, NetWorkListener listener) {
        url=NetworkRepo.appendUri(url,params);
        FormBody.Builder builder=new FormBody.Builder();
        builder=configPostParams(builder,params);
        FormBody body=builder.build();
        Request.Builder requestBuilder=new Request.Builder().url(url).post(body).tag(okhttpOption);
        requestBuilder=configHeaders(requestBuilder,okhttpOption);
        Request request=requestBuilder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handleError(e,listener);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handlerResult(response,listener);
            }
        });
    }
    //post添加参数
    private FormBody.Builder configPostParams(FormBody.Builder builder,Map<String,String> params){
        if (params!=null){
            Set<Map.Entry<String,String>> entrySet=params.entrySet();
            for (Map.Entry<String,String> entry:entrySet){
                String key=entry.getKey();
                String val=entry.getValue();
                builder.add(key,val);
            }
        }
        return builder;
    }
    //添加Header
    private Request.Builder configHeaders(Request.Builder builder,NetworkRepo.OkhttpOption option){
        Map<String,String> headers=option.getParams();
        if (headers==null || headers.size()==0){
            return builder;
        }
        Set<Map.Entry<String,String>> entries=headers.entrySet();
        for (Map.Entry<String,String> entry:entries){
            String key=entry.getKey();
            String val=entry.getValue();
            builder.addHeader(key,val);
        }
        return builder;
    }
    @Override
    public void cancel(String tag) {
        if (client!=null){
            for (Call call:client.dispatcher().queuedCalls()){
                if (call.request().tag().equals(tag)){
                    call.cancel();
                }
            }
        }
    }
    private void handlerResult(Response response,final NetWorkListener listener)throws IOException{
        final String result=response.body().string();
        if (listener!=null){
            getmHandler().post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(result);
                }
            });
        }
    }
    private void handleError(IOException e,final NetWorkListener listener){
        if (listener!=null){
            listener.onFailed(e.getMessage());
        }
    }

    private static class NetworkUtilHolder{
        private static final NetworkUtil NetworkUtilInstance=new NetworkUtil();
    }
    public static NetworkUtil getInstance(){
        return NetworkUtilHolder.NetworkUtilInstance;
    }
    private NetworkUtil(){
        client=new OkHttpClient.Builder()
                .readTimeout(10000,TimeUnit.MILLISECONDS)
                .connectTimeout(10000,TimeUnit.MILLISECONDS)
                .cache(new Cache(mContext.getExternalFilesDir("okhttp"),cacheSize))
                .build();
    }
    public interface NetWorkListener{
        void onSuccess(String response);
        void onFailed(String errorMsg);
    }
}