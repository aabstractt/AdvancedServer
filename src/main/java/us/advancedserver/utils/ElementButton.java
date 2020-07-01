package us.advancedserver.utils;

import cn.nukkit.form.element.ElementButtonImageData;

public class ElementButton extends cn.nukkit.form.element.ElementButton {

    private final String definition;

    public ElementButton(String text, String definition) {
        this(text, definition, null);
    }

    public ElementButton(String text, String definition, ElementButtonImageData image) {
        super(text, image);

        this.definition = definition;
    }

    public String getDefinition() {
        return this.definition;
    }
}
