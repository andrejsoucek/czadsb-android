package cz.adsb.czadsb.model

enum class EnginePlacementEnum(val placement: Int) {
    UNKNOWN(0),
    AFT_MOUNTED(1),
    WING_BURIED(2),
    FUSELAGE_BURIED(3),
    NOSE_MOUNTED(4),
    WING_MOUNTED(5)
}
