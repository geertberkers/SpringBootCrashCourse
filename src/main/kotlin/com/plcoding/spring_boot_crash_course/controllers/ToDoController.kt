package com.plcoding.spring_boot_crash_course.controllers

import com.plcoding.spring_boot_crash_course.controllers.NoteController.NoteResponse
import com.plcoding.spring_boot_crash_course.database.model.Note
import com.plcoding.spring_boot_crash_course.database.model.ToDo
import com.plcoding.spring_boot_crash_course.database.repository.ToDoRepository
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Update
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/todos")
class ToDoController(
    private val repository: ToDoRepository,
    private val toDoRepository: ToDoRepository
) {

    data class ToDoRequest(
        val id: String?,
        @field:NotBlank(message = "Title can't be blank.")
        val title: String,
        val comment: String?,
        val color: Long,
    )

    data class ToDoResponse(
        val id: String,
        val title: String,
        val comment: String?,
        val color: Long,
        val createdAt: Instant,
        val finishedAt: Instant,

    )

    // TODO: Find a way to update a current TODO!
    // Created at needs to be the same, and finishedAt will be when done and comment is added
    @PostMapping
    fun save(
        @Valid @RequestBody body: ToDoRequest
    ): ToDoResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val toDo = repository.save(
            ToDo(
                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
                title = body.title,
                comment = body.comment,
                color = body.color,
                createdAt = Instant.now(),
                finishedAt = Instant.now(),
                ownerId = ObjectId(ownerId)
            )
        )

        return toDo.toResponse()
    }

    // TODO: Find out if there is an UpdateMapping or we need to use the PostMapping and edit the value.
//    @UpdateMapping
//    fun update(
//        @Valid @RequestBody body: ToDoRequest
//    ): ToDoResponse {
//        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
//        val toDo = repository.save(
//            ToDo(
//                id = body.id?.let { ObjectId(it) } ?: ObjectId.get(),
//                title = body.title,
//                comment = null,
//                color = body.color,
//                createdAt = Instant.now(),
//                ownerId = ObjectId(ownerId)
//            )
//        )
//
//        return toDo.toResponse()
//    }

    @GetMapping
    fun findByOwnerId(): List<ToDoResponse> {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        return repository.findByOwnerId(ObjectId(ownerId)).map {
            it.toResponse()
        }
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(@PathVariable id: String) {
        val toDo = toDoRepository.findById(ObjectId(id)).orElseThrow {
            IllegalArgumentException("ToDo not found")
        }
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        if(toDo.ownerId.toHexString() == ownerId) {
            repository.deleteById(ObjectId(id))
        }
    }
}

private fun ToDo.toResponse(): ToDoController.ToDoResponse {
    return ToDoController.ToDoResponse(
        id = id.toHexString(),
        title = title,
        comment = comment,
        color = color,
        createdAt = createdAt,
        finishedAt = finishedAt,
    )
}