package com.wuj10n.mysimplebrowser;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Parameter;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity{
    private WebView webView;            //网页视图
    private ProgressBar progressBar;    //进度条
    private EditText textUrl;           //网址输入文本框
    private ImageView btnStart;         //图片按钮
    private  NavigationView navigationView;
    private long exitTime = 0;

    private Context mContext;
    private InputMethodManager manager;

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final int PRESS_BACK_EXIT_GAP = 2000;

    MyDataBaseHelper myDataBaseHelper;
    SQLiteDatabase mDatabase;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        myDataBaseHelper=new MyDataBaseHelper(MainActivity.this);
        mDatabase=myDataBaseHelper.getWritableDatabase();



        super.onCreate(savedInstanceState);
        // 防止底部按钮上移
        getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;
        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // 导航
        navigationView = findViewById(R.id.NavigationView_nav);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                WebSettings settings = webView.getSettings();
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                switch (item.getItemId()){
                    case R.id.item1:
                        Intent intent1=new Intent(MainActivity.this,HistoryActivity.class);
                        startActivityForResult(intent1,0x01);

                        break;
                    case R.id.item2:
                        Intent intent2=new Intent(MainActivity.this,FavouritesActivity.class);
                        startActivityForResult(intent2,0x03);
                        break;
                    case R.id.item3:
                        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        getWindow().setAttributes(lp);
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        break;
                    case R.id.item4:
                        lp.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        getWindow().setAttributes(lp);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                        break;
                    case R.id.item5:
                         settings.setBlockNetworkImage(true);
                         //无图模式开启 缺陷 图片不加载内容但是会仍显示空白的图片
                        break;
                    case R.id.item6:
                        settings.setBlockNetworkImage(false);
                        //无图模式关闭
                        break;
                    case R.id.item7:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    case R.id.item8:
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case R.id.item9:
                        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        break;
                    case R.id.item10:
                        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                        break;
                    default:
                }
                Toast.makeText(MainActivity.this,item.getTitle().toString(),Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        // 绑定控件
        initView();

        // 初始化 WebView
        initWeb();

    }

    private void initView() {
        //#############顶部组件#############
        textUrl=findViewById(R.id.editTextText_webUrl);
        btnStart=findViewById(R.id.imageButton_refresh);

        btnStart.setOnClickListener(view -> {
            if (textUrl.hasFocus()) {
                // 隐藏软键盘
                if (manager.isActive()) {
                    manager.hideSoftInputFromWindow(textUrl.getApplicationWindowToken(), 0);
                }

                // 地址栏有焦点，是跳转
                String input = textUrl.getText().toString();
                if (!isHttpUrl(input)) {
                    // 不是网址，加载搜索引擎处理
                    try {
                        // URL 编码
                        input = URLEncoder.encode(input, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    input = "https://www.baidu.com/s?wd=" + input + "&ie=UTF-8";
                }

                webView.loadUrl(input);



                // 取消掉地址栏的焦点
                textUrl.clearFocus();
            } else {
                // 地址栏没焦点，是刷新
                webView.reload();
            }
        });
        //#############顶部组件#############end

        //#############中间组件#############
        webView=findViewById(R.id.webView);
        progressBar=findViewById(R.id.progressBar);
        //#############中间组件#############end

        //#############底部组件############
        ImageView goBack = findViewById(R.id.imageButton_goBack);
        ImageView home=findViewById(R.id.imageButton_home);
        ImageView goForward = findViewById(R.id.imageButton_goForward);

        goBack.setOnClickListener(view -> webView.goBack());

        home.setOnClickListener(view -> {
            webView.loadUrl(getResources().getString(R.string.home_url));
        });


        goForward.setOnClickListener(view -> webView.goForward());
        //#############底部组件############end

        // 地址输入栏获取与失去焦点处理
        textUrl.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // 显示当前网址链接 TODO:搜索页面显示搜索词
                textUrl.setText(webView.getUrl());
                // 光标置于末尾
                textUrl.setSelection(textUrl.getText().length());
                // 显示因特网图标
                //webIcon.setImageResource(R.drawable.internet);
                // 显示跳转按钮
                btnStart.setImageResource(R.drawable.go);
            } else {
                // 显示网站名
                textUrl.setText(webView.getTitle());
                // 显示网站图标
                //  webIcon.setImageBitmap(webView.getFavicon());
                // 显示刷新按钮
                btnStart.setImageResource(R.drawable.refresh);
            }
        });

        // 监听键盘回车搜索
        textUrl.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                // 执行搜索
                btnStart.callOnClick();
                textUrl.clearFocus();
            }
            return false;
        });

    }

    /**
     * 初始化 web
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWeb() {
        // 重写 WebViewClient
        webView.setWebViewClient(new MkWebViewClient());
        // 重写 WebChromeClient
        webView.setWebChromeClient(new MkWebChromeClient());

        WebSettings settings = webView.getSettings();
        // 启用 js 功能
        settings.setJavaScriptEnabled(true);
        // 设置浏览器 UserAgent
        settings.setUserAgentString(settings.getUserAgentString() + " mkBrowser/" + getVerName(mContext));

        // 将图片调整到适合 WebView 的大小
        settings.setUseWideViewPort(true);
        // 缩放至屏幕的大小
        settings.setLoadWithOverviewMode(true);

        // 支持缩放，默认为true。是下面那个的前提。
        settings.setSupportZoom(true);
        // 设置内置的缩放控件。若为false，则该 WebView 不可缩放
        settings.setBuiltInZoomControls(true);
        // 隐藏原生的缩放控件
        settings.setDisplayZoomControls(false);

        // 缓存
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 设置可以访问文件
        settings.setAllowFileAccess(true);
        // 支持通过JS打开新窗口
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 支持自动加载图片
        settings.setLoadsImagesAutomatically(true);
        // 设置默认编码格式
        settings.setDefaultTextEncodingName("utf-8");
        // 本地存储
        settings.setDomStorageEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        // 资源混合模式
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 加载首页
        webView.loadUrl(getResources().getString(R.string.home_url));
    }

    /**
     * 重写 WebViewClient
     */
    private class MkWebViewClient extends WebViewClient {
        boolean if_load;
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if_load=false;

            // 设置在webView点击打开的新网页在当前界面显示,而不跳转到新的浏览器中

            if (url == null) {
                // 返回true自己处理，返回false不处理
                return true;
            }

            // 正常的内容，打开
            if (url.startsWith(HTTP) || url.startsWith(HTTPS)) {
                view.loadUrl(url);
                return true;
            }

            // 调用第三方应用，防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
            try {
                // TODO:弹窗提示用户，允许后再调用
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            } catch (Exception e) {
                return true;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if_load=true;
            // 网页开始加载，显示进度条
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);

            // 更新状态文字
            textUrl.setText("加载中...");

            // 切换默认网页图标
            //webIcon.setImageResource(R.drawable.internet);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            if(if_load) {
                addinfo_history(view.copyBackForwardList().getCurrentItem().getTitle(), view.copyBackForwardList().getCurrentItem().getUrl());
                if_load=false;
            }
            super.onPageFinished(view, url);
            // 网页加载完毕，隐藏进度条
            progressBar.setVisibility(View.INVISIBLE);

            // 改变标题
            setTitle(webView.getTitle());
            // 显示页面标题
            textUrl.setText(webView.getTitle());
        }
    }

    /**
     * 重写 WebChromeClient
     */
    private class MkWebChromeClient extends WebChromeClient {
        private final static int WEB_PROGRESS_MAX = 100;
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);

            // 加载进度变动，刷新进度条
            progressBar.setProgress(newProgress);
            if (newProgress > 0) {
                if (newProgress == WEB_PROGRESS_MAX) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);

            // 改变标题
            setTitle(title);
            // 显示页面标题
            textUrl.setText(title);
        }
    }
    /**
     * 返回按钮处理
     */
    @Override
    public void onBackPressed() {
        // 能够返回则返回上一页
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if ((System.currentTimeMillis() - exitTime) > PRESS_BACK_EXIT_GAP) {
                // 连点两次退出程序
                Toast.makeText(mContext, "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            webView.getClass().getMethod("onPause").invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            webView.getClass().getMethod("onResume").invoke(webView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断字符串是否为URL（https://blog.csdn.net/bronna/article/details/77529145）
     *
     * @param urls 要勘定的字符串
     * @return true:是URL、false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        boolean isUrl;
        // 判断是否是网址的正则表达式
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";

        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        isUrl = mat.matches();
        return isUrl;
    }
    /**
     * 获取版本号名称
     *
     * @param context 上下文
     * @return 当前版本名称
     */
    private static String getVerName(Context context) {
        String verName = "unKnow";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    //数据库添加数据（历史记录表）
    public void addinfo_history( String title,String url){
        //第二个参数是数据库名
        //数据库
        String sql = "insert into history values(null,?,?)";
        mDatabase.execSQL(sql, new String[]{title, url});

        Toast.makeText(MainActivity.this,"历史记录添加成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDatabase!=null){
            mDatabase.close();
        }
        if (myDataBaseHelper!=null) {
            myDataBaseHelper.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == 0x02) {
            String result = data.getStringExtra("URL");
            webView.loadUrl(result);
        }else if(requestCode == 0x03 && resultCode == 0x04){
            String result = data.getStringExtra("URL");
            webView.loadUrl(result);
        }
    }
    public void dialog1(View view) {
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_edittext,
                (ViewGroup) findViewById(R.id.item_lin_ed));
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("添加至收藏夹");
        inputDialog.setView(layout);
        EditText item_Title=layout.findViewById(R.id.item_addBookmarkTitle);
        EditText item_Url=layout.findViewById(R.id.item_addBookmarkUrl);
        Spinner category=layout.findViewById(R.id.spinner_category);
        item_Title.setText(webView.getTitle());
        item_Url.setText(webView.getUrl());

        inputDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addinfo(item_Title.getText().toString(),item_Url.getText().toString(),category.getSelectedItem().toString());
            }
        });
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Toast.makeText(MainActivity.this, "关闭对话框", Toast.LENGTH_SHORT).show();
            }
        });
        inputDialog.create().show();
    }
    //数据库添加数据(书签表)
    public void addinfo(String title,String url,String category){
        try{
            String sql = "insert into bookmark values(null,?,?,?)";
            mDatabase.execSQL(sql, new String[]{title, url,category});
            Toast.makeText(this,"添加收藏夹成功",Toast.LENGTH_SHORT).show();
        }catch (SQLiteConstraintException e){
            Toast.makeText(this,"网址已添加",Toast.LENGTH_SHORT).show();
        }

    }
}