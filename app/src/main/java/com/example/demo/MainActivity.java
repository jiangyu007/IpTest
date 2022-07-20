package com.example.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvIpMy;
    private EditText etIpPing;
    private EditText etUrl;
    private ProgressBar pb;
    private Button btIp;
    private Button btPing;
    private Button btUrl;
    private TextView tvIpDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initIp();
    }

    private void initIp() {
        try {
            pb.setVisibility(View.VISIBLE);
            tvIpMy.setText("");
            StringBuffer address = new StringBuffer();
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            LinkedList<InetAddress> adds = new LinkedList<>();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement());
                }
            }
            for (InetAddress add : adds) {
                if (!add.isLoopbackAddress()) {
                    String hostAddress = add.getHostAddress();
                    boolean isIPv4 = hostAddress.indexOf(':') < 0;
                    if (isIPv4) {
                        address.append(hostAddress).append("\n");
                    }
                }
            }
            tvIpMy.setText(address);
            x.http().get(new RequestParams("https://ip.cn/api/index?ip=&type=0"), new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    tvIpDesc.setText(result);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ToastUtils.showShort("请求错误");
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    ToastUtils.showShort("请求取消");
                }

                @Override
                public void onFinished() {
                    pb.setVisibility(View.GONE);
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
            tvIpMy.setText("异常");
            pb.setVisibility(View.GONE);
        }
    }

    private void initView() {
        tvIpMy = findViewById(R.id.tv_ip_my);
        etIpPing = findViewById(R.id.et_ip_ping);
        etUrl = findViewById(R.id.et_url);
        btPing = findViewById(R.id.bt_ping);
        btIp = findViewById(R.id.bt_ip);
        btUrl = findViewById(R.id.bt_url);
        tvIpDesc = findViewById(R.id.tv_ip_desc);
        pb = findViewById(R.id.pb);
        btPing.setOnClickListener(this);
        btUrl.setOnClickListener(this);
        btIp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btPing) {
            tvIpDesc.setText("");
            pb.setVisibility(View.VISIBLE);
            ShellUtils.execCmdAsync("ping -c 5 " + etIpPing.getText().toString(), false,
                    new Utils.Consumer<ShellUtils.CommandResult>() {
                        @Override
                        public void accept(ShellUtils.CommandResult commandResult) {
                            tvIpDesc.setText(commandResult.toString());
                            pb.setVisibility(View.GONE);
                        }
                    });
        } else if (v == btUrl) {
            String url = etUrl.getText().toString();
            tvIpDesc.setText("正在发送http请求");
            pb.setVisibility(View.VISIBLE);
            final long timeMillis = System.currentTimeMillis();
            x.http().get(new RequestParams(url), new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    long time = System.currentTimeMillis() - timeMillis;
                    tvIpDesc.setText("加载完毕，时间为" + time + "毫秒");
                    String desc = tvIpDesc.getText().toString();
                    desc = desc + "\n" + result;
                    tvIpDesc.setText(desc);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ToastUtils.showShort("请求错误");
                }

                @Override
                public void onCancelled(CancelledException cex) {
                    ToastUtils.showShort("请求取消");
                }

                @Override
                public void onFinished() {
                    pb.setVisibility(View.GONE);
                }
            });
        } else if (v == btIp) {
            initIp();
        }
    }
}