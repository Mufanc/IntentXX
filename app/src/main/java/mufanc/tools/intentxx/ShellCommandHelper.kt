package mufanc.tools.intentxx

import android.content.Intent
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import java.io.*
import java.util.*

object ShellCommandHelper {

    private val records = object : LinkedList<Pair<Intent, String?>>() {
        override fun add(element: Pair<Intent, String?>): Boolean {
            return super.add(element).also {
                while (size > 10) {
                    removeFirst()
                }
            }
        }
    }

    private val CmdHooker = object : XC_MethodHook() {

        private var output: PrintWriter? = null

        fun print(intent: Intent, caller: String?) {
            synchronized (this) {
                if (output == null) return
                output?.write("Intent: [$caller]\n${IntentWrapper(intent)}\n")
                output?.flush()
            }
        }

        override fun beforeHookedMethod(param: MethodHookParam) {
            handle {
                val command = param.args[0] as String
                if (command != "ir") return@handle

                param.thisObject.invokeMethodAs<PrintWriter>("getOutPrintWriter").let {
                    synchronized (this) { output = it }
                }

                synchronized (records) {
                    for (pair in records) {
                        print(pair.first, pair.second)
                    }
                }

                Log.i("[CmdHooker] Start recording Intent")

                param.thisObject
                    .invokeMethodAs<InputStream>("getRawInputStream")
                    .let { InputStreamReader(it) }
                    .let { BufferedReader(it) }
                    .let {
                        try {
                            it.readLine()
                        } catch (err: IOException) { }
                        synchronized (this) { output = null }
                    }

                Log.i("[CmdHooker] Stop recording Intent")

                param.result = 0
            }
        }
    }

    private val IntentHooker = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            handle {
                param.thisObject.getObject("mRequest").let { mRequest ->
                    val caller = mRequest.getObjectOrNullAs<String>("callingPackage")
                    (mRequest.getObjectOrNullAs<Intent>("intent") ?: return@handle).let {
                        val intent = it.clone() as Intent
                        synchronized (records) { records.add(Pair(intent, caller)) }
                        CmdHooker.print(intent, caller)
                    }
                }
            }
        }
    }

    fun main() {
        findMethod("com.android.server.am.ActivityManagerShellCommand") {
            name == "onCommand"
        }.hookMethod(CmdHooker)

        findMethod("com.android.server.wm.ActivityStarter") {
            name == "execute"
        }.hookMethod(IntentHooker)
    }
}