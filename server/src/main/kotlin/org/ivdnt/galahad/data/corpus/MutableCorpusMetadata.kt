package org.ivdnt.galahad.data.corpus

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.app.JSONable
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.exceptions.CorpusNameInvalidException
import org.ivdnt.galahad.exceptions.CorpusUnauthorizedException
import org.springframework.lang.Nullable
import java.net.URL

/**
 * Corpus metadata that can be changed by the user.
 * Although technically [owner] should only be set once.
 */
open class MutableCorpusMetadata(
    @JsonProperty("owner") var owner: String,
    @JsonProperty("name") var name: String,
    @JsonProperty("eraFrom") var eraFrom: Int,
    @JsonProperty("eraTo") var eraTo: Int,
    @JsonProperty("language") @Nullable var language: String?,
    @JsonProperty("tagset") @Nullable var tagset: String?,
    @JsonProperty("dataset") var dataset: Boolean,
    @JsonProperty("collaborators") var collaborators: MutableSet<String>,
    @JsonProperty("viewers") var viewers: MutableSet<String>,
    @JsonProperty("sourceName") @Nullable var sourceName: String?,
    @JsonProperty("sourceURL") @Nullable var sourceURL: URL?,
) : JSONable {

    /**
     * Whether the user is in the list of collaborators of this corpus.
     * Note that this is not the same as having write access: use [hasWriteAccess].
     */
    fun isCollaborator(user: User): Boolean {
        return collaborators.contains(user.id) == true
    }

    /**
     * Whether the user is in the list of viewers of this corpus.
     * Note that this is not the same as having read access: use [hasReadAccess].
     */
    fun isViewer(user: User): Boolean {
        return viewers.contains(user.id) == true
    }

    /** To have write access, you need to be an owner, collaborator or admin. */
    fun hasWriteAccess(user: User): Boolean {
        if (user.isAdmin) return true
        if (owner == user.id) return true
        return isCollaborator(user)
    }

    /** Only the owner can delete a corpus, unless you are an admin. */
    fun canDelete(user: User): Boolean {
        if (user.isAdmin) return true
        return owner == user.id
    }

    /** Only the owner and admin can add new collaborators and viewers. */
    fun canAddNewUsers(user: User): Boolean {
        if (user.isAdmin) return true
        return owner == user.id
    }

    /** Only admins can make corpora into benchmark datasets. */
    fun canDefineDataset(user: User): Boolean {
        return user.isAdmin
    }

    fun removeAsViewer(user: User) {
        viewers.removeIf { i -> i == user.id }
    }

    fun removeAsCollaborator(user: User) {
        collaborators.removeIf { i -> i == user.id }
    }

    /**
     * You can view a corpus if you are a viewer, collaborator or owner of that corpus, or if it's public.
     * Although admins have access to everything, you might not want to see all corpora listed in your own view,
     * so optionally exclude them.
     */
    fun hasReadAccess(user: User, excludeAdmin: Boolean = false): Boolean {
        if (!excludeAdmin) {
            if (user.isAdmin) return true
        }
        if (dataset) return true // technically, datasets are always public, but still.
        if (isCollaborator(user)) return true
        if (isViewer(user)) return true
        if (owner == user.id) return true
        return false
    }

    companion object {

        private fun assertCorpusNameValidOrThrow(corpusName: String) {
            if (!Regex("^.{3,100}$").matches(corpusName.trim())) {
                throw CorpusNameInvalidException(corpusName)
            }
        }

        /**
         * Overwrite the [CorpusMetadata] in [metadata] with [newMeta],
         * except for the owner, which should be grabbed from the existing [metadata].
         *
         * If a user appears multiple times in the permission hierarchy, only the upper level remains.
         */
        fun clean(
            user: User,
            newMeta: MutableCorpusMetadata,
            oldMeta: MutableCorpusMetadata? = null,
        ): MutableCorpusMetadata {
            // Overwrite the owner with the original, so collaborators can't change it,
            // unless it's empty, in which case it's a new corpus.
            newMeta.owner = oldMeta?.owner ?: user.id

            // Viewers are allowed to remove themselves, but no more than that.
            if (!newMeta.isViewer(user) && oldMeta?.isViewer(user) == true) {
                oldMeta.removeAsViewer(user)
                // ignore all other changes and return the old metadata, minus the viewer
                return oldMeta
            }

            // Same for collaborators
            if (!newMeta.isCollaborator(user) && oldMeta?.isCollaborator(user) == true) {
                oldMeta.removeAsCollaborator(user)
                // Although collaborators could change other metadata,
                // if you have chosen to remove yourself as a collaborator,
                // you probably don't want to change anything else.
                return oldMeta
            }

            assertCorpusNameValidOrThrow(newMeta.name)

            if (oldMeta?.dataset == false && newMeta.dataset) {
                // Corpus is being set to public
                if (!newMeta.canDefineDataset(user)) {
                    throw CorpusUnauthorizedException("Cannot create a dataset.")
                }
            }

            if (oldMeta?.collaborators != newMeta.collaborators || oldMeta.viewers != newMeta.viewers) {
                // Collaborators have changed
                if (!newMeta.canAddNewUsers(user)) {
                    throw CorpusUnauthorizedException("Cannot change collaborators or viewers.")
                }
            }

            // Trim textual inputs
            newMeta.apply {
                name = name.trim()
                sourceName = sourceName?.trim()
                tagset = tagset?.trim()
                language = language?.trim()
                collaborators = collaborators.map { it.trim() }.toMutableSet()
                viewers = viewers.map { it.trim() }.toMutableSet()
            }

            // Remove owner from list of collaborators & viewers
            newMeta.collaborators.remove(newMeta.owner)
            newMeta.viewers.remove(newMeta.owner)

            // Remove collaborators from list of viewers
            newMeta.viewers.removeIf { newMeta.collaborators.contains(it) }

            return newMeta
        }
    }
}