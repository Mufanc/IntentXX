package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

public interface IActivityManager extends IInterface {

    void registerProcessObserver(IProcessObserver iProcessObserver) throws RemoteException;

    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum);

    abstract class Stub extends Binder implements IActivityManager {

        public static IActivityManager asInterface(IBinder iBinder) {
            throw new RuntimeException("stub!");
        }
    }
}
