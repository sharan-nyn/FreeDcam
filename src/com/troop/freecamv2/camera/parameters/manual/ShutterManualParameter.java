package com.troop.freecamv2.camera.parameters.manual;

import android.hardware.Camera;
import android.util.Log;

import com.troop.freecamv2.camera.BaseCameraHolder;
import com.troop.freecamv2.utils.DeviceUtils;

/**
 * Created by troop on 17.08.2014.
 */
public class ShutterManualParameter extends BaseManualParameter
{
    public static String HTCShutterValues = "4.0,3.2,2.5,1.6,1.3,1.0,0.8,0.6,0.5,0.4,0.3,0.25,0.2,0.125,0.1,0.07,0.06,0.05,0.04,0.03,0.025,0.02,0.015,0.0125,0.01,1/125"+
            ",1/200,1/250,1/300,1/400,1/500,1/640,1/800,1/1000,1/1250,1/1600,1/2000,1/2500,1/3200,1/4000,1/5000,1/6400,1/8000";
    
    public static String Z5SShutterValues = "31.0,30.0,29.0,28.0,27.0,26.0,25.0,24.0,23.0,22.0,21.0,"+
    										"20.0,19.0,18.0,17.0,16.0,15.0,14.0,13.0,12.0,11.0,10.0" +
    										",9.0,8.0,7.0,6.0,5.0,4.0,3.0,2.0,1.6,1.3,1.0,0.8,0.6," +
    										"0.5,0.4,0.3,0.25,0.2,0.125,0.1,0.07,0.06,0.05,0.04," +
    										"0.03,0.025,0.02,0.015,0.0125,0.01,1/125,1/200,1/250," +
    										"1/300,1/400,1/500,1/640,1/800,1/1000,1/1250,1/1600" +
    										",1/2000,1/2500,1/3200,1/4000,1/5000,1/6400,1/8000," +
    										"1/10000,1/12000,1/20000,1/30000,1/45000,1/90000";

    String shutterValues[];
    int current = 0;
    BaseCameraHolder baseCameraHolder;

    /*public ShutterManualParameter(Camera.Parameters parameters, String value, String maxValue, String MinValue) {
        super(parameters, value, maxValue, MinValue);


        if (DeviceUtils.isHTCADV())
        {
            this.isSupported = true;
            shutterValues = HTCShutterValues.split(",");
        }
        if (DeviceUtils.isZTEADV())
        {
            this.isSupported = true;
            shutterValues = Z5SShutterValues.split(",");
        }
        //TODO add missing logic
    }*/

    public ShutterManualParameter(Camera.Parameters parameters, String value, String maxValue, String MinValue, BaseCameraHolder baseCameraHolder) {
        super(parameters, value, maxValue, MinValue);


        if (DeviceUtils.isHTCADV())
        {
            this.isSupported = true;
            shutterValues = HTCShutterValues.split(",");
        }
        if (DeviceUtils.isZTEADV())
        {
            this.isSupported = true;
            shutterValues = Z5SShutterValues.split(",");
        }
        //TODO add missing logic
    }

    @Override
    public boolean IsSupported() {
        return super.IsSupported();
    }

    @Override
    public int GetMaxValue() {
    	/*if (DeviceUtils.isSonyADV())
        return Integer.parseInt(parameters.get("sony-min-shutter-speed"));*/
        return shutterValues.length-1;
    }

    @Override
    public int GetMinValue() {
    	/*if (DeviceUtils.isSonyADV())
        return Integer.parseInt(parameters.get("sony-max-shutter-speed"));*/
    	
        return 0;
    }

    @Override
    public int GetValue() {
    	
    	
    	
        return current;
    }

    @Override
    public void SetValue(int valueToSet)
    {
    	/*if(DeviceUtils.isSonyADV())
        {
        	parameters.set("sony-ae-mode", "manual");
    		parameters.set("sony-shutter-speed", valueToSet);
    		
        }*/
    	
    	//Do Not Merge HTC Was Disabled
        
        
        	Log.e("", "Shutter Entered");
            current = valueToSet;
            String shutterstring = shutterValues[current];
            if (shutterstring.contains("/"))
            {
                String split[] = shutterstring.split("/");
                float a = Float.parseFloat(split[0])/ Float.parseFloat(split[1]);
                shutterstring = ""+a;
            }
            Log.e("AE", "ae-af");

           // parameters.set("night_exposure_mode", 0);
        if (DeviceUtils.isZTEADV())
            parameters.set("slow_shutter", shutterstring);
        if (DeviceUtils.isHTCADV())
            parameters.set("shutter", shutterstring);
        Log.e("exposure", shutterstring);

       
    }
/* HTC M8 Value -1 = off
 * 
 *  May have to use this key "non-zsl-manual-mode" set to true for raw with manual controls
 * 
 * 
 * Sony values Untested
 * 
 */
    
    

    public String GetStringValue()
    {
        return shutterValues[current];
    }

    @Override
    public void RestartPreview()
    {
        baseCameraHolder.StopPreview();
        if (DeviceUtils.isHTCADV()||DeviceUtils.isZTEADV()) {
            parameters.set("zsl", "off");
            parameters.set("auto-exposure", "center-weighted");
        }
        if (DeviceUtils.isZTEADV())
            parameters.set("slow_shutter_addition", 0);
        baseCameraHolder.SetCameraParameters(parameters);
        baseCameraHolder.StartPreview();
    }
}
