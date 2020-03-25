import java.lang.StringBuilder

val input = """
    "dreamlte","SM-G950F","[canonicalName - null] [isAlias - null] [isHardwareAccelerated - null] [isSoftwareOnly - null] [isVendor - null] [name - OMX.Exynos.avc.dec] [ [ profile:AVCProfileBaseline - level:AVCLevel52 :: profile:AVCProfileConstrainedBaseline - level:AVCLevel52 :: profile:AVCProfileMain - level:AVCLevel52 :: profile:AVCProfileHigh - level:AVCLevel52 :: profile:AVCProfileConstrainedHigh - level:AVCLevel52 ] ]","38898"
""".trimIndent()

val profile = "profile:".toCharArray()
val level = "level:".toCharArray()

var state = State.PROFILE_SEARCH
var profileIndex = 0
var levelIndex = 0

fun main(args: Array<String>) {
    val split = input.split(",")

    val device = Device(
        codename = split.toCodename(),
        model = split.toName()
    )

    val codecsMap = split.toCodecsMap(device)

    println(codecsMap)
}

private fun  List<String>.toCodecsMap(device: Device): HashMap<String, HashMap<String, Set<Device>>> {
    val map = HashMap<String, HashMap<String, Set<Device>>>()

    val codecs = this[2]
    val profileName = StringBuilder()
    val levelName = StringBuilder()

    codecs.toCharArray().forEach { character ->
        when (state) {
            State.PROFILE_SEARCH -> searchProfile(character)
            State.PROFILE_MATCH -> matchProfile(character, profileName)
            State.LEVEL_SEARCH -> searchLevel(character)
            State.LEVEL_MATCH -> matchLevel(character, levelName, profileName, device, map)
        }
    }

    return map
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
    device: Device,
    map: HashMap<String, HashMap<String, Set<Device>>>
) {
    if (character != ' ') {
        levelName.append(character)
    } else {
        val profileNameString = profileName.toString()
        val levelNameString = levelName.toString()

        if (map.containsKey(profileNameString)) {
            val levelDevice = map[profileNameString]
            if (levelDevice!!.containsKey(levelNameString)) {
                levelDevice[levelNameString]!!.plus(device)
            } else {
                levelDevice[levelNameString] = setOf(device)
            }
        } else {
            map[profileNameString] = hashMapOf(Pair(levelNameString, setOf(device)))
        }

        levelName.clear()
        profileName.clear()
        state = State.PROFILE_SEARCH
    }
}

enum class State {
    PROFILE_SEARCH, LEVEL_SEARCH, PROFILE_MATCH, LEVEL_MATCH
}

private fun List<String>.toCodename(): String =
    this[0].substring(startIndex = 1, endIndex = this[0].length - 1)

private fun List<String>.toName(): String =
    this[1].substring(startIndex = 1, endIndex = this[0].length - 1)

private fun List<String>.toCodecs(): String =
    this[2].substring(
        startIndex = this[2].indexOf("[ ["),
        endIndex = this[2].indexOf("] ]")
    ).substring(4)

data class Device(val codename: String, val model: String)
