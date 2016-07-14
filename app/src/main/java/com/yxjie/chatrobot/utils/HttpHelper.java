package com.yxjie.chatrobot.utils;

import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpHelper {

    /**
     * GET 方法
     */
    public static final String GET = "GET";

    /**
     * POST方法
     */
    public static final String POST = "POST";

    /**
     * 连接超时
     */
    private static final long CONNECT_TIMEOUT = 30 * 1000;
    /**
     * 读取超时
     */
    private static final int READ_TIMEOUT = 30 * 1000;

    /**
     * GET请求类型
     */
    private static final int REQUEST_TYPE_GET = 0x01;

    /**
     * POST请求类型
     */
    private static final int REQUEST_TYPE_POST = REQUEST_TYPE_GET + 1;

    private static HttpHelper sInstance;

    private final OkHttpClient mClient;


    /**
     * 网络事件监听器
     */
    public interface OnNetEventListener {

        /**
         * 请求成功, 执行在非UI线程中
         *
         * @param content 响应内容
         */
        @WorkerThread
        void onSuccess(String content);

        /**
         * 请求失败, 执行在非UI线程中
         *
         * @param msg 错误消息
         */
        @WorkerThread
        void onFailure(String msg);
    }


    public static HttpHelper getInstance() {
        synchronized (HttpHelper.class) {
            if (sInstance == null) {
                sInstance = new HttpHelper();
            }
            return sInstance;
        }
    }

    private HttpHelper() {
        //配置okhttp
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);//设置连接超时
        builder.readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);//设置读取超时
        mClient = builder.build();
    }



    /**
     * 发起GET请求
     *
     * @return NetworkRequest
     */
    public NetworkRequest doGet() {
        NetworkRequest request = new NetworkRequest(REQUEST_TYPE_GET, mClient);
        return request;
    }

    /**
     * 发起POST请求
     *
     * @return NetworkRequest
     */
    public NetworkRequest doPost() {
        NetworkRequest request = new NetworkRequest(REQUEST_TYPE_POST, mClient);
        return request;
    }

    /**
     * 停止所有网络请求
     */
    public void stopAll() {
        mClient.dispatcher().cancelAll();
    }


    /**
     * 网络请求回调
     */
    private static class NetEventCallback implements Callback {

        private final OnNetEventListener mListener;

        public NetEventCallback(OnNetEventListener listener) {
            mListener = listener;
        }

        @Override
        public void onFailure(Call call, IOException e) {

            String errorMsg = e.getMessage();
            if (errorMsg == null) {
                errorMsg = "Unknown exception";
            }

            //如果是用户取消，就不做处理
            if (call.isCanceled()) {
                return;
            }
            if (mListener != null) {
                mListener.onFailure(errorMsg);
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            try {

                if (response.isSuccessful()) {

                    String content = response.body().string();

                    if (mListener != null) {
                        mListener.onSuccess(content);
                    }
                } else {
                    int code = response.code();
                    String message = response.message();
                    String errorMsg = String.format("code: %d, message: %s", code, message);
                    if (mListener != null) {
                        mListener.onFailure(errorMsg);
                    }
                }
            } finally {
                response.body().close();
            }
        }


    }


    public static class NetworkRequest {

        /**
         * 请求方法
         */
        private int mRequestType;

        /**
         * url
         */
        private String mUrl;

        /**
         * 请求头
         */
        private Map<String, String> mHeaders;

        /**
         * POST请求体
         */
        private Map<String, String> mBodies;

        /**
         * TAG
         */
        private Object mTag;

        /**
         * 网络监听
         */
        private OnNetEventListener mListener;


        private final OkHttpClient mClient;

        private Call mCall;

        /**
         * 构造方法
         *
         * @param requestType REQUEST_TYPE_GET or REQUEST_TYPE_POST
         * @param client      client
         */
        public NetworkRequest(int requestType, OkHttpClient client) {

            if (client == null) {
                throw new IllegalArgumentException("client must not be null!");
            }

            if (requestType != REQUEST_TYPE_GET && requestType != REQUEST_TYPE_POST) {
                throw new IllegalArgumentException("requestType is not supported");
            }

            mRequestType = requestType;
            mClient = client;
        }

        public NetworkRequest url(String url) {
            mUrl = url;
            return this;
        }

        public NetworkRequest headers(Map<String, String> headers) {
            mHeaders = headers;
            return this;
        }

        public NetworkRequest bodies(Map<String, String> bodies) {
            mBodies = bodies;
            return this;
        }

        public NetworkRequest tag(Object tag) {
            mTag = tag;
            return this;
        }

        public NetworkRequest listener(OnNetEventListener listener) {
            mListener = listener;
            return this;
        }


        /**
         * 发起请求
         */
        public NetworkRequest request() {

            Request request = makeRequest(mRequestType, mUrl, mTag, mHeaders, mBodies);
            enqueueRequestWithListener(request, mListener);

            return this;
        }

        /**
         * 取消请求
         */
        public void cancelRequest() {
            if (mCall != null) {
                mCall.cancel();
            }
        }


        /**
         * 创建request
         *
         * @param url     网址
         * @param tag     标签，用于取消请求，可以为空
         * @param headers header
         * @param bodies  post请求
         * @return 请求
         */
        private Request makeRequest(int requestType, String url, Object tag, Map<String, String> headers, Map<String, String> bodies) {

            Request.Builder builder = new Request.Builder();

            //添加header信息
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            if (requestType == REQUEST_TYPE_GET) {
                //no-ops
                builder.method(GET, null);
            } else if (requestType == REQUEST_TYPE_POST) {
                //设置body
                FormBody.Builder formBuilder = new FormBody.Builder();

                //添加提交字段
                if (bodies != null) {
                    for (Map.Entry<String, String> entry : bodies.entrySet()) {
                        formBuilder.add(entry.getKey(), entry.getValue());
                    }
                }

                RequestBody requestBody = formBuilder.build();
                builder.method(POST, requestBody);
            } else {
                throw new UnsupportedOperationException("Unsupported request type");
            }

            builder.url(url);

            //设置tag，可以用来取消请求
            builder.tag(tag);

            return builder.build();
        }


        /**
         * 把request放入队列中
         *
         * @param request  请求
         * @param listener 监听
         */
        private void enqueueRequestWithListener(Request request, final OnNetEventListener listener) {

            if (request == null) {
                throw new IllegalArgumentException("request must not be null!");
            }

            Callback callback = createCallback(listener);

            //网络请求
            mCall = mClient.newCall(request);
            mCall.enqueue(callback);
        }


        /**
         * 创建回调
         *
         * @param listener 回调
         * @return 回调
         */
        private Callback createCallback(OnNetEventListener listener) {
            return new NetEventCallback(listener);
        }
    }

}
