package nagioshistory.helper;

import java.awt.Color;

public class Gradient
{
    public final static Color getColor(double val)
    {
        double H = ((100-val)/360); // Hue (note 0.4 = Green)
        double S = 1.0; // Saturation
        double B = 1.0; // Brightness

        return Color.getHSBColor((float)H, (float)S, (float)B);
    }
}
