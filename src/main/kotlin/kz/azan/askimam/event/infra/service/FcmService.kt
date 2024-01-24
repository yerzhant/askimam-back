package kz.azan.askimam.event.infra.service

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.infra.dao.FcmTokenDao
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class FcmService(
    private val fcmTokenDao: FcmTokenDao,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().size == 0) {
            FirebaseApp.initializeApp()
        }
    }

    fun notify(tokens: List<String>, subject: Subject?, text: NonBlankString) {
        if (tokens.isEmpty()) return

        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(subject?.value?.value)
                    .setBody(text.value)
                    .build()
            )
            .addAllTokens(tokens)
            .build()

        FirebaseMessaging.getInstance().sendEachForMulticast(message).run {
            if (failureCount > 0) {
                responses.indices
                    .filterNot { responses[it].isSuccessful }
                    .forEach {
                        logger.warn("Deleting an FCM token ${tokens[it]}")
                        fcmTokenDao.deleteById(tokens[it])
                    }
            }
        }
    }
}