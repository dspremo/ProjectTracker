package com.example.projecttracker.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class GoogleDriveHelper(private val context: Context) {

    companion object {
        private const val FOLDER_NAME = "ProjectTracker"
        private const val BACKUP_FOLDER = "Backups"
        private const val REPORTS_FOLDER = "Mjesečni Izvještaji"
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("ProjectTracker")
            .build()
    }

    /**
     * Upload Excel izvještaja na Google Drive
     */
    suspend fun uploadExcelReport(
        file: File,
        account: GoogleSignInAccount,
        monthYear: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            
            // Pronađi ili kreiraj glavni folder
            val mainFolderId = findOrCreateFolder(drive, FOLDER_NAME, null)
            
            // Pronađi ili kreiraj folder za izvještaje
            val reportsFolderId = findOrCreateFolder(drive, REPORTS_FOLDER, mainFolderId)

            // Upload fajla
            val fileMetadata = DriveFile().apply {
                name = "Izvještaj_${monthYear}.xlsx"
                parents = listOf(reportsFolderId)
            }

            val mediaContent = FileContent(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                file
            )

            val uploadedFile = drive.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute()

            Result.success(uploadedFile.webViewLink ?: uploadedFile.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Backup baze podataka na Google Drive
     */
    suspend fun backupDatabase(
        databaseFile: File,
        account: GoogleSignInAccount
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            
            // Pronađi ili kreiraj glavni folder
            val mainFolderId = findOrCreateFolder(drive, FOLDER_NAME, null)
            
            // Pronađi ili kreiraj backup folder
            val backupFolderId = findOrCreateFolder(drive, BACKUP_FOLDER, mainFolderId)

            // Obriši stare backup-e (ostavi samo zadnja 3)
            cleanOldBackups(drive, backupFolderId, 3)

            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.getDefault())
                .format(java.util.Date())

            val fileMetadata = DriveFile().apply {
                name = "backup_${timestamp}.db"
                parents = listOf(backupFolderId)
            }

            val mediaContent = FileContent("application/octet-stream", databaseFile)

            val uploadedFile = drive.files()
                .create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            Result.success(uploadedFile.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Restore baze iz backup-a
     */
    suspend fun restoreDatabase(
        fileId: String,
        destinationFile: File,
        account: GoogleSignInAccount
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            
            drive.files().get(fileId)
                .executeMediaAndDownloadTo(destinationFile.outputStream())

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Lista dostupnih backup-a
     */
    suspend fun listBackups(account: GoogleSignInAccount): Result<List<BackupInfo>> = withContext(Dispatchers.IO) {
        try {
            val drive = getDriveService(account)
            
            val mainFolderId = findOrCreateFolder(drive, FOLDER_NAME, null)
            val backupFolderId = findOrCreateFolder(drive, BACKUP_FOLDER, mainFolderId)

            val result = drive.files().list()
                .setQ("'$backupFolderId' in parents and trashed=false")
                .setOrderBy("createdTime desc")
                .setFields("files(id, name, createdTime)")
                .execute()

            val backups = result.files.map { file ->
                BackupInfo(
                    id = file.id,
                    name = file.name,
                    createdTime = file.createdTime?.value ?: 0L
                )
            }

            Result.success(backups)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun findOrCreateFolder(drive: Drive, folderName: String, parentId: String?): String {
        val query = if (parentId != null) {
            "name='$folderName' and mimeType='application/vnd.google-apps.folder' and '$parentId' in parents and trashed=false"
        } else {
            "name='$folderName' and mimeType='application/vnd.google-apps.folder' and trashed=false"
        }

        val result = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .execute()

        return if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            val folderMetadata = DriveFile().apply {
                name = folderName
                mimeType = "application/vnd.google-apps.folder"
                if (parentId != null) {
                    parents = listOf(parentId)
                }
            }

            val folder = drive.files().create(folderMetadata)
                .setFields("id")
                .execute()

            folder.id
        }
    }

    private fun cleanOldBackups(drive: Drive, folderId: String, keepCount: Int) {
        try {
            val result = drive.files().list()
                .setQ("'$folderId' in parents and trashed=false")
                .setOrderBy("createdTime desc")
                .setFields("files(id)")
                .execute()

            if (result.files.size > keepCount) {
                result.files.drop(keepCount).forEach { file ->
                    drive.files().delete(file.id).execute()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class BackupInfo(
    val id: String,
    val name: String,
    val createdTime: Long
)
