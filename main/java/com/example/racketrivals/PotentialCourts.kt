import kotlinx.serialization.Serializable

//This is where potential courts are created to be inserted in the supabase
@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)
@Serializable
data class PotentialCourts(
    val court_name: String,
    val court_coordinates: Coordinates,
    val admission_cost: Float,
    val lights: Boolean,
    val status:String
)
