enum class Commands(val value: Int) {
    MOVE(0), ATTACK(1);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value }
    }
}

enum class ServerCommands(val value: Int) {
    GAME_INFO(2), GAME_SESSION_ENDED(3);

    companion object {
        fun fromValue(value: Int) = ServerCommands.entries.find { it.value == value }
    }
}