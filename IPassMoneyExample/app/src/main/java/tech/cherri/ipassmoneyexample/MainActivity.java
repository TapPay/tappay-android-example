package tech.cherri.ipassmoneyexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import tech.cherri.tpdirect.api.TPDSetup;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String TAG_PREFIX = "f";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public static int CURRENT_POSITION = 0;
    public static String FRAGMENT_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipass_money);

        setupView();

        Toast.makeText(this, "SDK version is " + TPDSetup.getVersion(), Toast.LENGTH_SHORT).show();

        // Setup environment.
        TPDSetup.initInstance(getApplicationContext(), Constants.APP_ID, Constants.APP_KEY, Constants.SERVER_TYPE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "[IpassMoneyActivity] handleIncomingIntent, intent: " + intent.toString());

        CURRENT_POSITION = viewPager.getCurrentItem();
        IpassMoneyFragment ipassMoneyFragment =
                (IpassMoneyFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag());
        if (ipassMoneyFragment != null) {
            ipassMoneyFragment.handleIncomingIntent(intent);
        }
    }
    private void setupView() {
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new TabAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(this.getTabTitle(position))).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                CURRENT_POSITION = position;
                getFragmentTag();
            }
        });
    }

    private static class TabAdapter extends FragmentStateAdapter {
        public TabAdapter(@NonNull FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) return new IpassMoneyFragment(false);
            else return new IpassMoneyFragment(true);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private String getTabTitle(int position) {
        if (position == 0) return "EC";
        else return "BINDING";
    }

    private String getFragmentTag() {
        return FRAGMENT_TAG = TAG_PREFIX + CURRENT_POSITION;
    }

}