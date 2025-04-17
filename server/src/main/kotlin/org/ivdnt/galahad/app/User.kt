package org.ivdnt.galahad.app

import jakarta.servlet.http.HttpServletRequest
import java.io.File

class User(
    val id: String,
    val isAdmin: Boolean = false,
) {
    companion object {
        private const val USERNAME: String = "user"
        private val ADMIN_FILE: File = File("data/admins/admins.txt")
        private val DEFAULT_USER: User get() = User(id = USERNAME, isAdmin = isAdmin(USERNAME))

        private fun isAdmin(username: String): Boolean {
            if (!ADMIN_FILE.exists()) return false // When no admins are set, no one is admin by default
            return username in ADMIN_FILE.readLines().map { it.trim() } // Otherwise only declared admins are admins
        }

        fun fromRequest(request: HttpServletRequest?): User {
            return try {
                // This is the header used by the portal. We cannot spoof it
                val remoteUser = request!!.getHeader("remote_user")
                User(id = remoteUser, isAdmin = isAdmin(remoteUser))
            } catch (_: Exception) {
                // happens when the application is run in prod mode, but without the portal
                // In this case we default to a single user instance
                // Note that admin status is not set here
                DEFAULT_USER
            }
        }
    }
}