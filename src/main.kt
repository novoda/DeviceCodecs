import java.io.File
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

val input = """
    "dreamlte","SM-G950F","[canonicalName - null] [isAlias - null] [isHardwareAccelerated - null] [isSoftwareOnly - null] [isVendor - null] [name - OMX.Exynos.avc.dec] [ [ profile:AVCProfileBaseline - level:AVCLevel52 :: profile:AVCProfileConstrainedBaseline - level:AVCLevel52 :: profile:AVCProfileMain - level:AVCLevel52 :: profile:AVCProfileHigh - level:AVCLevel52 :: profile:AVCProfileConstrainedHigh - level:AVCLevel52 ] ]","38898"
""".trimIndent()

fun main(args: Array<String>) {

    val now = System.currentTimeMillis()

    val codecsInformationResult = CodecsInformation.extractFrom(args[0])

    Report.generateFrom(codecsInformationResult)

    println("took: ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now)}s")
}

object CodecsInformation {
    private val profile = "profile:".toCharArray()
    private val level = "level:".toCharArray()

    private var state = State.PROFILE_SEARCH
    private var profileIndex = 0
    private var levelIndex = 0

    fun extractFrom(fileName: String): CodecsInformationResult {
        val devices = HashSet<Device>()
        val codecs = HashMap<String, HashMap<String, Set<Device>>>()

        File(fileName).forEachLine { line ->

            state = State.PROFILE_SEARCH
            profileIndex = 0
            levelIndex = 0

            line
                .takeUnless { it.startsWith("\"devicecodename\"") }
                ?.takeUnless { it.contains("isAlias - true") }
                ?.run { process(this, devices, codecs) }
        }

        return CodecsInformationResult(devices, codecs)
    }

    private fun process(
        line: String,
        devices: HashSet<Device>,
        codecs: HashMap<String, HashMap<String, Set<Device>>>
    ) {
        val split = line.split(",")

        val device = Device(
            codename = split[0].extractName(),
            model = split[1].extractName()
        )

        devices.add(device)
        parseCodecs(input = split[2], device = device, codecs = codecs)
    }

    private fun parseCodecs(input: String, device: Device, codecs: HashMap<String, HashMap<String, Set<Device>>>) {
        val profileName = StringBuilder()
        val levelName = StringBuilder()

        input.toCharArray().forEach { character ->
            when (state) {
                State.PROFILE_SEARCH -> searchProfile(character)
                State.PROFILE_MATCH -> matchProfile(character, profileName)
                State.LEVEL_SEARCH -> searchLevel(character)
                State.LEVEL_MATCH -> matchLevel(character, levelName, profileName, device, codecs)
            }
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

            if (profileNameString != "UNKNOWN" && levelNameString != "UNKNOWN" && profileNameString.toIntOrNull() == null) {
                if (map.containsKey(profileNameString)) {
                    val levelDevice = map[profileNameString]
                    if (levelDevice!!.containsKey(levelNameString)) {
                        levelDevice[levelNameString] = levelDevice[levelNameString]!!.plus(device)
                    } else {
                        levelDevice[levelNameString] = setOf(device)
                    }
                } else {
                    map[profileNameString] = hashMapOf(Pair(levelNameString, setOf(device)))
                }
            }

            levelName.clear()
            profileName.clear()
            state = State.PROFILE_SEARCH
        }
    }

    enum class State {
        PROFILE_SEARCH, LEVEL_SEARCH, PROFILE_MATCH, LEVEL_MATCH
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
//            .filter { it.profile == "AVCProfileBaseline" && it.level == "AVCLevel4" }
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

//object FillImplicitCodecsSupport {
//
//    val map = hashMapOf(
//        "AVC" to listOf("AVCLevel1", "AVCLevel1b", "AVCLevel11", "AVCLevel12", "AVCLevel13", "AVCLevel2", "AVCLevel21", "AVCLevel22", "AVCLevel3", "AVCLevel31", "AVCLevel32", "AVCLevel4", "AVCLevel41", "AVCLevel42", "AVCLevel5", "AVCLevel51", "AVCLevel52", "AVCLevel6", "AVCLevel61", "AVCLevel62")
//    )
//
//    fun augment(list: List<Report.CodecsSupportCoverage>): List<Report.CodecsSupportCoverage> {
//    }
//}

data class Device(val codename: String, val model: String)

data class CodecsInformationResult(val devices: Set<Device>, val codecs: HashMap<String, HashMap<String, Set<Device>>>)

private fun String.extractName(): String =
    substring(startIndex = 1, endIndex = length - 1)
