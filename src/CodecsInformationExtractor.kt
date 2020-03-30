import java.io.File

object CodecsInformationExtractor {

    private val profile = "profile:".toCharArray()
    private val level = "level:".toCharArray()

    private var state = State.DEVICE_CODENAME_SEARCH
    private var profileIndex = 0
    private var levelIndex = 0

    fun extractFrom(fileName: String, codecsFilter: CodecsFilter): CodecsInformationResult {
        val devices = HashSet<Device>()
        val codecs = HashMap<String, HashMap<String, MutableSet<Device>>>()
        var firstLineSkipped = false

        File(fileName).forEachLine { line ->

            if (firstLineSkipped) {
                state = State.DEVICE_CODENAME_SEARCH
                profileIndex = 0
                levelIndex = 0

                extractDeviceProfilesLevels(line).also {
                    it?.let { deviceProfilesLevels ->
                        populateCodecs(deviceProfilesLevels, codecs, codecsFilter)
                        devices.add(deviceProfilesLevels.device)
                    }
                }
            } else {
                firstLineSkipped = true
            }
        }

        return CodecsInformationResult(devices, codecs)
    }

    private fun extractDeviceProfilesLevels(line: String): DeviceProfilesLevels? {
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

        if (profilesLevels.isNotEmpty()) {
            return DeviceProfilesLevels(
                Device(deviceCodename.toString(), deviceModel.toString()),
                profilesLevels.toList()
            )
        } else {
            return null
        }
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

    private fun populateCodecs(
        deviceProfilesLevels: DeviceProfilesLevels,
        codecs: HashMap<String, HashMap<String, MutableSet<Device>>>,
        codecsFilter: CodecsFilter
    ) {
        deviceProfilesLevels.profileLevels
            .filter { profileLevels -> codecsFilter == CodecsFilter.ALL || profileLevels.profile.startsWith(codecsFilter.name) }
            .forEach { profileLevel ->
                val profile = codecs[profileLevel.profile]
                if (profile == null) {
                    codecs[profileLevel.profile] =
                        hashMapOf(Pair(profileLevel.level, mutableSetOf(deviceProfilesLevels.device)))
                } else {
                    val level = profile[profileLevel.level]
                    if (level == null) {
                        profile[profileLevel.level] = mutableSetOf(deviceProfilesLevels.device)
                    } else {
                        profile[profileLevel.level]!!.add(deviceProfilesLevels.device)
                    }
                }
                DataAugmentator.augmentMissingExplicitLevels(
                    deviceProfilesLevels.device,
                    profileLevel.profile,
                    profileLevel.level,
                    codecs[profileLevel.profile]!!
                )
            }
    }

    private enum class State {
        DEVICE_CODENAME_SEARCH,
        DEVICE_MODEL_SEARCH,
        PROFILE_SEARCH,
        LEVEL_SEARCH,
        DEVICE_CODENAME_MATCH,
        DEVICE_MODEL_MATCH,
        PROFILE_MATCH, LEVEL_MATCH
    }

    private data class DeviceProfilesLevels(
        val device: Device,
        val profileLevels: List<ProfileLevel>
    ) {
        data class ProfileLevel(val profile: String, val level: String)
    }
}

data class Device(val codename: String, val model: String)

data class CodecsInformationResult(
    val devices: Set<Device>,
    val codecs: HashMap<String, HashMap<String, MutableSet<Device>>>
)
