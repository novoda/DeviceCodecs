import java.io.File
import java.util.concurrent.TimeUnit

val input = listOf(
    """
    "dreamlte","SM-G950F","[canonicalName - null] [isAlias - null] [isHardwareAccelerated - null] [isSoftwareOnly - null] [isVendor - null] [name - OMX.Exynos.avc.dec] [ [ profile:AVCProfileBaseline - level:AVCLevel52 :: profile:AVCProfileConstrainedBaseline - level:AVCLevel52 :: profile:AVCProfileMain - level:AVCLevel52 :: profile:AVCProfileHigh - level:AVCLevel52 :: profile:AVCProfileConstrainedHigh - level:AVCLevel52 ] ]","38898"
""".trimIndent(),
    """
    "another","device","[canonicalName - null] [isAlias - null] [isHardwareAccelerated - null] [isSoftwareOnly - null] [isVendor - null] [name - OMX.Exynos.avc.dec] [ [ profile:AVCProfileBaseline - level:AVCLevel52 :: profile:AVCProfileConstrainedBaseline - level:AVCLevel4 :: profile:AVCProfileMain - level:AVCLevel4 :: profile:AVCProfileHigh - level:AVCLevel4 :: profile:AVCProfileConstrainedHigh - level:AVCLevel4 ] ]","38898"
""".trimIndent()
)

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

fun main(args: Array<String>) {

    val now = System.currentTimeMillis()

    val codecsInformationResult = CodecsInformation.extractFrom(args[0])

    Report.generateFrom(codecsInformationResult)

    println("took: ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now)}s")
}

object CodecsInformation {
    private val profile = "profile:".toCharArray()
    private val level = "level:".toCharArray()

    private var state = State.DEVICE_CODENAME_SEARCH
    private var profileIndex = 0
    private var levelIndex = 0

    fun extractFrom(fileName: String): CodecsInformationResult {
        val devices = HashSet<Device>()
        val codecs = HashMap<String, HashMap<String, MutableSet<Device>>>()
        var firstLineSkipped = false

//        input.forEach { line ->
//            state = State.DEVICE_CODENAME_SEARCH
//            profileIndex = 0
//            levelIndex = 0
//
//            extractDeviceProfilesLevels(line).also { deviceProfilesLevels ->
//                println(codecs.toList().joinToString("\n"))
//                populateCodecs(deviceProfilesLevels, codecs)
//                //println(codecs.toList().joinToString("\n"))
//                devices.add(deviceProfilesLevels.device)
//            }
//        }
//
//        println(codecs.toList().joinToString("\n"))

        File(fileName).forEachLine { line ->

            if (firstLineSkipped) {
                state = State.DEVICE_CODENAME_SEARCH
                profileIndex = 0
                levelIndex = 0

                extractDeviceProfilesLevels(line).also { deviceProfilesLevels ->
                    populateCodecs(deviceProfilesLevels, codecs)
                    devices.add(deviceProfilesLevels.device)
                }
            } else {
                firstLineSkipped = true
            }
        }

        return CodecsInformationResult(devices, codecs)
    }

    private fun populateCodecs(
        deviceProfilesLevels: DeviceProfilesLevels,
        codecs: HashMap<String, HashMap<String, MutableSet<Device>>>
    ) {
        deviceProfilesLevels.profileLevels.forEach { profileLevel ->
            val profile = codecs[profileLevel.profile]
            if (profile == null) {
                codecs[profileLevel.profile] =
                    hashMapOf(Pair(profileLevel.level, mutableSetOf(deviceProfilesLevels.device)))
                augmentMissingExplicitLevels(
                    deviceProfilesLevels.device,
                    profileLevel.profile,
                    profileLevel.level,
                    codecs[profileLevel.profile]!!
                )
            } else {
                val level = profile[profileLevel.level]
                if (level == null) {
                    profile[profileLevel.level] = mutableSetOf(deviceProfilesLevels.device)
                    augmentMissingExplicitLevels(
                        deviceProfilesLevels.device,
                        profileLevel.profile,
                        profileLevel.level,
                        profile
                    )
                } else {
                    profile[profileLevel.level]!!.add(deviceProfilesLevels.device)
                    augmentMissingExplicitLevels(
                        deviceProfilesLevels.device,
                        profileLevel.profile,
                        profileLevel.level,
                        profile
                    )
                }
            }
        }
    }

    private fun augmentMissingExplicitLevels(
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

    private fun extractDeviceProfilesLevels(line: String): DeviceProfilesLevels {
        val profilesLevels = mutableListOf<DeviceProfilesLevels.ProfileLevel>()
        val deviceCodename = StringBuilder()
        val deviceModel = StringBuilder()
        val profileName = StringBuilder()
        val levelName = StringBuilder()

        line.toCharArray().forEach { character ->
            when (state) {
                State.DEVICE_CODENAME_SEARCH -> searchDeviceCodename(character)
                State.DEVICE_CODENAME_MATCH -> matchDeviceCodename(character, deviceCodename)
                State.DEVICE_MODEL_SEARCH -> searchDeviceModel(character, deviceModel)
                State.DEVICE_MODEL_MATCH -> matchDeviceModel(character, deviceModel)
                State.PROFILE_SEARCH -> searchProfile(character)
                State.PROFILE_MATCH -> matchProfile(character, profileName)
                State.LEVEL_SEARCH -> searchLevel(character)
                State.LEVEL_MATCH -> matchLevel(
                    character,
                    levelName,
                    profileName,
                    profilesLevels
                )
            }
        }

        return DeviceProfilesLevels(
            Device(deviceCodename.toString(), deviceModel.toString()),
            profilesLevels.toList()
        )
    }

    data class DeviceProfilesLevels(
        val device: Device,
        val profileLevels: List<ProfileLevel>
    ) {
        data class ProfileLevel(val profile: String, val level: String)
    }

    private fun searchDeviceCodename(character: Char) {
        if (character == '"') {
            state = State.DEVICE_CODENAME_MATCH
        }
    }

    private fun searchDeviceModel(character: Char, deviceModel: java.lang.StringBuilder) {
        if (character != '"' && character != ',') {
            deviceModel.append(character)
            state = State.DEVICE_MODEL_MATCH
        }
    }

    private fun searchProfile(character: Char) {
        if (character == profile[profileIndex]) {
            profileIndex++

            if (profileIndex == profile.size) {
                profileIndex = 0
                state = State.PROFILE_MATCH
            }
        } else {
            profileIndex = 0
        }
    }

    private fun matchDeviceCodename(character: Char, deviceCodename: StringBuilder) {
        if (character != '"') {
            deviceCodename.append(character)
        } else {
            state = State.DEVICE_MODEL_SEARCH
        }
    }

    private fun matchDeviceModel(character: Char, deviceModel: StringBuilder) {
        if (character != '"') {
            deviceModel.append(character)
        } else {
            state = State.PROFILE_SEARCH
        }
    }

    private fun matchProfile(character: Char, profileName: StringBuilder) {
        if (character != ' ') {
            profileName.append(character)
        } else {
            state = State.LEVEL_SEARCH
        }
    }

    private fun searchLevel(character: Char) {
        if (character == level[levelIndex]) {
            levelIndex++

            if (levelIndex == level.size) {
                levelIndex = 0
                state = State.LEVEL_MATCH
            }
        } else {
            levelIndex = 0
        }
    }

    private fun matchLevel(
        character: Char,
        levelName: StringBuilder,
        profileName: StringBuilder,
        profilesLevels: MutableList<DeviceProfilesLevels.ProfileLevel>
    ) {
        if (character != ' ') {
            levelName.append(character)
        } else {
            val profileNameString = profileName.toString()
            val levelNameString = levelName.toString()

            if (profileNameString != "UNKNOWN" && levelNameString != "UNKNOWN" && profileNameString.toIntOrNull() == null) {
                profilesLevels.add(DeviceProfilesLevels.ProfileLevel(profileNameString, levelNameString))
            }

            levelName.clear()
            profileName.clear()
            state = State.PROFILE_SEARCH
        }
    }

    enum class State {
        DEVICE_CODENAME_SEARCH,
        DEVICE_MODEL_SEARCH,
        PROFILE_SEARCH,
        LEVEL_SEARCH,
        DEVICE_CODENAME_MATCH,
        DEVICE_MODEL_MATCH,
        PROFILE_MATCH, LEVEL_MATCH
    }
}

object Report {
    fun generateFrom(codecsInformationResult: CodecsInformationResult) {
        val list = mutableListOf<CodecsSupportCoverage>()
        val totalNumberOfDevices = codecsInformationResult.devices.size

        codecsInformationResult.codecs.forEach { (profile, levelMap) ->
            levelMap.forEach { (level, deviceSet) ->
                val percentageSupport = deviceSet.size * 100 / totalNumberOfDevices.toFloat()
                list.add(CodecsSupportCoverage(profile, level, percentageSupport, deviceSet))
            }
        }

        list.sortByDescending { it.coverage }

        println(list.toLog().joinToString("\n"))

//        val listLevel4 = list
//            .filter { it.profile == "AVCProfileBaseline" && it.level == "AVCLevel3" }
//            .map { it.devices }[0]
//
//        println("${codecsInformationResult.devices.size - listLevel4.size}")
//
//        val diff: MutableSet<Device> = codecsInformationResult.devices.toMutableSet().apply { removeAll(listLevel4) }
//        println(diff)
    }

    data class CodecsSupportCoverage(
        val profile: String,
        val level: String,
        val coverage: Float,
        val devices: Set<Device>
    )

    data class CodecsSupportCoverageUI(val profile: String, val level: String, val coverage: Float)

    private fun List<CodecsSupportCoverage>.toLog(): List<CodecsSupportCoverageUI> {
        return map { CodecsSupportCoverageUI(it.profile, it.level, it.coverage) }
    }
}

data class Device(val codename: String, val model: String)

data class CodecsInformationResult(
    val devices: Set<Device>,
    val codecs: HashMap<String, HashMap<String, MutableSet<Device>>>
)

private fun String.extractName(): String =
    substring(startIndex = 1, endIndex = length - 1)
