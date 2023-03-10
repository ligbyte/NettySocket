package yunbo.nettyclient;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yunbo.netty.client.listener.MessageStateListener;
import com.yunbo.netty.client.listener.NettyClientListener;
import com.yunbo.netty.client.NettyTcpClient;
import com.yunbo.netty.client.status.ConnectState;

import yunbo.nettyclient.adapter.LogAdapter;
import yunbo.nettyclient.bean.LogBean;

public class SocketClientActivity extends AppCompatActivity implements View.OnClickListener, NettyClientListener<String> {

    private static final String TAG = "MainActivity";
    private Button mClearLog;
    private Button mSendBtn;
    private Button mConnect;
    private EditText mSendET;
    private EditText et_ip;
    private RecyclerView mSendList;
    private RecyclerView mReceList;

    private LogAdapter mSendLogAdapter = new LogAdapter();
    private LogAdapter mReceLogAdapter = new LogAdapter();
    private NettyTcpClient mNettyTcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_client);
        findViews();
        initView();

    }

    private void initView() {
        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSendList.setLayoutManager(manager1);
        mSendList.setAdapter(mSendLogAdapter);

        LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReceList.setLayoutManager(manager2);
        mReceList.setAdapter(mReceLogAdapter);

    }

    private void findViews() {
        mSendList = findViewById(R.id.send_list);
        mReceList = findViewById(R.id.rece_list);
        mSendET = findViewById(R.id.send_et);
        mConnect = findViewById(R.id.connect);
        mSendBtn = findViewById(R.id.send_btn);
        mClearLog = findViewById(R.id.clear_log);
        et_ip = findViewById(R.id.et_ip);
        mConnect.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mClearLog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.connect:
                String host = et_ip.getText().toString().trim();
                if (TextUtils.isEmpty(host)){
                    Toast.makeText(SocketClientActivity.this, "???????????????????????????IP??????", Toast.LENGTH_SHORT).show();
                    return;
                }
                mNettyTcpClient = new NettyTcpClient.Builder()
                        .setHost(Const.HOST)    //?????????????????????
                        .setTcpPort(Const.TCP_PORT) //????????????????????????
                        .setMaxReconnectTimes(3)    //????????????????????????
                        .setReconnectIntervalTime(5)    //???????????????????????????????????????
                        .setSendheartBeat(false) //????????????????????????
                        .setHeartBeatInterval(5)    //???????????????????????????????????????
                        .setHeartBeatData("I'm is HeartBeatData") //??????????????????????????????String?????????????????????byte[]????????????????????????
                        .setIndex(0)    //?????????????????????.(????????????????????????tcp??????)
//                .setPacketSeparator("#")//?????????????????????????????????????????????????????????????????????????????????????????????
                        .setMaxPacketLong(1024)//???????????????????????????????????????????????????1024
                        .build();

                mNettyTcpClient.setListener(SocketClientActivity.this); //??????TCP??????
                connect();
                break;

            case R.id.send_btn:
                if (!mNettyTcpClient.getConnectStatus()) {
                    Toast.makeText(getApplicationContext(), "?????????,????????????", Toast.LENGTH_SHORT).show();
                } else {
                    final String msg = mSendET.getText().toString();
                    if (TextUtils.isEmpty(msg.trim())) {
                        return;
                    }
                    mNettyTcpClient.sendMsgToServer(msg, new MessageStateListener() {
                        @Override
                        public void isSendSuccss(boolean isSuccess) {
                            if (isSuccess) {
                                Log.d(TAG, "Write auth successful");
                                logSend(msg);
                            } else {
                                Log.d(TAG, "Write auth error");
                            }
                        }
                    });
                    mSendET.setText("");
                }

                break;

            case R.id.clear_log:
                mReceLogAdapter.getDataList().clear();
                mSendLogAdapter.getDataList().clear();
                mReceLogAdapter.notifyDataSetChanged();
                mSendLogAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void connect() {
        Log.d(TAG, "connect");
        if (!mNettyTcpClient.getConnectStatus()) {
            mNettyTcpClient.connect();//???????????????
        } else {
            mNettyTcpClient.disconnect();
        }
    }

    @Override
    public void onMessageResponseClient(String msg, int index) {
        Log.e(TAG, "onMessageResponse:" + msg);
        logRece(index + ":" + msg);
    }

    @Override
    public void onClientStatusConnectChanged(final int statusCode, final int index) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (statusCode == ConnectState.STATUS_CONNECT_SUCCESS) {
                    Log.e(TAG, "STATUS_CONNECT_SUCCESS:");
                    mConnect.setText("DisConnect:" + index);
                } else {
                    Log.e(TAG, "onServiceStatusConnectChanged:" + statusCode);
                    mConnect.setText("Connect:" + index);
                }
            }
        });

    }

    private void logSend(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mSendLogAdapter.getDataList().add(0, logBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendLogAdapter.notifyDataSetChanged();
            }
        });

    }

    private void logRece(String log) {
        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
        mReceLogAdapter.getDataList().add(0, logBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceLogAdapter.notifyDataSetChanged();
            }
        });

    }

    /**
     * ????????????
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes, int length) {
        StringBuilder buf = new StringBuilder(length * 2);
        for (int i = 0; i < length; i++) {// ??????String???format??????????????????
            buf.append(String.format("%02x", new Integer(bytes[i] & 0xFF)));
        }
        return buf.toString();
    }

    public void disconnect(View view) {
        mNettyTcpClient.disconnect();
    }
}
