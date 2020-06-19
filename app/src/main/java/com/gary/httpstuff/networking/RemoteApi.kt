
package com.gary.httpstuff.networking

import com.gary.httpstuff.App
import com.gary.httpstuff.model.Task
import com.gary.httpstuff.model.UserProfile
import com.gary.httpstuff.model.request.AddTaskRequest
import com.gary.httpstuff.model.request.UserDataRequest
import com.gary.httpstuff.model.response.GetTasksResponse
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


const val BASE_URL = ""

class RemoteApi {

    private val gson = Gson()

    fun loginUser(userDataRequest: UserDataRequest, onUserLoggedIn: (String?, Throwable?) -> Unit) {
        Thread(Runnable {
            val connection = URL("$BASE_URL/api/login").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.readTimeout = 10000
            connection.connectTimeout = 10000
            connection.doOutput = true
            connection.doInput = true



            val body = gson.toJson(userDataRequest)
            val bytes = body.toByteArray()

            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(bytes)
                }

                val reader = InputStreamReader(connection.inputStream)

                reader.use { input ->
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(input)

                    bufferedReader.useLines { lines ->
                        lines.forEach {
                            response.append(it.trim())
                        }
                    }
                    val jsonObject = JSONObject(response.toString())
                    val token = jsonObject.getString("token")

                    onUserLoggedIn(token, null)
                }
            } catch (error: Throwable) {
                onUserLoggedIn(null, error)
            }


            connection.disconnect()

        }).start()
    }

    fun registerUser(
        userDataRequest: UserDataRequest,
        onUserCreated: (String?, Throwable?) -> Unit
    ) {
        Thread(Runnable {
            val connection = URL("$BASE_URL/api/register").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.readTimeout = 10000
            connection.connectTimeout = 10000
            connection.doOutput = true
            connection.doInput = true

            val body = gson.toJson(userDataRequest)
            val bytes = body.toByteArray()

            try {
                connection.outputStream.use { outputStream ->
                    outputStream.write(bytes)
                }

                val reader = InputStreamReader(connection.inputStream)

                reader.use { input ->
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(input)

                    bufferedReader.useLines { lines ->
                        lines.forEach {
                            response.append(it.trim())
                        }
                    }

                    val jsonObject = JSONObject(response.toString())
                    onUserCreated(jsonObject.getString("message"), null)
                }
            } catch (error: Throwable) {
                onUserCreated(null, error)
            }

            connection.disconnect()
        }).start()
    }

    fun getTasks(onTasksReceived: (List<Task>, Throwable?) -> Unit) {
        Thread(Runnable {
            val connection = URL("$BASE_URL/api/register").openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Authorization", App.getToken())
            connection.readTimeout = 10000
            connection.connectTimeout = 10000
            connection.doOutput = true
            connection.doInput = true

            try {
                val reader = InputStreamReader(connection.inputStream)
                reader.use { input ->
                    val response = StringBuilder()
                    val bufferedReader = BufferedReader(input)

                    bufferedReader.useLines { lines ->
                        lines.forEach {
                            response.append(it.trim())
                        }
                    }
                    val taskResponse =
                        gson.fromJson(response.toString(), GetTasksResponse::class.java)
                    onTasksReceived(taskResponse.notes.filter { !it.isCompleted } ?: listOf(), null)
                }
            } catch (error: Throwable) {
                onTasksReceived(emptyList(), error)
            }
            connection.disconnect()
        }).start()

    }
        fun deleteTask(onTaskDeleted: (Throwable?) -> Unit) {
            onTaskDeleted(null)
        }

        fun completeTask(taskId: String, onTaskCompleted: (Throwable?) -> Unit) {
            Thread(Runnable {
                val connection = URL("$BASE_URL/api/note/complete?id=$taskId").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", App.getToken())
                connection.readTimeout = 10000
                connection.connectTimeout = 10000
                connection.doOutput = true
                connection.doInput = true

                try {
                    val reader = InputStreamReader(connection.inputStream)
                    reader.use { input ->
                        val response = StringBuilder()
                        val bufferedReader = BufferedReader(input)

                        bufferedReader.useLines { lines ->
                            lines.forEach {
                                response.append(it.trim())
                            }
                        }
                        val jsonObject = JSONObject(response.toString())

                        val task = Task(
                            jsonObject.getString("id"),
                            jsonObject.getString("title"),
                            jsonObject.getString("content"),
                            jsonObject.getBoolean("isCompleted"),
                            jsonObject.getInt("taskPriority")
                        )

                     onTaskCompleted(null)
                    }
                } catch (error: Throwable) {
                    onTaskCompleted(error)
                }
                connection.disconnect()
            }).start()
        }

        fun addTask(addTaskRequest: AddTaskRequest, onTaskCreated: (Task?, Throwable?) -> Unit) {

            Thread(Runnable {
                val connection = URL("$BASE_URL/api/login").openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", App.getToken())
                connection.readTimeout = 10000
                connection.connectTimeout = 10000
                connection.doOutput = true
                connection.doInput = true

                val body = gson.toJson(addTaskRequest)

                try {
                    connection.outputStream.use { outputStream ->
                        outputStream.write(body.toByteArray())
                    }

                    val reader = InputStreamReader(connection.inputStream)

                    reader.use { input ->
                        val response = StringBuilder()
                        val bufferedReader = BufferedReader(input)

                        bufferedReader.useLines { lines ->
                            lines.forEach {
                                response.append(it.trim())
                            }
                        }
                        val jsonObject = JSONObject(response.toString())

                        val task = Task(
                            jsonObject.getString("id"),
                            jsonObject.getString("title"),
                            jsonObject.getString("content"),
                            jsonObject.getBoolean("isCompleted"),
                            jsonObject.getInt("taskPriority")
                        )

                        onTaskCreated(task, null)
                    }
                } catch (error: Throwable) {
                    onTaskCreated(null, error)
                }
                connection.disconnect()
            }).start()
        }

        fun getUserProfile(onUserProfileReceived: (UserProfile?, Throwable?) -> Unit) {
            onUserProfileReceived(UserProfile("mail@mail.com", "NotFilip", 10), null)
        }
    }

