package com.yyj.stydyroom.study.ui.tab;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.yyj.stydyroom.study.util.NonScrollViewPager;


public abstract class SlidingTabPagerAdapter extends FragmentPagerAdapter implements TabFragment.State,
        PagerSlidingTabStrip.OnTabClickListener, PagerSlidingTabStrip.OnTabDoubleTapListener {

    protected final TabFragment[] fragments;

    protected final Context context;

    private final NonScrollViewPager pager;

    public abstract int getCacheCount();

    private int lastPostion ;

    public SlidingTabPagerAdapter(FragmentManager fm, int count, Context context, NonScrollViewPager pager) {
        super(fm);
        this.fragments = new TabFragment[count];
        this.context = context;
        this.pager = pager;
        lastPostion = 0;
    }

    @Override
    public TabFragment getItem(int pos) {
        return fragments[pos];
    }

    @Override
    public abstract int getCount();

    @Override
    public abstract CharSequence getPageTitle(int position);

    @Override
    public boolean isCurrent(TabFragment f) {
        // FROM PAGER
        int current = pager.getCurrentItem();

        // TRAVEL
        for (int index = 0; index < fragments.length; index++) {
            // CATCH
            if (f == fragments[index]) {
                // MATCH
                if (index == current) {
                    return true;
                }
            }
        }

        // ANY PROBLEM
        return false;
    }

    public void onPageSelected(int position) {
        TabFragment fragment = getFragmentByPosition(position);

        // INSTANCE
        if (fragment == null) {
            return;
        }

        fragment.onCurrent();
        onLeave(position);
    }

    private void onLeave(int position) {
        TabFragment fragment = getFragmentByPosition(lastPostion);
        lastPostion = position;
        // INSTANCE
        if (fragment == null) {
            return;
        }
        fragment.onLeave();
    }

    public void onPageScrolled(int position) {
//		TabFragment fragment = getFragmentByPosition(position);
//
//		// INSTANCE
//		if (fragment == null) {
//			return;
//		}
//
//		fragment.onCurrentScrolled();
//		onLeave(position);
    }

    private TabFragment getFragmentByPosition(int position) {
        // IDX
        if (position < 0 || position >= fragments.length) {
            return null;
        }
        return fragments[position];
    }

    @Override
    public void onCurrentTabClicked(int position) {

        TabFragment fragment = getFragmentByPosition(position);

        // INSTANCE
        if (fragment == null) {
            return;
        }

        fragment.onCurrentTabClicked();
    }

    @Override
    public void onCurrentTabDoubleTap(int position) {
        TabFragment fragment = getFragmentByPosition(position);

        // INSTANCE
        if (fragment == null) {
            return;
        }

        fragment.onCurrentTabDoubleTap();
    }
}