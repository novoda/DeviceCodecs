import org.junit.Test
import kotlin.test.assertEquals

class CodecsInformationExtractorTest {

    @Test
    fun `extracts one device for one profile and a base level`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-1.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))))
                )
            ),
            devices = setOf(Device(codename = "gtaxlwifi", model = "SM-T580"))
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `two identical entries extracts one device for one profile and a base level`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-5.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))))
                )
            ),
            devices = setOf(Device(codename = "gtaxlwifi", model = "SM-T580"))
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `two entries with same device and additional level is extracted correctly`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-6.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(
                        Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level11", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level2", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                )
            ),
            devices = setOf(Device(codename = "gtaxlwifi", model = "SM-T580"))
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `four entries with same device and additional level is extracted correctly`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-8.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(
                        Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level11", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level2", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                ),
                Pair(
                    "AVCProfileMain",
                    hashMapOf(
                        Pair("AVCLevel1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("AVCLevel1b", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("AVCLevel11", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                ),
                Pair(
                    "HEVCProfileMain10",
                    hashMapOf(
                        Pair("HEVCMainTierLevel1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("HEVCMainTierLevel2", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("HEVCMainTierLevel21", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                ),
                Pair(
                    "HEVCProfileHigh10",
                    hashMapOf(
                        Pair("HEVCHighTierLevel1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("HEVCHighTierLevel2", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("HEVCHighTierLevel21", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                )
            ),
            devices = setOf(Device(codename = "gtaxlwifi", model = "SM-T580"))
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `two entries with same device and additional profile but same level is extracted correctly`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-7.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))))
                ),
                Pair(
                    "VP9Profile1",
                    hashMapOf(Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))))
                )
            ),
            devices = setOf(Device(codename = "gtaxlwifi", model = "SM-T580"))
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `extracts two devices for one profile and a base level`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-3.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(
                        Pair(
                            "VP9Level1", mutableSetOf(
                                Device(codename = "gtaxlwifi", model = "SM-T580"),
                                Device(codename = "dreamlte", model = "SM-G950F")
                            )
                        )
                    )
                )
            ),
            devices = setOf(
                Device(codename = "gtaxlwifi", model = "SM-T580"),
                Device(codename = "dreamlte", model = "SM-G950F")
            )
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `extracts one device for one profile and populates all missing levels`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-2.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(
                        Pair("VP9Level1", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level11", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level2", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level21", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level3", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level31", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level4", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level41", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level5", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                )
            ),
            devices = setOf(Device(codename = "gtaxlwifi", model = "SM-T580"))
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }

    @Test
    fun `extracts two devices for one profile and populates all missing levels`() {
        val codecsInformationResult = CodecsInformationExtractor.extractFrom("device-codecs-4.csv", CodecsFilter.ALL)

        val expectedCodecsInformationResult = CodecsInformationResult(
            codecs = hashMapOf(
                Pair(
                    "VP9Profile0",
                    hashMapOf(
                        Pair(
                            "VP9Level1",
                            mutableSetOf(
                                Device(codename = "gtaxlwifi", model = "SM-T580"),
                                Device(codename = "dreamlte", model = "SM-G950F")
                            )
                        ),
                        Pair(
                            "VP9Level11",
                            mutableSetOf(
                                Device(codename = "gtaxlwifi", model = "SM-T580"),
                                Device(codename = "dreamlte", model = "SM-G950F")
                            )
                        ),
                        Pair(
                            "VP9Level2",
                            mutableSetOf(
                                Device(codename = "gtaxlwifi", model = "SM-T580"),
                                Device(codename = "dreamlte", model = "SM-G950F")
                            )
                        ),
                        Pair(
                            "VP9Level21",
                            mutableSetOf(
                                Device(codename = "gtaxlwifi", model = "SM-T580"),
                                Device(codename = "dreamlte", model = "SM-G950F")
                            )
                        ),
                        Pair("VP9Level3", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level31", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level4", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level41", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580"))),
                        Pair("VP9Level5", mutableSetOf(Device(codename = "gtaxlwifi", model = "SM-T580")))
                    )
                )
            ),
            devices = setOf(
                Device(codename = "gtaxlwifi", model = "SM-T580"),
                Device(codename = "dreamlte", model = "SM-G950F")
            )
        )

        assertEquals(
            expected = expectedCodecsInformationResult,
            actual = codecsInformationResult
        )
    }
}