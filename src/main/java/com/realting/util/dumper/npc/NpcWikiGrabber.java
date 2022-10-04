package com.realting.util.dumper.npc;


import java.util.Map;

import com.realting.util.dumper.HtmlGrab;
import com.realting.util.dumper.HtmlGrabber;
import com.realting.model.definitions.NpcDefinition;

public class NpcWikiGrabber {

    public static void main(String...args) {
        try {
            NpcDefinition.parseNpcs().load();
            HtmlGrabber grabber = new HtmlGrabber(true);
            for (Map.Entry<Integer, NpcDefinition> def : NpcDefinition.getEntries()) {
                if (def.getValue().getName() != null) {
                    grabber.submit(new HtmlGrab(def.getValue().getName(), "npcs", null));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
