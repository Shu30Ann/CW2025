package com.comp2042;

import com.comp2042.logic.bricks.Brick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Super Rotation System wall-kick data for guideline pieces.
 * The offsets are defined using the standard orientation order (0 = spawn, 1 = right, 2 = 180, 3 = left).
 */
public final class SrsKickTable {

    private static final Map<String, List<PointInt>> JLSTZ_KICKS = new HashMap<>();
    private static final Map<String, List<PointInt>> I_KICKS = new HashMap<>();

    static {
        // JLSTZ kicks
        JLSTZ_KICKS.put("0>1", offsets(0, 0, -1, 0, -1, 1, 0, -2, -1, -2));
        JLSTZ_KICKS.put("1>0", offsets(0, 0, 1, 0, 1, -1, 0, 2, 1, 2));
        JLSTZ_KICKS.put("1>2", offsets(0, 0, 1, 0, 1, -1, 0, 2, 1, 2));
        JLSTZ_KICKS.put("2>1", offsets(0, 0, -1, 0, -1, 1, 0, -2, -1, -2));
        JLSTZ_KICKS.put("2>3", offsets(0, 0, 1, 0, 1, 1, 0, -2, 1, -2));
        JLSTZ_KICKS.put("3>2", offsets(0, 0, -1, 0, -1, -1, 0, 2, -1, 2));
        JLSTZ_KICKS.put("3>0", offsets(0, 0, -1, 0, -1, -1, 0, 2, -1, 2));
        JLSTZ_KICKS.put("0>3", offsets(0, 0, 1, 0, 1, 1, 0, -2, 1, -2));

        // I piece kicks
        I_KICKS.put("0>1", offsets(0, 0, -2, 0, 1, 0, -2, -1, 1, 2));
        I_KICKS.put("1>0", offsets(0, 0, 2, 0, -1, 0, 2, 1, -1, -2));
        I_KICKS.put("1>2", offsets(0, 0, -1, 0, 2, 0, -1, 2, 2, -1));
        I_KICKS.put("2>1", offsets(0, 0, 1, 0, -2, 0, 1, -2, -2, 1));
        I_KICKS.put("2>3", offsets(0, 0, 2, 0, -1, 0, 2, 1, -1, -2));
        I_KICKS.put("3>2", offsets(0, 0, -2, 0, 1, 0, -2, -1, 1, 2));
        I_KICKS.put("3>0", offsets(0, 0, 1, 0, -2, 0, 1, -2, -2, 1));
        I_KICKS.put("0>3", offsets(0, 0, -1, 0, 2, 0, -1, 2, 2, -1));
    }

    private SrsKickTable() {
    }

    /**
     * Returns a copy of the kick offsets to try for a given rotation transition.
     * Offsets are expressed in guideline coordinates (positive Y is up) and must be flipped for matrix rows.
     */
    public static List<PointInt> getKickData(Brick brick, int fromOrientation, int toOrientation) {
        String key = normalize(fromOrientation) + ">" + normalize(toOrientation);
        boolean isIPiece = brick.getClass().getSimpleName().startsWith("IBrick");
        boolean isOPiece = brick.getClass().getSimpleName().startsWith("OBrick");

        if (isOPiece) {
            return List.of(new PointInt(0, 0));
        }

        List<PointInt> raw = isIPiece ? I_KICKS.getOrDefault(key, defaultKick()) : JLSTZ_KICKS.getOrDefault(key, defaultKick());
        List<PointInt> copy = new ArrayList<>(raw.size());
        for (PointInt p : raw) {
            copy.add(new PointInt(p));
        }
        return copy;
    }

    private static int normalize(int value) {
        int mod = value % 4;
        return mod < 0 ? mod + 4 : mod;
    }

    private static List<PointInt> offsets(int... coords) {
        List<PointInt> list = new ArrayList<>();
        for (int i = 0; i + 1 < coords.length; i += 2) {
            list.add(new PointInt(coords[i], coords[i + 1]));
        }
        return list;
    }

    private static List<PointInt> defaultKick() {
        return List.of(new PointInt(0, 0));
    }
}
