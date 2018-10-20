package com.moonface.collabocode;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.duolingo.open.rtlviewpager.RtlViewPager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    FirebaseUser user;

    private RtlViewPager viewPager;
    BottomNavigationView navigation;
    private int[] item_ids = {R.id.navigation_projects, R.id.navigation_forum, R.id.navigation_learn};
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            viewPager.setCurrentItem(findIndex(item_ids, item.getItemId()));
            return true;
        }
    };
    private ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            navigation.setSelectedItemId(item_ids[position]);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigation = findViewById(R.id.main_bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        viewPager = findViewById(R.id.main_pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), new Fragment[]{new ProjectsFragment(), new ForumFragment(), new LearnFragment()});
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }
    private int findIndex(int[] objects, int object){
        for(int i=0; i<objects.length; i++){
            if(objects[i] == object) return i;
        }
        return 0;
    }
}
