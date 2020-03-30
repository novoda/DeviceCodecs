object DataAugmentator {

    fun augmentMissingExplicitLevels(
        device: Device,
        profile: String,
        level: String,
        profiles: java.util.HashMap<String, MutableSet<Device>>
    ) {
        when {
            profile.startsWith("AVC") -> populate(level, profiles, device, mapAVCLevels)
            profile.startsWith("HEVC") && level.startsWith("HEVCMain") -> populate(
                level,
                profiles,
                device,
                mapHEVCMainLevels
            )
            profile.startsWith("HEVC") && level.startsWith("HEVCHigh") -> populate(
                level,
                profiles,
                device,
                mapHEVCHighLevels
            )
            profile.startsWith("VP9") -> populate(level, profiles, device, mapVP9Levels)
        }
    }

    private fun populate(
        level: String,
        profiles: java.util.HashMap<String, MutableSet<Device>>,
        device: Device,
        listOfAllLevels: List<String>
    ) {
        val indexOfLevel = listOfAllLevels.indexOf(level)

        for (i in 0 until indexOfLevel) {
            val levelDevice = profiles[listOfAllLevels[i]]
            if (levelDevice == null) {
                profiles[listOfAllLevels[i]] = mutableSetOf(device)
            } else {
                profiles[listOfAllLevels[i]]!!.add(device)
            }
        }
    }
}

val mapAVCLevels = listOf(
    "AVCLevel1",
    "AVCLevel1b",
    "AVCLevel11",
    "AVCLevel12",
    "AVCLevel13",
    "AVCLevel2",
    "AVCLevel21",
    "AVCLevel22",
    "AVCLevel3",
    "AVCLevel31",
    "AVCLevel32",
    "AVCLevel4",
    "AVCLevel41",
    "AVCLevel42",
    "AVCLevel5",
    "AVCLevel51",
    "AVCLevel52",
    "AVCLevel6",
    "AVCLevel61",
    "AVCLevel62"
)

val mapHEVCMainLevels = listOf(
    "HEVCMainTierLevel1",
    "HEVCMainTierLevel2",
    "HEVCMainTierLevel21",
    "HEVCMainTierLevel3",
    "HEVCMainTierLevel31",
    "HEVCMainTierLevel4",
    "HEVCMainTierLevel41",
    "HEVCMainTierLevel5",
    "HEVCMainTierLevel51",
    "HEVCMainTierLevel52",
    "HEVCMainTierLevel6",
    "HEVCMainTierLevel61",
    "HEVCMainTierLevel62"
)

val mapHEVCHighLevels = listOf(
    "HEVCHighTierLevel1",
    "HEVCHighTierLevel2",
    "HEVCHighTierLevel21",
    "HEVCHighTierLevel3",
    "HEVCHighTierLevel31",
    "HEVCHighTierLevel4",
    "HEVCHighTierLevel41",
    "HEVCHighTierLevel5",
    "HEVCHighTierLevel51",
    "HEVCHighTierLevel52",
    "HEVCHighTierLevel6",
    "HEVCHighTierLevel61",
    "HEVCHighTierLevel62"
)

val mapVP9Levels = listOf(
    "VP9Level1",
    "VP9Level11",
    "VP9Level2",
    "VP9Level21",
    "VP9Level3",
    "VP9Level31",
    "VP9Level4",
    "VP9Level41",
    "VP9Level5",
    "VP9Level51",
    "VP9Level52",
    "VP9Level6",
    "VP9Level61",
    "VP9Level62"
)