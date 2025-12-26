package cat.fell.lif

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import bef.ice.TelMe
import com.re.sid.ual.ben.GetZenbox
import com.re.sid.ual.frist.DataTool
import com.re.sid.ual.frist.HandTool

class DiMo : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        GetZenbox.activityStack.add(activity)
        HandTool.loopStart(activity)
        DataTool.showLog("onActivityCreated: ${activity.javaClass.simpleName}")
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        GetZenbox.activityStack.remove(activity)
    }


}