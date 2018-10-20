package com.moonface.collabocode;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.duolingo.open.rtlviewpager.RtlViewPager;

public class ProjectsFragment extends Fragment {
    private RtlViewPager viewPager;
    TabLayout navigation;
    private TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener()
    {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        navigation = view.findViewById(R.id.projects_tabs_navigation);
        navigation.addOnTabSelectedListener(onTabSelectedListener);
        viewPager = view.findViewById(R.id.projects_pager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(navigation));
        final PagerAdapter adapter = new PagerAdapter(getFragmentManager(), new Fragment[]{new MyProjectsFragment(), new SharedWithMeFragment()});
        viewPager.setAdapter(adapter);
    }

}
