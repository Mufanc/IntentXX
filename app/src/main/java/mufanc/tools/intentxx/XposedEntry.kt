package mufanc.tools.intentxx

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage


fun handle(block: () -> Unit) {
    try {
        block()
    } catch (err: Throwable) {
        Log.e("Error at ${block::class.java}: ", err)
    }
}

class XposedEntry : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "android") return

        EzXHelperInit.initHandleLoadPackage(lpparam)
        EzXHelperInit.setLogTag("IntentXX")

        handle {
            ShellCommandHelper.main()
        }
    }
}