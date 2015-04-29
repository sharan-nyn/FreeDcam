package com.troop.freedcam.camera2.parameters.manual;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;

import com.troop.freedcam.camera2.BaseCameraHolderApi2;
import com.troop.freedcam.camera2.parameters.ParameterHandlerApi2;
import com.troop.freedcam.i_camera.parameters.AbstractManualParameter;

/**
 * Created by troop on 06.03.2015.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ManualExposureTimeApi2 extends AbstractManualParameter
{
    ParameterHandlerApi2 camParametersHandler;
    BaseCameraHolderApi2 cameraHolder;
    public ManualExposureTimeApi2(ParameterHandlerApi2 camParametersHandler, BaseCameraHolderApi2 cameraHolder) {
        super(camParametersHandler);
        this.cameraHolder = cameraHolder;
        this.camParametersHandler = camParametersHandler;
    }

    int current = 0;

    @Override
    public int GetMaxValue() {
        return (cameraHolder.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE).getUpper()).intValue()/1000;
    }

    @Override
    public int GetMinValue()
    {
        //if (cameraHolder.mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_MODE) == CaptureRequest.CONTROL_AE_MODE_OFF)
        //    return cameraHolder.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE).getLower().intValue();
        return 0;
    }

    @Override
    public int GetValue()
    {

            return cameraHolder.mPreviewRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME).intValue();
    }

    @Override
    public String GetStringValue()
    {
        if (current < (cameraHolder.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE).getLower().intValue()))
            return "auto";
        else {
            long mili = cameraHolder.mPreviewRequestBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME);
            return getSECONDS(mili) + "";
        }
    }

    public String getSECONDS (long time)
    {
        double mili = time /1000000  ;
        double sec =  mili / 1000;
        return sec +"";
    }

    @Override
    public String[] getStringValues() {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void SetValue(int valueToSet)
    {
        current = valueToSet;
        if (valueToSet < (cameraHolder.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE).getLower().intValue()))
        {
            cameraHolder.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            cameraHolder.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        else
        {
            if (cameraHolder.mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AE_MODE) != CaptureRequest.CONTROL_AE_MODE_OFF) {
                cameraHolder.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                cameraHolder.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            cameraHolder.mPreviewRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (long)valueToSet * 1000);
        }
        try {
            cameraHolder.mCaptureSession.setRepeatingRequest(cameraHolder.mPreviewRequestBuilder.build(), cameraHolder.mCaptureCallback,
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean IsSupported() {
        return cameraHolder.characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE) != null;
    }

    @Override
    public boolean IsSetSupported() {
        return true;
    }


}
