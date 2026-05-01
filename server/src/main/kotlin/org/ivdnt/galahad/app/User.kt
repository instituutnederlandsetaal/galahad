package org.ivdnt.galahad.app

import jakarta.servlet.http.HttpServletRequest
import java.io.File
import org.ivdnt.galahad.app.User.Companion.DEFAULT_USER
import org.ivdnt.galahad.app.User.Companion.USER_HEADER

class User(val id: String) {
    val admin: Boolean
        get() = isAdmin(id)

    companion object {
        internal const val USER_HEADER: String = "remote_user"
        private const val USERNAME: String = "user"
        private val ADMIN_FILE: File = File("data/admins/admins.txt")
        internal val DEFAULT_USER: User
            get() = User(USERNAME)

        /** Check if [username] is in [ADMIN_FILE]. */
        private fun isAdmin(username: String): Boolean =
            username in ADMIN_FILE.takeIf { it.exists() }?.readLines()?.map { it.trim() }.orEmpty()

        /** Get [User] from [USER_HEADER] in [request], or [DEFAULT_USER] if missing. */
        fun fromRequest(request: HttpServletRequest?): User =
            request?.getHeader(USER_HEADER)?.let { User(it) } ?: DEFAULT_USER
    }
}
