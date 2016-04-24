package apps.gen.lib.controllers;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;

import apps.gen.lib.utils.Configs;
import apps.gen.lib.utils.H;
import apps.gen.lib.views.TabBar;
import apps.gen.lib.R;

/**
 * Created by gen on 16/4/22.
 */
public class TabController extends Controller {
    class TabControllerView extends RelativeLayout {
        final int DEFAULT_TAB_BAR_HEIGHT = 52;

        TabBar mTabBar;
        TabBar getTabBar() {
            return mTabBar;
        }
        RelativeLayout mContentView;
        RelativeLayout getContentView() {
            return mContentView;
        }

        boolean isInitView = false;

        public TabControllerView(Context context) {
            this(context, null);
        }

        public TabControllerView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public TabControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            isInitView = true;
            initView(context);
            isInitView = false;
        }

        void initView(Context context) {
            mContentView = new RelativeLayout(context);
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mContentView.setLayoutParams(layoutParams);
            mContentView.setPadding(0, 0, 0, H.dip2px(context, DEFAULT_TAB_BAR_HEIGHT));
            addView(mContentView);

            RelativeLayout view = new RelativeLayout(context);
            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(layoutParams);
            view.setId(R.id.tab_content);
            mContentView.addView(view);

            mTabBar = new TabBar(context);
            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, H.dip2px(context, DEFAULT_TAB_BAR_HEIGHT));
            layoutParams.setMargins(0, 0, 0, 0);
            layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
            mTabBar.setLayoutParams(layoutParams);
            mTabBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            addView(mTabBar);
        }

        @Override
        public void addView(View child) {
            if (isInitView) {
                super.addView(child);
            }else {
                mContentView.addView(child);
            }
        }
    }

    public static class TabItem {
        Controller mController;
        TabBar.TabItem mTabItem;
        public void setController(Controller controller) {
            mController = controller;
        }
        public Controller getController() {
            return mController;
        }
        public void setTabItem(TabBar.TabItem tabItem) {
            mTabItem = tabItem;
        }
        public TabBar.TabItem getTabItem() {
            return mTabItem;
        }
        public TabItem(Drawable icon, String label, Controller controller) {
            mTabItem = new TabBar.TabItem(label, icon);
            mController = controller;
        }
    }

    TabControllerView tabView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tabView = new TabControllerView(getContext());
        tabView.getTabBar().setOnClickListener(new TabBar.OnClickListener() {
            @Override
            public void onClick(View v, int index) {
                if (index < mItems.size())
                    select(index, true);
            }
        });
        return tabView;
    }

    ArrayList<TabItem> mItems = new ArrayList<>();
    int mIndex;
    public int getIndex() {
        return mIndex;
    }

    public void setItems(TabItem[] items) {
        for (TabItem item: mItems) {
            item.getController().setParent(null);
        }
        mItems.clear();
        Collections.addAll(mItems, items);
        updateItems();
    }
    public ArrayList<TabItem> getItems() {
        return mItems;
    }

    enum AnimationType {
        NONE,
        PUSH_LEFT,
        PUSH_RIGHT
    }

    public void select(int index) {select(index, false);}
    public void select(int index, boolean animated) {
        if (mIndex != index) {
            if (animating) return;
            setCurrentController(mItems.get(index).getController(), animated ? (mIndex < index ? AnimationType.PUSH_LEFT:AnimationType.PUSH_RIGHT) : AnimationType.NONE);
            mIndex = index;
        }
    }

    boolean animating = false;
    void updateItems() {
        if (mItems.size() > 0) {
            TabBar.TabItem[] items = new TabBar.TabItem[mItems.size()];
            int n = 0;
            for (TabItem item : mItems) {
                items[n++] = item.getTabItem();
                item.getController().setParent(this);
            }
            tabView.getTabBar().setItems(items);
            mIndex = 0;
            setCurrentController(mItems.get(mIndex).getController(), AnimationType.NONE);
        }
    }
    Controller currentController;
    void setCurrentController(Controller controller, AnimationType type) {
        if (animating || currentController == controller) return;
        if (type != AnimationType.NONE) animating = true;
        switch (type) {
            case NONE:
            {
                if (currentController != null) currentController.exitStart();
                controller.enterStart();
                getFragmentManager().beginTransaction().replace(R.id.tab_content, controller).commit();
                if (currentController != null) currentController.exitEnd();
                controller.enterEnd();
                currentController = controller;
            }break;
            default: {
                if (currentController != null) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(currentController, "moveAnimatorValue", 0, type == AnimationType.PUSH_LEFT ? -1:1);
                    animator.setDuration(Configs.AnimationDuring);
                    currentController.exitStart();
                    final Controller willDisappear = currentController;
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            willDisappear.exitEnd();
                            getFragmentManager().beginTransaction().remove(willDisappear).commit();
                            animating = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                }

                getFragmentManager().beginTransaction().add(R.id.tab_content, controller).commit();
                ObjectAnimator animator = ObjectAnimator.ofFloat(controller, "moveAnimatorValue", type == AnimationType.PUSH_LEFT ? 1:-1, 0);
                animator.setDuration(Configs.AnimationDuring);
                controller.enterStart();
                final Controller willAppear = controller;
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        willAppear.enterEnd();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
                currentController = controller;
            }break;
        }
    }

    public static TabController getTabController(Controller thiz) {
        Fragment parent = thiz.getParent();
        while (parent != null) {
            if (parent instanceof TabController) {
                return (TabController)parent;
            }
            parent = parent.getParentFragment();
        }
        return null;
    }
}
