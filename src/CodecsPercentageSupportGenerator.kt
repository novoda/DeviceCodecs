object CodecsPercentageSupportGenerator {

    fun generateFrom(codecsInformationResult: CodecsInformationResult): List<CodecsSupportCoverage> {
        val list = mutableListOf<CodecsSupportCoverage>()
        val totalNumberOfDevices = codecsInformationResult.devices.size.toFloat()

        codecsInformationResult.codecs.forEach { (profile, levelMap) ->
            levelMap.forEach { (level, deviceSet) ->
                val percentageSupport = deviceSet.size * 100 / totalNumberOfDevices
                list.add(CodecsSupportCoverage(profile, level, percentageSupport, deviceSet))
            }
        }

        list.sortByDescending { it.coverage }

        println(list.toLog().joinToString("\n"))

        return list
    }

    private fun List<CodecsSupportCoverage>.toLog(): List<CodecsSupportCoverageUI> =
        map { CodecsSupportCoverageUI(it.profile, it.level, it.coverage) }

    private data class CodecsSupportCoverageUI(val profile: String, val level: String, val coverage: Float)

    data class CodecsSupportCoverage(
        val profile: String,
        val level: String,
        val coverage: Float,
        val devices: Set<Device>
    )
}