package at.hannibal2.skyhanni.config.features.gui.customscoreboard;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BackgroundConfig {
    @Expose
    @ConfigOption(
        name = "Enabled",
        desc = "Show a background behind the scoreboard."
    )
    @ConfigEditorBoolean
    public boolean enabled = true;

    @Expose
    @ConfigOption(
        name = "Background Color",
        desc = "The color of the background."
    )
    @ConfigEditorColour
    public String color = "0:80:0:0:0";

    @Expose
    @ConfigOption(
        name = "Background Border Size",
        desc = "The size of the border around the background."
    )
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 20,
        minStep = 1
    )
    public int borderSize = 5;

    @Expose
    @ConfigOption(
        name = "Rounded Corner Smoothness",
        desc = "The smoothness of the rounded corners."
    )
    @ConfigEditorSlider(
        minValue = 0,
        maxValue = 30,
        minStep = 1
    )
    public int roundedCornerSmoothness = 10;

    @Expose
    @ConfigOption(
            name = "Outline",
            desc = "Shows an outline around the scoreboard"
    )
    @ConfigEditorBoolean
    public boolean outline = false;

    @Expose
    @ConfigOption(
            name = "Outline Thickness",
            desc = "Thickness of the outline"
    )
    @ConfigEditorSlider(
            minValue = 1,
            maxValue = 15,
            minStep = 1
    )
    public int outlineThickness = 5;

    @Expose
    @ConfigOption(
            name = "Outline Blur",
            desc = "Amount that the outline is blurred"
    )
    @ConfigEditorSlider(
            minValue = 0.0f,
            maxValue = 1.0f,
            minStep = 0.1f
    )
    public float outlineBlur = 0.7f;

    @Expose
    @ConfigOption(
            name = "Outline Color Top",
            desc = "Color of the top of the outline"
    )
    @ConfigEditorColour
    public String outlineColorTop = "0:255:175:89:255";

    @Expose
    @ConfigOption(
            name = "Outline Color Bottom",
            desc = "Color of the bottom of the outline"
    )
    @ConfigEditorColour
    public String outlineColorBottom = "0:255:127:237:255";

    @Expose
    @ConfigOption(
        name = "Use Custom Background Image",
        desc = "Put that image into a resource pack, using the path \"skyhanni/scoreboard.png\"."
    )
    @ConfigEditorBoolean
    public boolean useCustomBackgroundImage = false;

    @Expose
    @ConfigOption(
        name = "Custom Background",
        desc = "Add an image named \"scoreboard.png\" to your texture pack at \"\\assets\\skyhanni\\scoreboard.png.\" Activate the texture pack in Minecraft, then reload the game."
    )
    @ConfigEditorInfoText
    public String useless;
}
