rootProject.name = "hotel-reservation-platform"

include(
    "services:auth",

    "modules:web",
    "modules:id-generator",
    "modules:jwt"
)
