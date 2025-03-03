package org.ivdnt.galahad.app

/**
 * A resource that might be expensive to load, can be encapsulated through this interface
 * so that it only needs to be loaded if it is actually consumed.
 */
interface ExpensiveGettable<T> {
    fun expensiveGet(): T
}

/**
 * Indicates the class has the proper annotations to be serialized to JSON
 */
interface JSONable

interface CRDSet<Key, ReadType, CreateType> {
    // Create
    fun createOrNull(value: CreateType): ReadType?
    fun createOrThrow(value: CreateType): ReadType = createOrNull(value) ?: throw Exception("Failed to create $value")
    // Read
    fun readAll(): Set<ReadType>
    fun readOrNull(key: Key): ReadType? // TODO: see if we can remove this method in favour of readOrThrow
    fun readOrThrow(key: Key) = readOrNull(key) ?: throw Exception("Failed to read $key")
    // Delete
    fun delete(key: Key)
}
