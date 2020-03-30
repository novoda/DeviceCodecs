import java.io.File

object CvsOutputGenerator {

    fun generateFrom(
        outputFileName: String,
        codecsSupportCoverage: List<CodecsPercentageSupportGenerator.CodecsSupportCoverage>
    ) {
        File(outputFileName).printWriter().use { out ->
            out.println(
                """
                PROFILE, LEVEL, COVERAGE
            """.trimIndent()
            )

            codecsSupportCoverage.forEach { codecsSupportCoverage ->
                out.println(
                    """
                    ${codecsSupportCoverage.profile}, ${codecsSupportCoverage.level}, ${codecsSupportCoverage.coverage}
                """.trimIndent()
                )
            }
        }
    }
}