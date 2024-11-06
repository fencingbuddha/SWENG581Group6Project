package pl.art.lach.mateusz.javaopenchess.display.windows.components;

import javax.swing.JSlider;

public class ComputerLevelSlider extends JSlider
{
    private static final long serialVersionUID = 1L;

    public ComputerLevelSlider()
    {
        super();
        setEnabled(true);
        setValue(1);
        setMaximum(2);
        setMinimum(1);
        setPaintTicks(true);
        setPaintLabels(true);
        setMajorTickSpacing(1);
        setMinorTickSpacing(1);
    }

}
