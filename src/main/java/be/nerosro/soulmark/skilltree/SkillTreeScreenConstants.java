package be.nerosro.soulmark.skilltree;

/** All constants for the Skill Tree UI system. */
public final class SkillTreeScreenConstants {
    private SkillTreeScreenConstants() {}

    /** Layout dimensions and spacing. */
    public static final class Layout {
        private Layout() {}

        public static final int NODE_SIZE = 24;
        public static final int NODE_HALF = NODE_SIZE / 2;
        public static final int LABEL_OFFSET_Y = 6;
        public static final int LABEL_NAMEPLATE_PADDING_X = 5;
        public static final int LABEL_NAMEPLATE_PADDING_Y = 3;
        public static final int LANE_SPACING = 30;
        public static final int DEPTH_SPACING = 100;
        public static final int HEADER_HEIGHT = 30;
        public static final int TAB_HEIGHT = 20;
        public static final int TAB_WIDTH = 80;
        public static final int TAB_GAP = 2;
        public static final int TOOLTIP_PADDING = 6;
        public static final int LINE_THICKNESS = 2;
        public static final int TOOLTIP_LINE_HEIGHT = 10;
    }

    /** Color values for Skill Tree UI elements. */
    public static final class Colors {
        private Colors() {}

        // Node type colors (frame tint)
        public static final int PASSIVE = 0xFF888888;       // gray
        public static final int ABILITY = 0xFF4488FF;       // blue
        public static final int UTILITY = 0xFF44BB44;       // green
        public static final int SPECIALIZATION = 0xFFFFAA00; // gold
        public static final int CAPSTONE = 0xFFFF4444;       // red
        public static final int RECIPE = 0xFFCC8844;         // bronze
        public static final int RITUAL = 0xFFBB44FF;         // purple

        // Visibility colors
        public static final int UNLOCKED_BG = 0xFF2A2A3A;
        public static final int SCRAMBLED_BG = 0xFF1A1A2A;
        public static final int TEASED_BG = 0xFF101018;
        public static final int EXCLUDED_BG = 0xFF3A1010;
        public static final int LINE_UNLOCKED = 0xFFAAFFAA;
        public static final int LINE_LOCKED = 0xFF444466;
        public static final int TEXT_READABLE = 0xFFFFFFFF;
        public static final int TEXT_UNLOCKED = 0xFFCCCC88;
        public static final int TEXT_UNLOCKABLE = 0xFFFFFFFF;
        public static final int TEXT_SCRAMBLED = 0xFF888888;
        public static final int LABEL_NAMEPLATE_FILL_TOP = 0xB81C1830;
        public static final int LABEL_NAMEPLATE_FILL_BOTTOM = 0xB8101020;
        public static final int LABEL_NAMEPLATE_BORDER_TOP = 0xFFB8B4C0;
        public static final int LABEL_NAMEPLATE_BORDER_BOTTOM = 0xFF6F6B78;
        public static final int POINTS_LABEL = 0xFFAAFFAA;
        public static final int SOUL_POINT_LABEL = 0xFFFF5555;
        public static final int JOB_POINT_LABEL = 0xFFFFFF55;

        // Tab colors
        public static final int TAB_SELECTED_BG = 0xFF3344AA;
        public static final int TAB_HOVERED_BG = 0xFF2A2A4A;
        public static final int TAB_NORMAL_BG = 0xFF1A1A2A;
        public static final int TAB_SELECTED_BORDER = 0xFF5566CC;
        public static final int TAB_NORMAL_BORDER = 0xFF333344;

        // Tooltip colors
        public static final int TOOLTIP_BG = 0xF0100010;
        public static final int TOOLTIP_BORDER_TOP = 0xFF5000FF;
        public static final int TOOLTIP_BORDER_BOTTOM = 0xFF28007F;
    }
}
