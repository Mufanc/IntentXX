package mufanc.tools.intentxx

import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose

class IntentWrapper(src: Intent) {

    private val intent = src.clone() as Intent

    @Expose val componentName = intent.component?.flattenToShortString()
    @Expose val action = intent.action
    @Expose val categories = intent.categories?.toTypedArray()
    @Expose val uri = intent.data?.toString()
    @Expose val extras: JsonObject? = intent.extras?.let { bundle2json(it) }
    @Expose val flags = intent.flags

    private fun bundle2json(bundle: Bundle): JsonObject {
        return JsonObject().also { json ->
            for (key in bundle.keySet()) {
                bundle.get(key)?.let { value ->
                    when (value) {
                        is Bundle -> json.add(key, bundle2json(value))
                        is Number, is String, is Boolean -> json.add(key, Gson().toJsonTree(value))
                        else -> json.addProperty(key, "{ parse failed: ${value::class.java.name} }")
                    }
                }
            }
        }
    }

    override fun toString(): String {
        return GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .create().toJson(this)
    }
}