/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.cam.apis.basecamera.modules;


import android.os.Handler;

import java.io.File;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract.CaptureStateChanged;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract.CaptureStates;
import freed.utils.AppSettingsManager;
import freed.utils.Log;
import freed.viewer.holder.FileHolder;

/**
 * Created by troop on 15.08.2014.
 */
public abstract class ModuleAbstract implements ModuleInterface
{

    protected boolean isWorking;
    public String name;

    protected CaptureStateChanged captureStateChangedListner;
    private final String TAG = ModuleAbstract.class.getSimpleName();
    protected AppSettingsManager appSettingsManager;
    protected CaptureStates currentWorkState;
    protected CameraWrapperInterface cameraUiWrapper;
    protected Handler mBackgroundHandler;
    protected Handler mainHandler;


    public ModuleAbstract(CameraWrapperInterface cameraUiWrapper, Handler mBackgroundHandler, Handler mainHandler)
    {
        this.cameraUiWrapper = cameraUiWrapper;
        this.appSettingsManager = cameraUiWrapper.getAppSettingsManager();
        this.mBackgroundHandler = mBackgroundHandler;
        this.mainHandler = mainHandler;
    }

    public void SetCaptureStateChangedListner(CaptureStateChanged captureStateChangedListner)
    {
        this.captureStateChangedListner = captureStateChangedListner;
    }

    /**
     * throw this when camera starts working to notify ui
     */
    protected void changeCaptureState(final CaptureStates captureStates)
    {
        Log.d(TAG, "work started");
        currentWorkState = captureStates;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (captureStateChangedListner != null)
                    captureStateChangedListner.onCaptureStateChanged(captureStates);
            }
        });
    }

    @Override
    public String ModuleName() {
        return name;
    }


    @Override
    public void DoWork() {
    }

    @Override
    public boolean IsWorking() {
        return isWorking;
    }

    /**
     * this gets called when the module gets loaded. set here specific parameters that are needed by the module
     */
    @Override
    public void InitModule()
    {
        isWorking = false;
    }

    /**
     * this gets called when module gets unloaded reset the parameters that where set on InitModule
     */
    @Override
    public  void DestroyModule()
    {

    }

    @Override
    public abstract String LongName();

    @Override
    public abstract String ShortName();

    @Override
    public void fireOnWorkFinish(File file) {
        cameraUiWrapper.getActivityInterface().WorkHasFinished(new FileHolder(file, appSettingsManager.GetWriteExternal()));
    }

    @Override
    public void fireOnWorkFinish(File files[])
    {
        FileHolder[] fileHolders = new FileHolder[files.length];
        int i= 0;
        for (File f : files) {
            fileHolders[i++] = new FileHolder(f, appSettingsManager.GetWriteExternal());
        }
        cameraUiWrapper.getActivityInterface().WorkHasFinished(fileHolders);
    }
}
