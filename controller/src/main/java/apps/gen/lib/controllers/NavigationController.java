package apps.gen.lib.controllers;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fichardu.interpolator.EaseInCubicInterpolator;
import com.fichardu.interpolator.EaseOutCubicInterpolator;

import java.util.Stack;

import apps.gen.lib.R;
import apps.gen.lib.utils.Configs;
import apps.gen.lib.utils.H;
import apps.gen.lib.views.NavigationBar;

/**
 * Created by Gen on 2016/4/13.
 */
public class NavigationController extends Controller {
    static class NavigationControllerView extends android.support.design.widget.CoordinatorLayout {
        NavigationBar mNavigationBar;
        NavigationBar getNavigationBar() {
            return mNavigationBar;
        }

        RelativeLayout mContentView;
        RelativeLayout getContentView() {
            return mContentView;
        }

        boolean isInitView = false;
        public NavigationControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            isInitView = true;
            initView(context);
            isInitView = false;
        }
        public NavigationControllerView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }
        public  NavigationControllerView(Context context) {
            this(context, null);
        }

        final float DefaultHeight = 52;
        void initView(Context context) {
            mContentView = new RelativeLayout(context);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mContentView.setLayoutParams(params);
            mContentView.setPadding(0, H.dip2px(context, DefaultHeight), 0, 0);
            addView(mContentView);

            RelativeLayout view = new RelativeLayout(context);
            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            view.setLayoutParams(params);
            view.setId(R.id.navigation_content);
            mContentView.addView(view);

            mNavigationBar = new NavigationBar(context);
            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, H.dip2px(context, DefaultHeight));
            mNavigationBar.setLayoutParams(params);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mNavigationBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            }else {
                mNavigationBar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            addView(mNavigationBar);
        }

        float mPushAnimatorValue;
        public float getPushAnimatorValue() {
            return mPushAnimatorValue;
        }
        public void getPushAnimatorValue(float pushAnimatorValue) {
            if (mPushAnimatorValue != pushAnimatorValue) {
                mPushAnimatorValue = pushAnimatorValue;
            }
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

    NavigationControllerView navView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        navView = new NavigationControllerView(getContext());
        navView.getNavigationBar().setLeftListener(new NavigationBar.OnClickListener() {
            @Override
            public void onClick(View v, int index) {
                ButtonItem item = currentLeftItem.getItems()[index];
                if (item.getOnClickListener() != null)
                    item.getOnClickListener().onClick(v);
            }
        });
        navView.getNavigationBar().setRightListener(new NavigationBar.OnClickListener() {
            @Override
            public void onClick(View v, int index) {
                ButtonItem item = currentRightItem.getItems()[index];
                if (item.getOnClickListener() != null)
                    item.getOnClickListener().onClick(v);
            }
        });
        return navView;
    }

    boolean animating = false;
    Stack<Controller> controllersStack = new Stack<Controller>();
    public void push(Controller controller) {push(controller, false);}
    public void push(Controller controller, boolean animated) {
        if (animating)
            return;
        if (animated)
            animating = true;
        Controller base = null;
        if (controllersStack.size() > 0)
            base = controllersStack.lastElement();
        controllersStack.push(controller);
        controller.setParent(this);
        if (animated) {
            if (base != null) {
                base.onViewWillDisappear();
                ObjectAnimator animator1 = ObjectAnimator.ofFloat(base, "pushAnimatorValue", 0, -1);
                animator1.setInterpolator(new EaseInCubicInterpolator());
                animator1.setDuration(Configs.AnimationDuring);
                base.exitStart();
                final Controller willDisappear = base;
                animator1.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        willDisappear.exitEnd();
                        getFragmentManager().beginTransaction().remove(willDisappear).commit();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                animator1.start();
            }

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.navigation_content, controller).commit();
            ObjectAnimator animator = ObjectAnimator.ofFloat(controller, "pushAnimatorValue", 1, 0);
            animator.setInterpolator(new EaseOutCubicInterpolator());
            animator.setDuration(Configs.AnimationDuring);
            controller.enterStart();
            final Controller willAppear = controller;
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    getView().getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animating = false;
                            willAppear.enterEnd();
                        }
                    }, 0);
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
            animator.start();
        }else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (base != null) base.exitStart();
            controller.enterStart();
            fragmentTransaction.replace(R.id.navigation_content, controller).commit();
            if (base != null) base.exitEnd();
            controller.enterEnd();
        }
        pushNavAnim(controller, animated);
    }

    public Controller pop() { return pop(false); }

    public Controller pop(boolean animated) {
        if (animating) return null;
        if (controllersStack.size() <= 1) {
            Log.w(Configs.LogTag, "Can not pop controller because the controllers stack is empty.");
        }
        if (animated) animating = true;

        Controller top = controllersStack.pop();
        Controller last = controllersStack.lastElement();
        if (animated) {
            top.onViewWillDisappear();
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(top, "pushAnimatorValue", 0, 1);
            animator1.setDuration(Configs.AnimationDuring);
            animator1.setInterpolator(new EaseInCubicInterpolator());
            top.exitStart();
            top.setParent(null);
            final Controller willDisappear = top;
            animator1.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) { }

                @Override
                public void onAnimationEnd(Animator animation) {
                    willDisappear.exitEnd();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.remove(willDisappear);
                    fragmentTransaction.commit();
                }

                @Override
                public void onAnimationCancel(Animator animation) { }

                @Override
                public void onAnimationRepeat(Animator animation) { }
            });
            animator1.start();

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.navigation_content, last).commit();
            ObjectAnimator animator = ObjectAnimator.ofFloat(last, "pushAnimatorValue", -1, 0);
            animator.setDuration(Configs.AnimationDuring);
            animator.setInterpolator(new EaseOutCubicInterpolator());
            last.enterStart();
            final Controller willAppear = last;
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    getView().getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animating = false;
                            willAppear.enterEnd();
                        }
                    }, 0);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        }else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Controller controller = controllersStack.lastElement();
            fragmentTransaction.replace(R.id.navigation_content, controller).commit();
        }
        popNavAnim(last, animated);
        return top;
    }

    NavigationItem defaultBackItem;
    NavigationItem getDefaultBackItem() {
        if (defaultBackItem == null) {
            defaultBackItem = new NavigationItem(getResources().getDrawable(R.drawable.ic_action_back), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pop(true);
                }
            });
        }
        return defaultBackItem;
    }
    NavigationItem currentLeftItem = null;
    NavigationItem currentRightItem = null;

    public void setLeftNavItem(NavigationItem leftItem, NavigationBar.AnimationType animationType) {
        currentLeftItem = leftItem;
        if (currentLeftItem == null && controllersStack.size() > 1) {
            currentLeftItem = getDefaultBackItem();
        }
        Drawable icon = null;
        if (currentLeftItem != null && currentLeftItem.getItems().length > 0) {
            icon = currentLeftItem.getItems()[0].getIcon();
        }
        if (navView != null)
            navView.getNavigationBar().setLeftIcon(icon, animationType);
    }
    public void setRightNavItem(NavigationItem rightItem, NavigationBar.AnimationType animationType) {
        currentRightItem = rightItem;
        Drawable icon = null;
        if (currentRightItem != null && currentRightItem.getItems().length > 0) {
            icon = currentRightItem.getItems()[0].getIcon();
        }
        if (navView != null)
            navView.getNavigationBar().setRightIcon(icon, animationType);
    }

    public void pushNavAnim(Controller controller, boolean animated) {
        if (navView != null) {
            final Controller tController = controller;
            final NavigationBar.AnimationType tAnimation = animated? NavigationBar.AnimationType.PUSH:NavigationBar.AnimationType.NONE;
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setLeftNavItem(tController.getLeftItems(), tAnimation);
                    setRightNavItem(tController.getRightItems(), tAnimation);
                    navView.getNavigationBar().setTitle(tController.getTitle(), tAnimation);
                }
            }, 0);
        }
    }

    public void popNavAnim(Controller controller, boolean animated) {
        if (navView != null) {
            final NavigationBar.AnimationType tAnimation = animated? NavigationBar.AnimationType.POP: NavigationBar.AnimationType.NONE;
            setLeftNavItem(controller.getLeftItems(), tAnimation);
            setRightNavItem(controller.getRightItems(), tAnimation);
            navView.getNavigationBar().setTitle(controller.getTitle(), tAnimation);
        }
    }

    public static NavigationController getNavigationController(Controller thiz) {
        Fragment parent = thiz.getParent();
        while (parent != null) {
            if (parent instanceof NavigationController) {
                return (NavigationController)parent;
            }
            parent = parent.getParentFragment();
        }
        return null;
    }
}
