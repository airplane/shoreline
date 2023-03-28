package com.momentum.api.module;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.momentum.api.config.Config;
import com.momentum.api.config.Configuration;
import com.momentum.api.config.Macro;
import com.momentum.api.config.configs.BooleanConfig;
import com.momentum.api.config.factory.ConfigContainer;
import com.momentum.api.config.file.ConfigFile;
import com.momentum.api.config.file.IConfigurable;
import com.momentum.api.module.exceptions.IncompatibleInterfaceException;
import com.momentum.api.module.property.IConcurrent;
import com.momentum.api.module.property.IHideable;
import com.momentum.api.module.property.IToggleable;
import com.momentum.api.registry.ILabeled;
import com.momentum.api.util.Globals;
import com.momentum.impl.ui.click.ClickGuiScreen;

import java.util.Map.Entry;

/**
 * Configurable client feature that is displayed in the
 * {@link ClickGuiScreen} and the Hud. Modules can be
 * toggled using a {@link Macro}. Modules can be hidden from Hud.
 *
 * @author linus
 * @since 03/20/2023
 *
 * @see com.momentum.api.module.modules.SubscriberModule
 * @see com.momentum.api.module.modules.ConcurrentModule
 * @see com.momentum.api.module.modules.ToggleModule
 */
public class Module extends ConfigContainer
        implements Globals, IConfigurable<JsonObject>, IHideable, ILabeled
{
    // module identifier
    // must be unique for each module
    private final String name;

    // description of module functionality
    private final String desc;

    // module category
    private final ModuleCategory category;

    // hidden state
    // default set to false
    @Configuration("module_hidden")
    final BooleanConfig hidden = new BooleanConfig("Hidden",
            "Hidden state. Global in all modules. This config determines " +
                    "whether the module will appear in the Hud", false);

    /**
     * Default module constructor. Name must be unique for all modules.
     *
     * @param name The module name
     * @param desc The module description
     * @param category The module category
     * @throws IncompatibleInterfaceException if module implements incompatible
     * interfaces
     */
    public Module(String name, String desc, ModuleCategory category)
    {
        // {@see ConfigFactory#build(Class)}
        super();

        // incompatible module interfaces
        if (this instanceof IToggleable
                && this instanceof IConcurrent)
        {
            throw new IncompatibleInterfaceException(
                    "Module cannot implement IToggleable and IConcurrent at " +
                            "the same time");
        }

        // init
        this.name = name;
        this.desc = desc;
        this.category = category;
    }

    /**
     * Gets the module name
     *
     * @return The module name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the module description
     *
     * @return The module description
     */
    public String getDescription()
    {
        return desc;
    }

    /**
     * Gets the {@link ModuleCategory}
     *
     * @return The module category
     */
    public ModuleCategory getCategory()
    {
        return category;
    }

    /**
     * Sets the object's hidden state
     *
     * @param hide The new hide state
     */
    @Override
    public void setHidden(boolean hide)
    {
        // update hidden state val
        hidden.setValue(hide);
    }

    /**
     * Returns whether the object is hidden
     *
     * @return The hidden state
     */
    @Override
    public boolean isHidden()
    {
        // hidden state val
        return hidden.getValue();
    }

    /**
     * Gets the module label
     *
     * @return The module label
     */
    @Override
    public String getLabel()
    {
        // module label
        return name.toLowerCase() + "_module";
    }

    /**
     * Parses the values from a {@link JsonObject} and updates all
     * {@link Config} values in the objects
     *
     * @param o The Json object
     */
    @Override
    public void fromJson(JsonObject o)
    {
        // JsonElement set
        for (Entry<String, JsonElement> entry : o.entrySet())
        {
            // cfg from key
            Config<?> cfg = retrieve(entry.getKey());

            // check retrieved
            if (cfg != null)
            {
                // catches read exceptions
                try
                {
                    // parse Json value
                    cfg.fromJson(entry.getValue());
                }

                // couldn't parse Json value
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Returns configs as a string which will be passed to the
     * {@link ConfigFile} writer and written to a <tt>.json</tt> file
     *
     * @return The configs as a parsable Json string
     */
    @Override
    public JsonObject toJson()
    {
        // json object
        JsonObject out = new JsonObject();

        // write all configurations
        for (Config<?> cfg : getConfigs())
        {
            // toggleable configs
            if (cfg.getLabel().equalsIgnoreCase("module_enabled")
                    || cfg.getLabel().equalsIgnoreCase("module_keybind"))
            {
                // concurrent module
                if (this instanceof IConcurrent)
                {
                    continue;
                }
            }

            // add to output
            out.add(cfg.getLabel(), cfg.toJson());
        }

        // output JsonObject
        return out;
    }
}