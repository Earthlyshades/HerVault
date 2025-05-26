package com.earthlyapps.hervault.models

data class Notification(
    var id: Int,
    var title:
    String, var message: String
) {
    companion object {
        var supportMessages = listOf(
            Notification(1, "Check-in", "Ask how your partner is feeling today ❤️"),
            Notification(2, "Support Tip", "Offer to handle a chore for them today"),
            Notification(3, "Reminder", "Give them a genuine compliment"),
            Notification(4, "Cycle Aware", "They might need extra patience today"),
            Notification(5, "Connection", "Plan a quiet moment together tonight")
        )
    }
}