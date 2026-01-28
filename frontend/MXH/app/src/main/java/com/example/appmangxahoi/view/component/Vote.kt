package com.example.appmangxahoi.view.component

import com.example.appmangxahoi.view.screens.VoteType
data class VoteAction(
    val apiType: String?,
    val shouldCallApi: Boolean,
    val shouldNotify: Boolean
)

object VoteHelper {

    fun resolveVote(
        oldVote: VoteType,
        newVote: VoteType
    ): VoteAction {

        // click trùng trạng thái → không làm gì
        if (oldVote == newVote) {
            return VoteAction(
                apiType = null,
                shouldCallApi = false,
                shouldNotify = false
            )
        }

        return when {
            // ---- BỎ VOTE ----
            newVote == VoteType.none -> VoteAction(
                apiType = "remove",
                shouldCallApi = true,
                shouldNotify = false
            )

            // ---- UPVOTE ----
            newVote == VoteType.upvote -> VoteAction(
                apiType = "upvote",
                shouldCallApi = true,
                shouldNotify = true
            )

            // ---- DOWNVOTE ----
            newVote == VoteType.downvote -> VoteAction(
                apiType = "downvote",
                shouldCallApi = true,
                shouldNotify = true
            )

            else -> VoteAction(null, false, false)
        }
    }
}
