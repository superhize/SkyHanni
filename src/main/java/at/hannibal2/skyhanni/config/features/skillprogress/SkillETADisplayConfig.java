package at.hannibal2.skyhanni.config.features.skillprogress;

import at.hannibal2.skyhanni.config.FeatureToggle;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.moulberry.moulconfig.annotations.ConfigEditorSlider;
import io.github.moulberry.moulconfig.annotations.ConfigOption;
import io.github.moulberry.moulconfig.observer.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillETADisplayConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Show a display of your current active skill\n" +
        "with the XP/hour rate, ETA to the next level and current session time.")
    @ConfigEditorBoolean
    @FeatureToggle
    public Property<Boolean> enabled = Property.of(false);

    @Expose
    @ConfigOption(name = "Text", desc = "Drag text to change the appearance of the overlay.")
    @ConfigEditorDraggableList
    public List<TextEntry> textEntry = new ArrayList<>(Arrays.asList(
        TextEntry.SKILL,
        TextEntry.TIME,
        TextEntry.XP_HOUR,
        TextEntry.SESSION
    ));

    public enum TextEntry {
        SKILL("§6Skill: §aFarming"),
        XP_NEEDED("§7Needed XP: §e123,456,789"),
        TIME("§7In §b1d 4h 20m 69s"),
        XP_HOUR("§7XP/h: §e 123,456"),
        SESSION("§7Session: §e1h 22m 4s");

        private final String str;

        TextEntry(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }


    @Expose
    @ConfigOption(name = "Farming", desc = "After how much seconds the Farming session timer should pause.")
    @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 60)
    public int farmingPauseTime = 3;

    @Expose
    @ConfigOption(name = "Mining", desc = "After how much seconds the Mining session timer should pause.")
    @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 60)
    public int miningPauseTime = 3;

    @Expose
    @ConfigOption(name = "Combat", desc = "After how much seconds the Combat session timer should pause.")
    @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 60)
    public int combatPauseTime = 30;

    @Expose
    @ConfigOption(name = "Foraging", desc = "After how much seconds the Foraging session timer should pause.")
    @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 60)
    public int foragingPauseTime = 3;

    @Expose
    @ConfigOption(name = "Fishing", desc = "After how much seconds the Fishing session timer should pause.")
    @ConfigEditorSlider(minStep = 1, minValue = 3, maxValue = 60)
    public int fishingPauseTime = 15;
}
