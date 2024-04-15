package at.hannibal2.skyhanni.config.features.misc;

import at.hannibal2.skyhanni.config.FeatureToggle;
import at.hannibal2.skyhanni.config.core.config.Position;
import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class HoppityEggsConfig {

    @Expose
    @ConfigOption(name = "Hoppity Waypoints", desc = "Toggle guess waypoints for Hoppity's Hunt.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean waypointsEnabled = true;

    @Expose
    @ConfigOption(name = "Show Claimed Eggs", desc = "Show a display that says which eggs have been found in the last SkyBlock day.")
    @ConfigEditorBoolean
    @FeatureToggle
    public boolean showClaimedEggs = false;

    @Expose
    @ConfigOption(name = "Show All Waypoints", desc = "Show all possible egg waypoints for the current lobby.")
    @ConfigEditorBoolean
    public boolean showAllWaypoints = false;

    @Expose
    @ConfigLink(owner = HoppityEggsConfig.class, field = "showClaimedEggs")
    public Position position = new Position(33, 72, false, true);
}
