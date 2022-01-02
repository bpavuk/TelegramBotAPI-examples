import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptions
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.media.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.filters.MessageFilterByChat
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.utils.plus
import dev.inmo.tgbotapi.extensions.utils.shortcuts.*
import dev.inmo.tgbotapi.types.message.abstracts.MediaGroupMessage
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun activateResenderBot(
    token: String,
    print: (Any) -> Unit
) {
    val bot = telegramBot(token)

    print(bot.getMe())

    bot.buildBehaviourWithLongPolling(CoroutineScope(coroutineContext + SupervisorJob())) {
        onContentMessage(
            subcontextUpdatesFilter = MessageFilterByChat + BehaviourContextAndTwoTypesReceiver { it, _ -> it !is MediaGroupMessage<*> }
        ) {
            executeUnsafe(it.content.createResend(it.chat.id, replyToMessageId = it.messageId))
        }
        onVisualGallery {
            sendVisualMediaGroup(it.chat!!, it.map { it.content.toMediaGroupMemberInputMedia() })
        }
        onPlaylist {
            sendPlaylist(it.chat!!, it.map { it.content.toMediaGroupMemberInputMedia() })
        }
        onDocumentsGroup {
            sendDocumentsGroup(it.chat!!, it.map { it.content.toMediaGroupMemberInputMedia() })
        }

        allUpdatesFlow.subscribeSafelyWithoutExceptions(this) {
            println(it)
        }
    }.join()
}
