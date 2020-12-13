package cz.adsb.czadsb.model.planes

enum class EngineTypeEnum(val eT: Int) {
    NONE(0),
    PISTON(1),
    TURBO(2),
    JET(3),
    ELECTRIC(4)
}