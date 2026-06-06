package com.gramaurja.data.model

/**
 * Represents a transformer zone / village power zone.
 * Zones are pre-seeded for Karnataka demo districts.
 */
data class Zone(
    val id: String = "",
    val name: String = "",
    val district: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val transformerNumber: String = ""
)

/** Hardcoded demo zones for Karnataka villages */
object SampleZones {
    val zones = listOf(
        Zone("hosahalli_t1", "Hosahalli – Transformer 1", "Tumkur", 13.3379, 77.1173, "T-001"),
        Zone("sira_t2", "Sira – Main Feeder", "Tumkur", 13.7427, 76.9053, "T-002"),
        Zone("pavagada_t1", "Pavagada Solar Zone", "Tumkur", 14.1015, 77.2825, "T-003"),
        Zone("madhugiri_t1", "Madhugiri – Agri Feeder", "Tumkur", 13.6644, 77.2134, "T-004"),
        Zone("tiptur_t1", "Tiptur – Coconut Belt", "Tumkur", 13.2619, 76.4805, "T-005"),
        Zone("kunigal_t1", "Kunigal – Paddy Zone", "Tumkur", 13.0228, 77.0289, "T-006"),
        Zone("gubbi_t1", "Gubbi – Mulberry Zone", "Tumkur", 13.3114, 76.9411, "T-007"),
        Zone("koratagere_t1", "Koratagere – Mixed Crop", "Tumkur", 13.5219, 77.2337, "T-008")
    )
}
