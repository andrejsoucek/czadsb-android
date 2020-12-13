package cz.adsb.czadsb.model.planes

enum class WakeTurbulenceEnum(val turb: Int) {
    None(0),
    Light(1),
    Medium(2),
    Heavy(3)
}