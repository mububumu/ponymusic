package me.wcy.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import me.wcy.music.R;
import me.wcy.music.adapter.FragmentAdapter;
import me.wcy.music.constants.Extras;
import me.wcy.music.constants.Keys;
import me.wcy.music.executor.ControlPanel;
import me.wcy.music.executor.NaviMenuExecutor;
import me.wcy.music.executor.WeatherExecutor;
import me.wcy.music.fragment.LocalMusicFragment;
import me.wcy.music.fragment.PlayFragment;
import me.wcy.music.fragment.SheetListFragment;
import me.wcy.music.fragment.SportMusicFragment;
import me.wcy.music.fragment.SportMusicFragment;
import me.wcy.music.service.AudioPlayer;
import me.wcy.music.service.QuitTimer;
import me.wcy.music.utils.PermissionReq;
import me.wcy.music.utils.SystemUtils;
import me.wcy.music.utils.ToastUtils;
import me.wcy.music.utils.binding.Bind;

public class MusicActivity extends BaseActivity implements View.OnClickListener, QuitTimer.OnTimerListener,
        NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    @Bind(R.id.drawer_layout)
    private DrawerLayout drawerLayout;       // 抽屉式布局
    @Bind(R.id.navigation_view)
    private NavigationView navigationView;   // 导航栏
    @Bind(R.id.iv_menu)
    private ImageView ivMenu;                // 菜单选项
    @Bind(R.id.iv_search)
    private ImageView ivSearch;              // 搜索选项

    @Bind(R.id.tv_local_music)
    private TextView tvLocalMusic;
    @Bind(R.id.tv_online_music)
    private TextView tvOnlineMusic;
    @Bind(R.id.tv_sport_music)
    private TextView tvSportMusic;
    @Bind(R.id.tv_dance_music)
    private TextView tvDanceMusic;

    @Bind(R.id.viewpager)
    private ViewPager mViewPager;
    @Bind(R.id.fl_play_bar)
    private FrameLayout flPlayBar;          // 帧布局

    private View vNavigationHeader;

    private LocalMusicFragment mLocalMusicFragment;
    private SheetListFragment mSheetListFragment;
    private SportMusicFragment mSportMusicFragment;
    private PlayFragment mPlayFragment;

    private ControlPanel controlPanel;          // 播放控制面板（界面底部，播放/停止、下一首、播放列表）
    private NaviMenuExecutor naviMenuExecutor;  // 导航菜单执行器
    private MenuItem timerItem;

    private boolean isPlayFragmentShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
    }

    @Override
    protected void onServiceBound() {
        setupView();
        updateWeather();
        controlPanel = new ControlPanel(flPlayBar);
        naviMenuExecutor = new NaviMenuExecutor(this);
        AudioPlayer.get().addOnPlayEventListener(controlPanel);
        QuitTimer.get().setOnTimerListener(this);                   // 定时停止播放
        parseIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void setupView() {
        // add navigation header
        vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);

        // setup view pager
        mLocalMusicFragment = new LocalMusicFragment();
        mSheetListFragment = new SheetListFragment();
        mSportMusicFragment = new SportMusicFragment();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(mLocalMusicFragment);
        adapter.addFragment(mSheetListFragment);
        adapter.addFragment(mSportMusicFragment);
        mViewPager.setAdapter(adapter);
        tvLocalMusic.setSelected(true);

        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        tvLocalMusic.setOnClickListener(this);
        tvOnlineMusic.setOnClickListener(this);
        tvSportMusic.setOnClickListener(this);
        flPlayBar.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(this);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // 更新天气信息
    private void updateWeather() {
        // 请求位置权限
        PermissionReq.with(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .result(new PermissionReq.Result() {
                    @Override
                    public void onGranted() {   // 获得权限
                        new WeatherExecutor(MusicActivity.this, vNavigationHeader).execute();
                    }

                    @Override                   // 拒绝
                    public void onDenied() {
                        ToastUtils.show(R.string.no_permission_location);
                    }
                })
                .request();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    @Override
    public void onTimer(long remain) {
        if (timerItem == null) {
            timerItem = navigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchMusicActivity.class));
                break;
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_online_music:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawers();
        handler.postDelayed(() -> item.setChecked(false), 500);
        return naviMenuExecutor.onNavigationItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            tvLocalMusic.setSelected(true);
            tvOnlineMusic.setSelected(false);
            tvSportMusic.setSelected(false);
        }
        else if (position == 1) {
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(true);
            tvSportMusic.setSelected(false);
        }
        else if (position == 2) {
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(false);
            tvSportMusic.setSelected(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    // 展示正在播放界面
    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    // 隐藏正在播放界面
    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    @Override
    public void onBackPressed() {
        // 若处于正在播放界面
        if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    // APP 被销毁时将数据保存至 Bundle
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Keys.VIEW_PAGER_INDEX, mViewPager.getCurrentItem());
        mLocalMusicFragment.onSaveInstanceState(outState);
        mSheetListFragment.onSaveInstanceState(outState);
        mSportMusicFragment.onSaveInstanceState(outState);
    }

    // 从 Bundle 中恢复数据
    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(() -> {
            mViewPager.setCurrentItem(savedInstanceState.getInt(Keys.VIEW_PAGER_INDEX), false);
            mLocalMusicFragment.onRestoreInstanceState(savedInstanceState);
            mSheetListFragment.onRestoreInstanceState(savedInstanceState);
            mSportMusicFragment.onSaveInstanceState(savedInstanceState);
        });
    }

    @Override
    protected void onDestroy() {
        AudioPlayer.get().removeOnPlayEventListener(controlPanel);
        QuitTimer.get().setOnTimerListener(null);
        super.onDestroy();
    }
}
