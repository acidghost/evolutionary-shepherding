package nl.vu.ai.aso.shared;

import com.google.common.primitives.Doubles;

import java.util.List;

/**
 * Created by Selene on 2015-11-24.
 */
public class Utils {

    public static double[] toPrimitiveDouble(List<Double> list) {
        return Doubles.toArray(list);
    }

}
