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

package com.freedcam.apis.camera1.parameters.modes;

import android.hardware.Camera.Parameters;

import com.freedcam.apis.basecamera.interfaces.CameraWrapperInterface;
import com.freedcam.apis.camera1.parameters.ParametersHandler;
import com.freedcam.utils.DeviceUtils.Devices;
import com.troop.freedcam.R;

/**
 * Created by GeorgeKiarie on 9/24/2015.
 */
public class VirtualLensFilter extends  BaseModeParameter {

    private final int[] asT = {0, 1, 2, 3, 4, 5, 6};
    private final String[] asU;
    public VirtualLensFilter(Parameters parameters, CameraWrapperInterface cameraUiWrapper)
    {
        super(parameters, cameraUiWrapper, "", "");

        if (cameraUiWrapper.GetAppSettingsManager().getDevice() == Devices.ZTE_ADV)
            isSupported = true;
        this.cameraUiWrapper = cameraUiWrapper;
        valuesArray = cameraUiWrapper.getContext().getResources().getStringArray(R.array.virtual_lensfilter_colors);
        asU = cameraUiWrapper.getContext().getResources().getStringArray(R.array.virtual_lensfilter_asu);
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCam)
    {
        switch (valueToSet)
        {
            case "Off":
                parameters.set("color-filter-type", ""+ asT[0]);
                break;
            case "Red":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[1]);
                break;

            case "Orange":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[2]);
                break;
            case "Yellow":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[3]);
                break;
            case "Green":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[4]);
                break;
            case "Cyan":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[5]);
                break;
            case "Blue":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[6]);
                break;
            case "Purple":
                parameters.set("color-filter-type", ""+ asT[1]);
                parameters.set("color-filter-param", asU[7]);
                break;
            case "Grad Left":
                parameters.set("color-filter-type", ""+ asT[4]);
                parameters.set("color-filter-param", asU[8]);
                break;
            case "Grad Right":
                parameters.set("color-filter-type", ""+ asT[3]);
                parameters.set("color-filter-param", asU[9]);
                break;
            case "Grad Top":
                parameters.set("color-filter-type", ""+ asT[5]);
                parameters.set("color-filter-param", asU[10]);
                break;
            case "Grad Bottom":
                parameters.set("color-filter-type", ""+ asT[6]);
                parameters.set("color-filter-param", asU[11]);
                break;


        }
        ((ParametersHandler) cameraUiWrapper.GetParameterHandler()).SetParametersToCamera(parameters);
    }

    @Override
    public String GetValue()
    {
        return "Off";
    }
}