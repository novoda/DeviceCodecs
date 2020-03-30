import java.util.concurrent.TimeUnit

/**
 * Input parameters
 * [0] - input CSV file with data from SUMO
 * [1] - output CSV file with codec device coverage
 * [2] - any of "AVC", "VP9", "HEVC", "ALL" or if not specified ALL will be applied
 *
 * example:
 * device-codecs.csv coverage-hevc.csv HEVC
 */
fun main(args: Array<String>) {
    val now = System.currentTimeMillis()

    val inputFfilename = args[0]
    val outputFilename = args[1]

    val codecsFilter = CodecsFilter.from(args.getOrNull(2) ?: "ALL")
    val codecsInformationResult = CodecsInformationExtractor.extractFrom(inputFfilename, codecsFilter)
    val codecsSupportCoverage = CodecsPercentageSupportGenerator.generateFrom(codecsInformationResult)
    CvsOutputGenerator.generateFrom(outputFilename, codecsSupportCoverage)

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
