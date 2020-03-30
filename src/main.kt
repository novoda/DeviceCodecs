import java.util.concurrent.TimeUnit

val input = listOf(
    """
    "dreamlte","SM-G950F","[canonicalName - null] [isAlias - null] [isHardwareAccelerated - null] [isSoftwareOnly - null] [isVendor - null] [name - OMX.Exynos.avc.dec] [ [ profile:AVCProfileBaseline - level:AVCLevel52 :: profile:AVCProfileConstrainedBaseline - level:AVCLevel52 :: profile:AVCProfileMain - level:AVCLevel52 :: profile:AVCProfileHigh - level:AVCLevel52 :: profile:AVCProfileConstrainedHigh - level:AVCLevel52 ] ]","38898"
""".trimIndent(),
    """
    "another","device","[canonicalName - null] [isAlias - null] [isHardwareAccelerated - null] [isSoftwareOnly - null] [isVendor - null] [name - OMX.Exynos.avc.dec] [ [ profile:AVCProfileBaseline - level:AVCLevel52 :: profile:AVCProfileConstrainedBaseline - level:AVCLevel4 :: profile:AVCProfileMain - level:AVCLevel4 :: profile:AVCProfileHigh - level:AVCLevel4 :: profile:AVCProfileConstrainedHigh - level:AVCLevel4 ] ]","38898"
""".trimIndent()
)


/**
 * Input parameters
 * [0] - CSV file
 * [1] - any of "AVC", "VP9", "HEVC", "ALL" or if not specified ALL will be applied
 */
fun main(args: Array<String>) {
    val now = System.currentTimeMillis()

    val filename = args[0]

    val codecsFilter = CodecsFilter.from(args.getOrNull(1) ?: "ALL")
    val codecsInformationResult = CodecsInformationExtractor.extractFrom(filename, codecsFilter)
    CodecsPercentageSupportGenerator.generateFrom(codecsInformationResult)

    println("took: ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now)}s")
}

enum class CodecsFilter {
    AVC, VP9, HEVC, ALL;

    companion object {
        fun from(rawValue: String): CodecsFilter =
            values().find { it.name == rawValue }
                ?: ALL.also { println("No codecs filter will be applied") }
    }
}
