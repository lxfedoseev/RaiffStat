package com.example.alexfed.raiffstat;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ReportTabs extends SherlockFragmentActivity{

	private final String LOG = "ReportTabs";
	TabHost mTabHost;
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        setContentView(R.layout.fragment_tabs_pager);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();
        
        mViewPager = (ViewPager)findViewById(R.id.pager);

        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
        
        String dayTo = getCurrentDate();
        
        String dayFrom = getOneWeekBefore();
        Bundle dates = new Bundle();
        dates.putString("day_from", dayFrom);
        dates.putString("day_to", dayTo);  
        mTabsAdapter.addTab(mTabHost.newTabSpec("week").setIndicator(getResources().getString(R.string.str_week)),
        		ReportListAll.class, dates);
        
        dayFrom = getOneMonthBefore();
        dates = new Bundle();
        dates.putString("day_from", dayFrom);
        dates.putString("day_to", dayTo);
        mTabsAdapter.addTab(mTabHost.newTabSpec("month").setIndicator(getResources().getString(R.string.str_month)),
        		ReportListAll.class, dates);
        
        dayFrom = getOneYearBefore();
        dates = new Bundle();
        dates.putString("day_from", dayFrom);
        dates.putString("day_to", dayTo);
        mTabsAdapter.addTab(mTabHost.newTabSpec("year").setIndicator(getResources().getString(R.string.str_year)),
        		ReportListAll.class, dates);
        
        mTabsAdapter.addTab(mTabHost.newTabSpec("period").setIndicator(getResources().getString(R.string.str_period)),
        		ReportListAll.class, null);

        //setSelectedTabColor();
        
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
	}


	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
	
	private String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH)+1;
        return c.get(Calendar.DAY_OF_MONTH) + "/"+month+"/" + c.get(Calendar.YEAR);
    }
	
	private String getOneWeekBefore(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -7);
        int month = c.get(Calendar.MONTH)+1;
        return c.get(Calendar.DAY_OF_MONTH) + "/"+month+"/" + c.get(Calendar.YEAR);
	}
	
	private String getOneMonthBefore(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        int month = c.get(Calendar.MONTH)+1;
        return c.get(Calendar.DAY_OF_MONTH) + "/"+month+"/" + c.get(Calendar.YEAR);
	}
	
	private String getOneYearBefore(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        int month = c.get(Calendar.MONTH)+1;
        return c.get(Calendar.DAY_OF_MONTH) + "/"+month+"/" + c.get(Calendar.YEAR);
	}


	/**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    	private final String LOG = "TabsAdapter";
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        
        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
            //setSelectedTabColor();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
        
    	/*private void setSelectedTabColor() {
            for(int i=0;i<mTabHost.getTabWidget().getChildCount();i++)  
            {  
            	mTabHost.getTabWidget().getChildAt(i)
                                                .setBackgroundColor(Color.WHITE);  
            }  
            mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab())
                                                  .setBackgroundColor(Color.RED); 
        }*/
    }
}
