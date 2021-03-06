package apps.gen.lib.utils;

import android.content.Context;
import android.util.Log;

import apps.gen.lib.controllers.Controller;
import apps.gen.lib.controllers.NavigationController;
import apps.gen.lib.controllers.TabController;

/**
 * Created by gen on 16/4/23.
 * Helper of Gen libraries.
 */
public final class H {
    public static boolean debug = false;

    public static NavigationController navC(Controller thiz) {
        return NavigationController.getNavigationController(thiz);
    }
    public static TabController tabC(Controller thiz) {
        return TabController.getTabController(thiz);
    }
    public static int dip2px(Context context, float dipValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public static void i(String msg, Object ...args) {info(msg, args);}
    public static void info(String format, Object ...args) {
        if (!debug) return;
        if (args.length > 0) {
            Log.i(Configs.LogTag, String.format(format, args));
        }else {
            Log.i(Configs.LogTag, format);
        }
    }

    public static void e(String err, Object ...args) {error(err, args);}
    public static void error(String err, Object ...args) {
        if (!debug) return;
        if (args.length > 0) {
            Log.e(Configs.LogTag, String.format(err, args));
        }else {
            Log.e(Configs.LogTag, err);
        }
    }

    public interface BaseInterface<T> {
        void run(T p);
    }
    public interface BaseInterface2<T, E> {
        void run(T p, E e);
    }

    public static NotificationCenter NC() {
        return NotificationCenter.instance();
    }

}
