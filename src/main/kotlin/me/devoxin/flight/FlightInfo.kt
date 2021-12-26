package me.devoxin.flight

public object FlightInfo {
    public val VERSION: String

    public val GIT_REVISION: String

    init {
        val (buildVersion, buildRevision) = FlightInfo::class.java.classLoader.getResourceAsStream("flight.txt")!!
            .reader()
            .readText()
            .lines()

        VERSION = buildVersion
        GIT_REVISION = buildRevision
    }
}
