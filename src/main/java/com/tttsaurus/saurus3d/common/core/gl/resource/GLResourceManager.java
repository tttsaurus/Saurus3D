package com.tttsaurus.saurus3d.common.core.gl.resource;

import com.tttsaurus.saurus3d.Saurus3D;
import com.tttsaurus.saurus3d.common.core.gl.debug.GLDebug;
import com.tttsaurus.saurus3d.common.core.gl.debug.KHRDebugManager;
import java.util.PriorityQueue;

public final class GLResourceManager
{
    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    public static void addDisposable(GLDisposable disposable)
    {
        disposables.add(disposable);
    }
    public static void removeDisposable(GLDisposable disposable)
    {
        disposables.remove(disposable);
    }

    public static void disposeAll()
    {
        while (!disposables.isEmpty())
        {
            GLDisposable disposable = disposables.poll();
            Saurus3D.LOGGER.info("Disposing " + disposable.getClass().getSimpleName());
            disposable.dispose();
            if (!KHRDebugManager.isEnable()) GLDebug.checkError();
        }
    }
}
