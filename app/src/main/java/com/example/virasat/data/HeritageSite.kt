package com.example.virasat.data

data class HeritageSite(
    val id: String,
    val name: String,
    val location: String,
    val history: String,
    val architecture: String,
    val legend: String,
    val hiddenFact: String,
    val storyText: String,
    val audioRes: Int,
    val imageRes: Int,
    val latitude: Double,
    val longitude: Double,
    val titleKn: String? = null,
    val locationKn: String? = null,
    val historyKn: String? = null,
    val architectureKn: String? = null,
    val legendKn: String? = null,
    val hiddenFactKn: String? = null,
    val storyTextKn: String? = null
) {
    fun displayName(useKannada: Boolean): String = if (useKannada) titleKn ?: name else name
    fun displayLocation(useKannada: Boolean): String = if (useKannada) locationKn ?: location else location
    fun displayHistory(useKannada: Boolean): String = if (useKannada) historyKn ?: history else history
    fun displayArchitecture(useKannada: Boolean): String = if (useKannada) architectureKn ?: architecture else architecture
    fun displayLegend(useKannada: Boolean): String = if (useKannada) legendKn ?: legend else legend
    fun displayHiddenFact(useKannada: Boolean): String = if (useKannada) hiddenFactKn ?: hiddenFact else hiddenFact
    fun displayStory(useKannada: Boolean): String = if (useKannada) storyTextKn ?: storyText else storyText
}
