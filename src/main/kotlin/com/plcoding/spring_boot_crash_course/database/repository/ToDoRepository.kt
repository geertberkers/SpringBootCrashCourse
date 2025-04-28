package com.plcoding.spring_boot_crash_course.database.repository

import com.plcoding.spring_boot_crash_course.database.model.ToDo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ToDoRepository: MongoRepository<ToDo, ObjectId> {
    fun findByOwnerId(ownerId: ObjectId): List<ToDo>
}