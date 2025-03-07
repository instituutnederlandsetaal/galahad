package org.ivdnt.galahad.app

import jakarta.servlet.http.HttpServletRequest
import java.io.File

class User(
    val id: String,
    val isAdmin: Boolean = false,
) {
    companion object {
        const val USERNAME = "user"
        val DEFAULT_USER = User(id = USERNAME, isAdmin = isAdmin(USERNAME))
        val ADMIN_FILE = File("data/admins/admins.txt")

        private fun isAdmin(string: String): Boolean {
            if (!ADMIN_FILE.exists()) return false // When no admins are set, no one is admin by default
            return ADMIN_FILE.readLines().map { it.trim() }
                .contains(string) // Otherwise only declared admins are admins
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