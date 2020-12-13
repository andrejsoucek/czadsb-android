package cz.adsb.czadsb.model.planes

enum class SpeciesEnum(val spec: Int){
    NONE(0),
    LAND_PLANE(1),
    SEA_PLANE(2),
    AMPHIBIAN(3),
    HELICOPTER(4),
    GYROCOPTER(5),
    TILTWING(6),
    GROUND_VEHICLE(7),
    TOWER(8)
}