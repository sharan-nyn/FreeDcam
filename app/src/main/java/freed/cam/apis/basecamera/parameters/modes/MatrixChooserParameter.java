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

package freed.cam.apis.basecamera.parameters.modes;

import java.util.HashMap;

import freed.dng.CustomMatrix;
import freed.utils.Log;

/**
 * Created by troop on 02.05.2016.
 */
public class MatrixChooserParameter extends AbstractModeParameter
{
    public static final String NEXUS6 = "Nexus6";
    public static final String G4 = "G4";
    private final HashMap<String, CustomMatrix> custommatrixes;
    private String currentval = "off";
    private boolean isSupported;

    final String TAG = MatrixChooserParameter.class.getSimpleName();

    public MatrixChooserParameter(HashMap<String, CustomMatrix> matrixHashMap)
    {
        this.custommatrixes = matrixHashMap;
        isSupported = true;
    }

    @Override
    public boolean IsSupported() {
        return isSupported;
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCamera)
    {
        currentval = valueToSet;
        onValueHasChanged(currentval);
    }

    @Override
    public String GetValue() {
        return currentval;
    }

    @Override
    public String[] GetValues()
    {
        return custommatrixes.keySet().toArray(new String[custommatrixes.size()]);
    }

    @Override
    public boolean IsVisible() {
        return isSupported;
    }

    public CustomMatrix GetCustomMatrix(String key)
    {
        Log.d(TAG, "Key: " +key + " Currentvalue: " + currentval);
        if (currentval.equals("off"))
            return custommatrixes.get(key);
        else
            return custommatrixes.get(currentval);
    }

    public CustomMatrix GetCustomMatrixNotOverWritten(String key)
    {
            return custommatrixes.get(key);
    }

 }
