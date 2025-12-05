//package com.example.projecttracker.utils
//
//import android.content.Context
//import android.content.Intent
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.Scope
//import com.google.api.client.extensions.android.http.AndroidHttp
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.services.drive.Drive
//import com.google.api.services.drive.DriveScopes
//import com.google.api.services.drive.model.File as DriveFile
//import java.io.File
//import java.io.FileInputStream
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class GoogleDriveHelper(private val context: Context) {
//
//    companion object {
//        const val REQUEST_CODE_SIGN_IN = 1001
//        private const val FOLDER_NAME = "ProjectTracker Backup"
//    }
//
//    fun getSignInIntent(): Intent {
//        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
//            .build()
//
//        val client = GoogleSignIn.getClient(context, signInOptions)
//        return client.signInIntent
//    }
//
//    suspend fun uploadFile(file: File, account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
//        try {
//            val credential = GoogleAccountCredential.usingOAuth2(
//                context,
//                listOf(DriveScopes.DRIVE_FILE)
//            )
//            credential.selectedAccount = account.account
//
//            val drive = Drive.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                GsonFactory.getDefaultInstance(),
//                credential
//            )
//                .setApplicationName("Project Tracker")
//                .build()
//
//            // Pronađi ili kreiraj folder
//            val folderId = findOrCreateFolder(drive)
//
//            // Upload fajla
//            val fileMetadata = DriveFile().apply {
//                name = file.name
//                parents = listOf(folderId)
//            }
//
//            val mediaContent = com.google.api.client.http.FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", file)
//
//            val uploadedFile = drive.files()
//                .create(fileMetadata, mediaContent)
//                .setFields("id, webViewLink")
//                .execute()
//
//            uploadedFile.webViewLink
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    suspend fun backupDatabase(databaseFile: File, account: GoogleSignInAccount): Boolean = withContext(Dispatchers.IO) {
//        try {
//            val credential = GoogleAccountCredential.usingOAuth2(
//                context,
//                listOf(DriveScopes.DRIVE_FILE)
//            )
//            credential.selectedAccount = account.account
//
//            val drive = Drive.Builder(
//                AndroidHttp.newCompatibleTransport(),
//                GsonFactory.getDefaultInstance(),
//                credential
//            )
//                .setApplicationName("Project Tracker")
//                .build()
//
//            val folderId = findOrCreateFolder(drive)
//
//            val fileMetadata = DriveFile().apply {
//                name = "backup_${System.currentTimeMillis()}.db"
//                parents = listOf(folderId)
//            }
//
//            val mediaContent = com.google.api.client.http.FileContent("application/octet-stream", databaseFile)
//
//            drive.files()
//                .create(fileMetadata, mediaContent)
//                .execute()
//
//            true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    private fun findOrCreateFolder(drive: Drive): String {
//        // Traži postojeći folder
//        val result = drive.files().list()
//            .setQ("name='$FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
//            .setSpaces("drive")
//            .execute()
//
//        return if (result.files.isNotEmpty()) {
//            result.files[0].id
//        } else {
//            // Kreiraj novi folder
//            val folderMetadata = DriveFile().apply {
//                name = FOLDER_NAME
//                mimeType = "application/vnd.google-apps.folder"
//            }
//
//            val folder = drive.files().create(folderMetadata)
//                .setFields("id")
//                .execute()
//
//            folder.id
//        }
//    }
//}