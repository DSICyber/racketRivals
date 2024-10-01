import kotlinx.serialization.Serializable


//for loading up personalization
@Serializable
data class User(
    val elo: Int,
    val full_name: String,
    val avatar_url: String
    // Include other fields as necessary
)